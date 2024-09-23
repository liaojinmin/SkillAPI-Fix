package com.sucy.skill.api.player;

import com.rit.sucy.config.FilterType;
import com.rit.sucy.player.TargetHelper;
import com.rit.sucy.version.VersionManager;
import com.rit.sucy.version.VersionPlayer;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.event.*;
import com.sucy.skill.api.enums.*;
import com.sucy.skill.api.skills.PassiveSkill;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillShot;
import com.sucy.skill.api.skills.TargetSkill;
import com.sucy.skill.data.GroupSettings;
import com.sucy.skill.data.PlayerEquips;
import com.sucy.skill.dynamic.EffectComponent;
import com.sucy.skill.language.ErrorNodes;
import com.sucy.skill.language.RPGFilter;
import com.sucy.skill.listener.AttributeListener;
import com.sucy.skill.log.LogType;
import com.sucy.skill.log.Logger;
import com.sucy.skill.manager.AttributeManager;
import com.sucy.skill.screen.AttributeGermScreen;
import com.sucy.skill.screen.AttributeScreenKt;
import com.sucy.skill.utils.AttributeParseUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static com.sucy.skill.api.event.PlayerSkillCastFailedEvent.Cause.*;


public class PlayerData {

    private final ReentrantLock lock = new ReentrantLock();

    private final HashMap<String, PlayerClass> classes = new HashMap<>();

    private final HashMap<String, PlayerSkill> skills = new HashMap<>();

    /**
     * 入库数据
     * key=属性
     * value=加点等级
     **/
    public final HashMap<String, Integer> points = new HashMap<>();

    /**
     * 无源临时数据
     */
    public final HashMap<String, Double> bonusAttrib = new HashMap<>();

    /**
     * 有源临时属性
     **/
    public final ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> addAttrib
            = new ConcurrentHashMap<>();

    private final AtomicLong manaRestoreTick = new AtomicLong(0);
    private final OfflinePlayer  player;
    private PlayerEquips   equips;
    private int            keyTimer;
    private double         mana;
    private double         maxMana;
    private double         bonusHealth;
    private double         bonusMana;
    private double         lastHealth;
    private double         hunger;
    private boolean        passive;
    private int            attribPoints;
    private boolean init = false;


    /**
     * Initializes a new account data representation for a player.
     *
     * @param player player to store the data for
     */
    public PlayerData(OfflinePlayer player) {
        this.player = player;
        this.equips = new PlayerEquips(this);
        this.hunger = 1;
        for (String group : SkillAPI.getGroups()) {
            GroupSettings settings = SkillAPI.getSettings().getGroupSettings(group);
            RPGClass rpgClass = settings.getDefault();

            if (rpgClass != null && settings.getPermission() == null) {
                setClass(rpgClass);
            }
        }
    }

    /**
     * Retrieves the Bukkit player object of the owner
     *
     * @return Bukkit player object of the owner or null if offline
     */
    public Player getPlayer() {
        return new VersionPlayer(player).getPlayer();
    }

    /**
     * Retrieves the name of the owner
     *
     * @return name of the owner
     */
    public String getPlayerName() {
        return player.getName();
    }

    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    public boolean getInit() {
        lock.lock();
        try {
            return init;
        } finally {
            lock.unlock();
        }
    }

    public void setInit(boolean value) {
        lock.lock();
        try {
            init = value;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return equipped item data
     */
    public PlayerEquips getEquips() {
        return equips;
    }

    /**
     * @return health during last logout
     */
    public double getLastHealth() {
        return lastHealth;
    }

    /**
     * Used by the API for restoring health - do not use this.
     *
     * @param health health logged off with
     */
    public void setLastHealth(double health) {
        lastHealth = health;
    }

    /**
     * The hunger value here is not representative of the player's total hunger,
     * rather the amount left of the next hunger point. This is manipulated by
     * attributes were if an attribute says a player has twice as much "hunger"
     * as normal, this will go down by decimals to slow the decay of hunger.
     *
     * @return amount of the next hunger point the player has
     */
    public double getHungerValue() {
        return hunger;
    }

    /**
     * @param hungerValue new hunger value
     * @see PlayerData#getHungerValue
     */
    public void setHungerValue(final double hungerValue) {
        this.hunger = hungerValue;
    }

    public int subtractHungerValue(final double amount) {
        final double scaled = amount / scaleStat(AttributeManager.HUNGER, amount);
        final int lost = scaled >= hunger ? (int) (scaled - hunger) + 1 : 0;
        this.hunger += lost - amount;
        return lost;
    }

    /**
     * Ends the initialization flag for the data. Used by the
     * API to avoid async issues. Do not use this in other
     * plugins.
     */
    public void endInit() {
        init = false;
    }


    ///////////////////////////////////////////////////////
    //                                                   //
    //                    Attributes                     //
    //                                                   //
    ///////////////////////////////////////////////////////

    public int getAddAttribute(String key) {
        int infos = 0;
        for (ConcurrentHashMap<String, Integer> value : addAttrib.values()) {
            if (value.containsKey(key)) {
                infos += value.getOrDefault(key, 0);
            }
        }
        return infos;
    }



    public double getAttribute(String key) {
        double total = 0;
        if (points.containsKey(key)) {
            total += points.get(key);
        }
        if (bonusAttrib.containsKey(key)) {
            total += bonusAttrib.get(key);
        }
        for (ConcurrentHashMap<String, Integer> map : addAttrib.values()) {
            if (map.containsKey(key)) {
                total += map.get(key);
            }
        }
        for (PlayerClass playerClass : getClasses()) {
            total += playerClass.getData().getAttribute(key, playerClass.getLevel());
        }
        return AttributeParseUtils.round(total, 1);
    }

    /**
     * Gets the number of attribute points invested in the
     * given attribute
     *
     * @param key attribute key
     *
     * @return number of invested points
     */
    public int getInvestedAttribute(String key) {
        return points.getOrDefault(key.toLowerCase(), 0);
    }


    /**
     * Invests a point in the attribute if the player
     * has any remaining attribute points. If the player
     * has no remaining points, this will do nothing.
     *
     * @param key attribute key
     *
     * @return whether or not it was successfully upgraded
     */
    public boolean upAttribute(String key) {
        key = key.toLowerCase();
        int current = getInvestedAttribute(key);
        int max = SkillAPI.getAttributeManager().getAttribute(key).getMax();
        if (attribPoints > 0 && current < max) {
            points.put(key, current + 1);
            attribPoints--;

            PlayerUpAttributeEvent event = new PlayerUpAttributeEvent(this, key);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                points.put(key, current);
                attribPoints++;
            } else { return true; }
        }
        return false;
    }

    /**
     * Gives the player attribute points without costing
     * attribute points.
     *
     * @param key    attribute to give points for
     * @param amount amount to give
     */
    public void giveAttribute(String key, int amount) {
        key = key.toLowerCase();
        int current = getInvestedAttribute(key);
        int max = SkillAPI.getAttributeManager().getAttribute(key).getMax();
        amount = Math.min(amount + current, max);
        if (amount > current) {
            points.put(key, amount);
            AttributeListener.updatePlayer(this);
        }
    }

    /**
     * Adds bonus attributes to the player. These do not count towards
     * the max invest amount and cannot be refunded.
     *
     * @param key    attribute key
     * @param amount amount to add
     */
    public void addBonusAttributes(String key, Double amount) {
        key = SkillAPI.getAttributeManager().normalize(key);
        amount += bonusAttrib.getOrDefault(key, 0.0);
        if (amount <= 0) {
            amount = 0.0;
        }
        bonusAttrib.put(key, amount);
        AttributeListener.updatePlayer(this);
    }

    /**
     * Refunds an attribute point from the given attribute
     * if there are any points invested in it. If there are
     * none, this will do nothing.
     *
     * @param key attribute key
     */
    public boolean refundAttribute(String key) {
        key = key.toLowerCase();
        int current = getInvestedAttribute(key);
        if (current > 0) {
            PlayerRefundAttributeEvent event = new PlayerRefundAttributeEvent(this, key);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) { return false; }

            attribPoints += 1;
            points.put(key, current - 1);
            if (current - 1 <= 0) { points.remove(key); }
            AttributeListener.updatePlayer(this);

            return true;
        }
        return false;
    }

    /**
     * Refunds all spent attribute points for a specific attribute
     */
    public void refundAttributes(String... key) {
        boolean update = false;
        for (String a : key) {
            update = true;
            a = a.toLowerCase();
            attribPoints += getInvestedAttribute(a);
            points.remove(a);
        }
        if (update) {
            AttributeListener.updatePlayer(this);
        }
    }

    /**
     * @return 如果玩家有可返回的属性点，则true
     */
    public boolean hasInvestedAttributes() {
        for (String key : points.keySet()) {
            if (getInvestedAttribute(key) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Refunds all spent attribute points
     */
    public void refundAttributes() {
        refundAttributes(points.keySet().toArray(new String[0]));
    }

    /**
     * Retrieves the current number of attribute points the player has
     *
     * @return attribute point total
     */
    public int getAttributePoints() {
        return attribPoints;
    }

    /**
     * Gives the player attribute points
     *
     * @param amount amount of attribute points
     */
    public void giveAttribPoints(int amount) {
        attribPoints += amount;
    }

    /**
     * Sets the current amount of attribute points
     *
     * @param amount amount of points to have
     */
    public void setAttribPoints(int amount) {
        attribPoints = amount;
    }

    /**
     * Scales a stat value using the player's attributes
     *
     * @param stat  stat key
     * @param value base value
     *
     * @return modified value
     */
    public double scaleStat(final String stat, final double value) {
        final AttributeManager manager = SkillAPI.getAttributeManager();
        if (manager == null) { return value; }

        final List<AttributeManager.Attribute> matches = manager.forStat(stat);
        if (matches == null) { return value; }

        double modified = value;
        for (final AttributeManager.Attribute attribute : matches) {
            double amount = getAttribute(attribute.getKey());
            if (amount > 0) {
                modified = attribute.modifyStat(stat, modified, amount);
            }
        }
        return modified;
    }




    /**
     * Opens the attribute menu for the player
     */
    public void openAttributeMenu(boolean isGerm) {
        Player player = getPlayer();
        if (player != null) {
            if (isGerm) {
                new AttributeGermScreen(player, this).openGui(player);
            } else AttributeScreenKt.openAttributeScreen(player, this);
        }
    }

    /**
     * Retrieves the player's attribute data.
     * Modifying this will modify the player's
     * actual data.
     *
     * @return the player's attribute data
     */
    public HashMap<String, Integer> getAttributeData() {
        return points;
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                      Skills                       //
    //                                                   //
    ///////////////////////////////////////////////////////

    /**
     * Checks if the owner has a skill by name. This is not case-sensitive
     * and does not check to see if the skill is unlocked. It only checks if
     * the skill is available to upgrade/use.
     *
     * @param name name of the skill
     *
     * @return true if has the skill, false otherwise
     */
    public boolean hasSkill(String name) {
        return name != null && skills.containsKey(name.toLowerCase());
    }

    /**
     * Retrieves a skill of the owner by name. This is not case-sensitive.
     *
     * @param name name of the skill
     *
     * @return data for the skill or null if the player doesn't have the skill
     */
    public PlayerSkill getSkill(String name) {
        if (name == null) { return null; }
        return skills.get(name.toLowerCase());
    }

    public int getInvestedSkillPoints() {
        int total = 0;
        for (PlayerSkill playerSkill : skills.values()) {
            total += playerSkill.getInvestedCost();
        }
        return total;
    }

    /**
     * Retrieves all of the skill data the player has. Modifying this
     * collection will not modify the player's owned skills but modifying
     * one of the elements will change that element's data for the player.
     *
     * @return collection of skill data for the owner
     */
    public Collection<PlayerSkill> getSkills() {
        return skills.values();
    }

    /**
     * Retrieves the level of a skill for the owner. This is not case-sensitive.
     *
     * @param name name of the skill
     *
     * @return level of the skill or 0 if not found
     */
    public int getSkillLevel(String name) {
        PlayerSkill skill = getSkill(name);
        return skill == null ? 0 : skill.getLevel();
    }

    /**
     * Gives the player a skill outside of the normal class skills.
     * This skill will not show up in a skill tree.
     *
     * @param skill skill to give the player
     */
    public void giveSkill(Skill skill) {
        giveSkill(skill, null);
    }

    /**
     * Gives the player a skill using the class data as a parent. This
     * skill will not show up in a skill tree.
     *
     * @param skill  skill to give the player
     * @param parent parent class data
     */
    public void giveSkill(Skill skill, PlayerClass parent) {
        String key = skill.getKey();
        if (!skills.containsKey(key)) {
            PlayerSkill data = new PlayerSkill(this, skill, parent);
            skills.put(key, data);
            autoLevel(skill);
        }
    }

    /**
     * Attempts to auto-level any skills that are able to do so
     */
    public void autoLevel() {

        final Player player = getPlayer();
        if (player == null) { return; }

        for (PlayerSkill skill : skills.values()) {
            if (skill.getData().isAllowed(player)) {
                autoLevel(skill.getData());
            }
        }
    }

    private void autoLevel(Skill skill) {
        PlayerSkill data = skills.get(skill.getKey());
        if (data == null || getPlayer() == null || !skill.isAllowed(getPlayer())) { return; }

        int lastLevel = data.getLevel();
        while (data.getData().canAutoLevel(lastLevel)
                && !data.isMaxed()
                && data.getLevelReq() <= data.getPlayerClass().getLevel()) {
            upgradeSkill(skill);
            if (lastLevel == data.getLevel()) {
                break;
            }
            lastLevel++;
        }
    }

    /**
     * Upgrades a skill owned by the player. The player must own the skill,
     * have enough skill points, meet the level and skill requirements, and
     * not have maxed out the skill already in order to upgrade the skill.
     * This will consume the skill point cost while upgrading the skill.
     *
     * @param skill skill to upgrade
     *
     * @return true if successfully was upgraded, false otherwise
     */
    public boolean upgradeSkill(Skill skill) {
        // Cannot be null
        if (skill == null) {
            return false;
        }

        // Must be a valid available skill
        PlayerSkill data = skills.get(skill.getName().toLowerCase());
        if (data == null) {
            return false;
        }

        // Must meet any skill requirements
        if (!skill.isCompatible(this) || !skill.hasInvestedEnough(this) || !skill.hasDependency(this)) {
            return false;
        }

        int level = data.getPlayerClass().getLevel();
        int points = data.getPlayerClass().getPoints();
        int cost = data.getCost();
        if (!data.isMaxed() && level >= data.getLevelReq() && points >= cost) {
            // Upgrade event
            PlayerSkillUpgradeEvent event = new PlayerSkillUpgradeEvent(this, data, cost);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            // Apply upgrade
            data.getPlayerClass().usePoints(cost);
            forceUpSkill(data);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Forcefully upgrades a skill, not letting other plugins
     * cancel it and ignoring any requirements to do so
     *
     * @param skill skill to forcefully upgrade
     */
    public void forceUpSkill(PlayerSkill skill) {
        skill.addLevels(1);

        // Passive calls
        if (passive) {
            Player player = getPlayer();
            if (player != null && skill.getData() instanceof PassiveSkill) {
                if (skill.getLevel() == 1) {
                    ((PassiveSkill) skill.getData()).initialize(player, skill.getLevel());
                } else {
                    ((PassiveSkill) skill.getData()).update(player, skill.getLevel() - 1, skill.getLevel());
                }
            }

            // Unlock event
            if (skill.getLevel() == 1) {
                Bukkit.getPluginManager().callEvent(new PlayerSkillUnlockEvent(this, skill));
                this.autoLevel();
            }
        }
    }

    /**
     * Downgrades a skill owned by the player. The player must own the skill and it must
     * not currently be level 0 for the player to downgrade the skill. This will refund
     * the skill point cost when downgrading the skill.
     *
     * @param skill skill to downgrade
     *
     * @return true if successfully downgraded, false otherwise
     */
    public boolean downgradeSkill(Skill skill) {
        // Cannot be null
        if (skill == null) {
            return false;
        }

        // Must be a valid available skill
        PlayerSkill data = skills.get(skill.getName().toLowerCase());
        if (data == null) {
            return false;
        }

        // Must not be a free skill
        if (data.getCost() == 0) {
            return false;
        }

        // Must not be required by another skill
        for (PlayerSkill s : skills.values()) {
            if (s.getData().getSkillReq() != null
                    && s.getData().getSkillReq().equalsIgnoreCase(skill.getName())
                    && data.getLevel() <= s.getData().getSkillReqLevel()
                    && s.getLevel() > 0) {
                return false;
            }
        }

        int cost = skill.getCost(data.getLevel() - 1);
        if (data.getLevel() > 0) {
            // Upgrade event
            PlayerSkillDowngradeEvent event = new PlayerSkillDowngradeEvent(this, data, cost);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            // Apply upgrade
            data.getPlayerClass().givePoints(cost, PointSource.REFUND);
            forceDownSkill(data);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Forcefully downgrades a skill, not letting other plugins
     * stop it and ignoring any skill requirements to do so.
     *
     * @param skill skill to forcefully downgrade
     */
    public void forceDownSkill(PlayerSkill skill) {
        skill.addLevels(-1);

        // Passive calls
        Player player = getPlayer();
        if (player != null && skill.getData() instanceof PassiveSkill) {
            if (skill.getLevel() == 0) {
                ((PassiveSkill) skill.getData()).stopEffects(player, 1);
            } else {
                ((PassiveSkill) skill.getData()).update(player, skill.getLevel() + 1, skill.getLevel());
            }
        }

    }

    /**
     * Refunds a skill for the player, resetting it down
     * to level 0 and giving back any invested skill points.
     *
     * @param skill skill to refund
     */
    public void refundSkill(PlayerSkill skill) {
        Player player = getPlayer();

        if (skill.getCost() == 0 || skill.getLevel() == 0) { return; }

        skill.getPlayerClass().givePoints(skill.getInvestedCost(), PointSource.REFUND);
        skill.setLevel(0);

        if (player != null && (skill.getData() instanceof PassiveSkill)) {
            ((PassiveSkill) skill.getData()).stopEffects(player, 1);
        }
    }

    /**
     * Refunds all skills for the player
     */
    public void refundSkills() {
        for (PlayerSkill skill : skills.values()) {
            refundSkill(skill);
        }
    }


    ///////////////////////////////////////////////////////
    //                                                   //
    //                     Classes                       //
    //                                                   //
    ///////////////////////////////////////////////////////

    /**
     * Checks whether or not the player has as least one class they have professed as.
     *
     * @return true if professed, false otherwise
     */
    public boolean hasClass() {
        return classes.size() > 0;
    }

    /**
     * Checks whether or not a player has a class within the given group
     *
     * @param group class group to check
     *
     * @return true if has a class in the group, false otherwise
     */
    public boolean hasClass(String group) {
        return classes.containsKey(group);
    }

    /**
     * Retrieves the collection of the data for classes the player has professed as.
     *
     * @return collection of the data for professed classes
     */
    public Collection<PlayerClass> getClasses() {
        return classes.values();
    }

    /**
     * Retrieves the data of a class the player professed as by group. This is
     * case-sensitive.
     *
     * @param group group to get the profession for
     *
     * @return professed class data or null if not professed for the group
     */
    public PlayerClass getClass(String group) {
        return classes.get(group);
    }

    /**
     * Retrieves the data of the professed class under the com class group. The
     * "com" group is determined by the setting in the config.
     *
     * @return com professed class data or null if not professed for the com group
     */
    public PlayerClass getMainClass() {
        String main = SkillAPI.getSettings().getMainGroup();
        if (classes.containsKey(main)) {
            return classes.get(main);
        } else if (classes.size() > 0) {
            return classes.values().toArray(new PlayerClass[0])[0];
        } else {
            return null;
        }
    }

    /**
     * Sets the professed class for the player for the corresponding group. This
     * will not save any skills, experience, or levels of the previous class if
     * there was any. The new class will start at level 1 with 0 experience.
     *
     * @param rpgClass class to assign to the player
     *
     * @return the player-specific data for the new class
     */
    public PlayerClass setClass(RPGClass rpgClass) {
        PlayerClass c = classes.remove(rpgClass.getGroup());
        if (c != null) {
            for (Skill skill : c.getData().getSkills()) {
                skills.remove(skill.getName().toLowerCase());
            }
        } else {
            attribPoints += rpgClass.getGroupSettings().getStartingAttribs();
        }

        PlayerClass classData = new PlayerClass(this, rpgClass);
        classes.put(rpgClass.getGroup(), classData);

        // Add in missing skills
        for (Skill skill : rpgClass.getSkills()) {
            giveSkill(skill, classData);
        }

        updateHealthAndMana(getPlayer());
        return classes.get(rpgClass.getGroup());
    }

    /**
     * Checks whether or not the player is professed as the class
     * without checking child classes.
     *
     * @param rpgClass class to check
     *
     * @return true if professed as the specific class, false otherwise
     */
    public boolean isExactClass(RPGClass rpgClass) {
        if (rpgClass == null) { return false; }
        PlayerClass c = classes.get(rpgClass.getGroup());
        return (c != null) && (c.getData() == rpgClass);
    }

    /**
     * Checks whether or not the player is professed as the class
     * or any of its children.
     *
     * @param rpgClass class to check
     *
     * @return true if professed as the class or one of its children, false otherwise
     */
    public boolean isClass(RPGClass rpgClass) {
        if (rpgClass == null) {
            return false;
        }

        PlayerClass pc = classes.get(rpgClass.getGroup());
        if (pc == null) { return false; }

        RPGClass temp = pc.getData();
        while (temp != null) {
            if (temp == rpgClass) {
                return true;
            }
            temp = temp.getParent();
        }

        return false;
    }

    /**
     * Checks whether or not the player can profess into the given class. This
     * checks to make sure the player is currently professed as the parent of the
     * given class and is high enough of a level to do so.
     *
     * @param rpgClass class to check
     *
     * @return true if can profess, false otherwise
     */
    public boolean canProfess(RPGClass rpgClass) {
        Player p = getPlayer();
        if (p == null || !rpgClass.isAllowed(p)) {
            return false;
        }

        if (classes.containsKey(rpgClass.getGroup())) {
            PlayerClass current = classes.get(rpgClass.getGroup());
            return rpgClass.getParent() == current.getData() && current.getData().getMaxLevel() <= current.getLevel();
        } else {
            return !rpgClass.hasParent();
        }
    }

    /**
     * Resets the class data for the owner under the given group. This will remove
     * the profession entirely, leaving no remaining data until the player professes
     * again to a starting class.
     *
     * @param group group to reset
     */
    public void reset(String group) {
        GroupSettings settings = SkillAPI.getSettings().getGroupSettings(group);
        if (!settings.canReset()) { return; }

        PlayerClass playerClass = classes.remove(group);
        if (playerClass != null) {
            // Remove skills
            RPGClass data = playerClass.getData();
            for (Skill skill : data.getSkills()) {
                PlayerSkill ps = skills.remove(skill.getName().toLowerCase());
                if (ps != null && ps.isUnlocked() && ps.getData() instanceof PassiveSkill) {
                    ((PassiveSkill) ps.getData()).stopEffects(getPlayer(), ps.getLevel());
                }
            }
            // Call the event
            Bukkit.getPluginManager().callEvent(new PlayerClassChangeEvent(playerClass, data, null));
        }

        // Restore default class if applicable
        RPGClass rpgClass = settings.getDefault();
        if (rpgClass != null && settings.getPermission() == null) {
            setClass(rpgClass);
        }
        resetAttribs();
    }

    /**
     * Resets all profession data for the player. This clears all professions the player
     * has, leaving no remaining data until the player professes again to a starting class.
     */
    public void resetAll() {
        ArrayList<String> keys = new ArrayList<>(classes.keySet());
        for (String key : keys) { reset(key); }
    }

    /**
     * Resets attributes for the player
     */
    public void resetAttribs() {
        points.clear();
        attribPoints = 0;
        for (PlayerClass c : classes.values()) {
            GroupSettings s = c.getData().getGroupSettings();
            attribPoints += s.getStartingAttribs() + s.getAttribsForLevels(c.getLevel(), 1);
        }
        AttributeListener.updatePlayer(this);
        updateHealthAndMana(getPlayer());
    }

    /**
     * Professes the player into the class if they are able to. This will
     * reset the class data if the group options are set to reset upon
     * profession. Otherwise, all skills, experience, and levels of the
     * current class under the group will be retained and carried over into
     * the new profession.
     *
     * @param rpgClass class to profess into
     *
     * @return true if successfully professed, false otherwise
     */
    public boolean profess(RPGClass rpgClass) {
        if (rpgClass != null && canProfess(rpgClass)) {
            final PlayerClass previousData = classes.get(rpgClass.getGroup());
            final RPGClass previous = previousData == null ? null : previousData.getData();

            // Pre-class change event in case someone wants to stop it
            final PlayerPreClassChangeEvent event = new PlayerPreClassChangeEvent(
                    this,
                    previousData,
                    previous,
                    rpgClass);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            // Reset data if applicable
            final boolean isResetting = SkillAPI.getSettings().getGroupSettings(rpgClass.getGroup()).isProfessReset();
            if (isResetting) {
                reset(rpgClass.getGroup());
            }

            // Inherit previous class data if any
            final PlayerClass current;
            if (previousData == null || isResetting) {
                current = new PlayerClass(this, rpgClass);
                classes.put(rpgClass.getGroup(), current);
                attribPoints += rpgClass.getGroupSettings().getStartingAttribs();
            } else {
                current = previousData;
                previousData.setClassData(rpgClass);
            }

            // Add skills
            for (Skill skill : rpgClass.getSkills()) {
                if (!skills.containsKey(skill.getKey())) {
                    skills.put(skill.getKey(), new PlayerSkill(this, skill, current));
                }
            }

            Bukkit.getPluginManager().callEvent(new PlayerClassChangeEvent(current, previous, current.getData()));
            resetAttribs();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gives experience to the player from the given source
     *
     * @param amount amount of experience to give
     * @param source source of the experience
     */
    public void giveExp(double amount, ExpSource source) {
        giveExp(amount, source, true);
    }

    /**
     * Gives experience to the player from the given source
     *
     * @param amount amount of experience to give
     * @param source source of the experience
     * @param message whether or not to show the configured message if enabled
     */
    public void giveExp(double amount, ExpSource source, boolean message) {
        for (PlayerClass playerClass : classes.values()) {
            playerClass.giveExp(amount, source, message);
        }
    }

    /**
     * Causes the player to lose experience as a penalty (generally for dying)
     */
    public void loseExp() {
        for (PlayerClass playerClass : classes.values()) {
            double penalty = playerClass.getData().getGroupSettings().getDeathPenalty();
            if (penalty > 0) {
                playerClass.loseExp(penalty);
            }
        }
    }

    /**
     * Gives levels to the player for all classes matching the experience source
     *
     * @param amount amount of levels to give
     * @param source source of the levels
     */
    public boolean giveLevels(int amount, ExpSource source) {
        boolean success = false;
        for (PlayerClass playerClass : classes.values()) {
            RPGClass data = playerClass.getData();
            if (data.receivesExp(source)) {
                success = true;
                playerClass.giveLevels(amount);
            }
        }
        updateHealthAndMana(getPlayer());
        return success;
    }

    /**
     * Gives skill points to the player for all classes matching the experience source
     *
     * @param amount amount of levels to give
     * @param source source of the levels
     */
    public void givePoints(int amount, ExpSource source) {
        for (PlayerClass playerClass : classes.values()) {
            if (playerClass.getData().receivesExp(source)) {
                playerClass.givePoints(amount);
            }
        }
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                  Health and Mana                  //
    //                                                   //
    ///////////////////////////////////////////////////////

    /**
     * Updates the player's max health and mana using class data.
     *
     * @param player player to update the health and mana for
     */
    public void updateHealthAndMana(Player player) {
        if (player == null) {
            return;
        }

        // Update maxes
        double health = bonusHealth;
        maxMana = bonusMana;
        for (PlayerClass c : classes.values()) {
            health += c.getHealth();
            maxMana += c.getMana();
        }
        if (health == bonusHealth) {
            health += SkillAPI.getSettings().getDefaultHealth();
        }
        if (health <= 0) {
            health = SkillAPI.getSettings().getDefaultHealth();
        }
        if (SkillAPI.getSettings().isModifyHealth()) { player.setMaxHealth(health); }


        mana = Math.min(mana, maxMana);

       // System.out.println("updateHealthAndMana "+mana);

        // Health scaling is available starting with 1.6.2
        if (SkillAPI.getSettings().isOldHealth()) {
            player.setHealthScaled(true);
            player.setHealthScale(20);
        } else {
            player.setHealthScaled(false);
        }
    }

    /**
     * Gives max health to the player. This does not carry over to other accounts
     * and will reset when SkillAPI is disabled. This does however carry over through
     * death and professions. This will accept negative values.
     *
     * @param amount amount of bonus health to give
     */
    public void addMaxHealth(double amount) {
        bonusHealth += amount;
        final Player player = getPlayer();
        if (player != null) {
            if (VersionManager.isVersionAtLeast(VersionManager.V1_9_0)) {
                final AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                attribute.setBaseValue(attribute.getBaseValue() + amount);
            } else {
                final double newHealth = player.getMaxHealth() + amount;
                player.setMaxHealth(newHealth);
                if (player.getMaxHealth() > newHealth) {
                    player.setMaxHealth(newHealth * 2 - player.getMaxHealth());
                }
            }
        }
    }

    /**
     * Gives max mana to the player. This does not carry over to other accounts
     * and will reset when SkillAPI is disabled. This does however carry over through
     * death and professions. This will accept negative values.
     *
     * @param amount amount of bonus mana to give
     */
    public void addMaxMana(double amount) {
       // System.out.println("addMaxMana "+amount);
        bonusMana += amount;
        maxMana += amount;
        mana += amount;
    }



    /**
     * Retrieves the amount of mana the player currently has.
     *
     * @return current player mana
     */
    public double getMana() {
        return mana;
    }

    /**
     * Checks whether or not the player has at least the specified amount of mana
     *
     * @param amount required mana amount
     *
     * @return true if has the amount of mana, false otherwise
     */
    public boolean hasMana(double amount) {
        return mana >= amount;
    }

    /**
     * Retrieves the max amount of mana the player can have including bonus mana
     *
     * @return max amount of mana the player can have
     */
    public double getMaxMana() {
        return maxMana;
    }

    /**
     * Regenerates mana for the player based on the regen amounts of professed classes
     */
    public void regenMana() {
        if (this.getManaRestoreTick() > 0) {
           // System.out.println("已停止恢复Mana... tick "+this.getManaRestoreTick());
            return;
        }
      //  if (!ShieldCondition.checkBlocking(player.getPlayer())) {
            double amount = 0;
            for (PlayerClass c : classes.values()) {
                if (c.getData().hasManaRegen()) {
                    amount += c.getData().getManaRegen();
                }
            }
            if (amount > 0) {
                // System.out.println("已恢复Mana... "+amount);
                giveMana(amount, ManaSource.REGEN);
            }
      //  }
    }

    /**
     * Sets the player's amount of mana without launching events
     *
     * @param amount current mana
     */
    public void setMana(double amount) {
       // System.out.println("setMana "+amount);
        this.mana = amount;
    }


    /**
     * Gives mana to the player from an unknown source. This will not
     * cause the player's mana to go above their max amount.
     *
     * @param amount amount of mana to give
     */
    public void giveMana(double amount) {
       // System.out.println("giveMana "+amount);
        giveMana(amount, ManaSource.SPECIAL);
    }

    /**
     * Gives mana to the player from the given mana source. This will not
     * cause the player's mana to go above the max amount.
     *
     * @param amount amount of mana to give
     * @param source source of the mana
     */
    public void giveMana(double amount, ManaSource source) {
      //  System.out.println("giveMana "+amount + " source "+source.name());
        PlayerManaGainEvent event = new PlayerManaGainEvent(this, amount, source);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            Logger.log(
                    LogType.MANA,
                    2,
                    getPlayerName() + " gained " + amount + " mana due to " + event.getSource().name());

            mana += event.getAmount();
            if (mana > maxMana) {
                mana = maxMana;
            }
            if (mana < 0) {
                mana = 0;
            }
        } else { Logger.log(LogType.MANA, 2, getPlayerName() + " had their mana gain cancelled"); }
    }

    /**
     * Takes mana away from the player for an unknown reason. This will not
     * cause the player to fall below 0 mana.
     *
     * @param amount amount of mana to take away
     */
    public void useMana(double amount) {
        useMana(amount, ManaCost.SPECIAL);
    }

    /**
     * Takes mana away from the player for the specified reason. This will not
     * cause the player to fall below 0 mana.
     *
     * @param amount amount of mana to take away
     * @param cost   source of the mana cost
     */
    public void useMana(double amount, ManaCost cost) {
        PlayerManaLossEvent event = new PlayerManaLossEvent(this, amount, cost);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            Logger.log(
                    LogType.MANA,
                    2,
                    getPlayerName() + " used " + amount + " mana due to " + event.getSource().name());

            mana -= event.getAmount();
            if (mana < 0) {
                mana = 0;
            }
        }
    }

    public void setKeyTimer(int timer) {
        if (timer == 0) return;
        this.keyTimer = timer;
    }

    public void setManaRestoreTick(long tick) {
        if (tick <= 0) {
            return;
        }
        long timer = System.currentTimeMillis() + tick;
        if (timer <= manaRestoreTick.get()) {
            return;
        }
        ManaRestoreTickStartEvent startEvent = new ManaRestoreTickStartEvent(getPlayer(), timer);
        Bukkit.getPluginManager().callEvent(startEvent);
        if (startEvent.isCancelled()) {
            return;
        }
        this.manaRestoreTick.set(timer);
    }

    public int getKeyTimer(boolean reset) {
        if (reset) {
            int out = keyTimer;
            keyTimer = 0;
            return out;
        } else return keyTimer;
    }

    public int getKeyTimer() {
        return keyTimer;
    }

    public long getManaRestoreTick() {
        long timer = System.currentTimeMillis();
        long now = manaRestoreTick.get();
        if (now > 0 && timer >= now) {
            manaRestoreTick.set(0);
            ManaRestoreTickEndEvent endEvent = new ManaRestoreTickEndEvent(getPlayer());
            Bukkit.getPluginManager().callEvent(endEvent);
            //System.out.println("唤起 ManaRestoreTickEndEvent");
            return 0;
        }
        return now - timer;
    }

    /**
     * Clears bonus health/mana
     */
    public void clearBonuses() {
        bonusMana = 0;
        bonusHealth = 0;
        bonusAttrib.clear();
        equips = new PlayerEquips(this);
    }


    ///////////////////////////////////////////////////////
    //                                                   //
    //                     Functions                     //
    //                                                   //
    ///////////////////////////////////////////////////////

    /**
     * Records any data to save with class data
     *
     * @param player player to record for
     */
    public void record(Player player) {
        this.lastHealth = player.getHealth();
    }

    /**
     * Starts passive abilities for the player if they are online. This is
     * already called by the API and shouldn't be called by other plugins.
     *
     * @param player player to set the passive skills up for
     */
    public void startPassives(Player player) {
        if (player == null) {
            return;
        }
        passive = true;
        for (PlayerSkill skill : skills.values()) {
            if (skill.isUnlocked() && (skill.getData() instanceof PassiveSkill)) {
                ((PassiveSkill) skill.getData()).initialize(player, skill.getLevel());
            }
        }
    }

    private PlayerSkill passiveSkill;
    public void startPassives(Player player, PlayerSkill skill) {
      //  System.out.println("PlayerSkill "+skill.getData().getName());
      //  System.out.println("PlayerSkill Class "+skill.getData().getClass());
        if (skill.getData() instanceof PassiveSkill) {
         //   System.out.println("skill 是被动技能 "+skill.getData().getName());
            endPassives(player);
            passiveSkill = skill;
            ((PassiveSkill) passiveSkill.getData()).initialize(player, 1);
        }
    }
    public void endPassives(Player player) {
        if (passiveSkill != null) {
            ((PassiveSkill) passiveSkill.getData()).stopEffects(player, passiveSkill.getLevel());
        }
    }

    /**
     * Stops passive abilities for the player if they are online. This is already
     * called by the API and shouldn't be called by other plugins.
     *
     * @param player player to stop the passive skills for
     */
    public void stopPassives(Player player) {
        if (player == null) {
            return;
        }
        passive = false;
        for (PlayerSkill skill : skills.values()) {
            if (skill.isUnlocked() && (skill.getData() instanceof PassiveSkill)) {
                try {
                    ((PassiveSkill) skill.getData()).stopEffects(player, skill.getLevel());
                } catch (Exception ex) {
                    Logger.bug("Failed to stop passive skill " + skill.getData().getName());
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Casts a skill by name for the player. In order to cast the skill,
     * the player must be online, have the skill unlocked, have enough mana,
     * have the skill off cooldown, and have a proper target if applicable.
     *
     * @param skillName name of the skill ot cast
     *
     * @return true if successfully cast the skill, false otherwise
     */
    public boolean cast(String skillName) {
        return cast(skills.get(skillName.toLowerCase()));
    }

    /**
     * Casts a skill for the player. In order to cast the skill,
     * the player must be online, have the skill unlocked, have enough mana,
     * have the skill off cooldown, and have a proper target if applicable.
     *
     * @param skill skill to cast
     *
     * @return true if successfully cast the skill, false otherwise
     */
    public boolean cast(PlayerSkill skill) {
        // Invalid skill
        if (skill == null) { throw new IllegalArgumentException("Skill cannot be null"); }

        int level = skill.getLevel();

        // Not unlocked or on cooldown
        if (!check(skill, true, true)) { return false; }

        // Dead players can't cast skills
        Player p = getPlayer();
        if (p.isDead()) { return PlayerSkillCastFailedEvent.invoke(skill, CASTER_DEAD); }

        // Disable casting in spectator mode
        if (p.getGameMode().name().equals("SPECTATOR")) { return PlayerSkillCastFailedEvent.invoke(skill, SPECTATOR); }

        // Skill Shots
        if (skill.getData() instanceof SkillShot) {
          //  System.out.println("instanceof SkillShot "+ skill.getData().getName());
            PlayerCastSkillEvent event = new PlayerCastSkillEvent(this, skill, p);
            Bukkit.getPluginManager().callEvent(event);

            // Make sure it isn't cancelled
            if (!event.isCancelled()) {
                try {
                    // 设置魔法值恢复等待时间
                    setManaRestoreTick(skill.getManaTick());
                    setKeyTimer(skill.getKeyTimer());
                    if (((SkillShot) skill.getData()).cast(p, level)) {
                        //System.out.println(" skill applyUse");
                        return applyUse(p, skill, event.getManaCost());
                    } else {
                      //  System.out.println(" skill cast EFFECT_FAILED");
                        return PlayerSkillCastFailedEvent.invoke(skill, EFFECT_FAILED);
                    }
                } catch (Exception ex) {
                    Logger.bug("Failed to cast skill - " + skill.getData().getName() + ": Internal skill error");
                    ex.printStackTrace();
                    return PlayerSkillCastFailedEvent.invoke(skill, EFFECT_FAILED);
                }
            } else {
              //  System.out.println("  技能 " + skill.getData().getName() + " 执行事件被阻断");
                return PlayerSkillCastFailedEvent.invoke(skill, CANCELED);
            }
        }

        // Target Skills
        else if (skill.getData() instanceof TargetSkill) {
           // System.out.println("instanceof TargetSkill" + skill.getData().getName());
            LivingEntity target = TargetHelper.getLivingTarget(p, skill.getData().getRange(level));

            // Must have a target
            if (target == null) { return PlayerSkillCastFailedEvent.invoke(skill, NO_TARGET); }

            PlayerCastSkillEvent event = new PlayerCastSkillEvent(this, skill, p);
            Bukkit.getPluginManager().callEvent(event);

            // Make sure it isn't cancelled
            if (!event.isCancelled()) {
                try {
                    // 设置魔法值恢复等待时间
                    setManaRestoreTick(skill.getManaTick());
                    setKeyTimer(skill.getKeyTimer());
                    final boolean canAttack = !SkillAPI.getSettings().canAttack(p, target);
                    if (((TargetSkill) skill.getData()).cast(p, target, level, canAttack)) {
                     //   System.out.println(" skill applyUse");
                        return applyUse(p, skill, event.getManaCost());
                    } else {
                      //  System.out.println(" skill cast EFFECT_FAILED");
                        return PlayerSkillCastFailedEvent.invoke(skill, EFFECT_FAILED);
                    }
                } catch (Exception ex) {
                    Logger.bug("Failed to cast skill - " + skill.getData().getName() + ": Internal skill error");
                    ex.printStackTrace();
                    return PlayerSkillCastFailedEvent.invoke(skill, EFFECT_FAILED);
                }
            } else {
               // System.out.println("  技能 " + skill.getData().getName() + " 执行事件被阻断");
                PlayerSkillCastFailedEvent.invoke(skill, CANCELED);
            }
        }

        return false;
    }

    private boolean applyUse(final Player player, final PlayerSkill skill, final double manaCost) {
        skill.startCooldown();
        if (SkillAPI.getSettings().isShowSkillMessages()) {
            skill.getData().sendMessage(player, SkillAPI.getSettings().getMessageRadius());
        }
        if (SkillAPI.getSettings().isManaEnabled()) {
            useMana(manaCost, ManaCost.SKILL_CAST);
        }
        return true;
    }

    /**
     * Checks the cooldown and mana requirements for a skill
     *
     * @param skill    skill to check for
     * @param cooldown whether or not to check cooldowns
     * @param mana     whether or not to check mana requirements
     *
     * @return true if can use
     */
    public boolean check(PlayerSkill skill, boolean cooldown, boolean mana) {
        if (skill == null) { return false; }

        SkillStatus status = skill.getStatus();
        int level = skill.getLevel();
        double cost = skill.getData().getManaCost(level);

        // Not unlocked
        if (level <= 0) {
            return PlayerSkillCastFailedEvent.invoke(skill, NOT_UNLOCKED);
        }

        // On Cooldown
        if (status == SkillStatus.ON_COOLDOWN && cooldown) {
            SkillAPI.getLanguage().sendMessage(
                    ErrorNodes.COOLDOWN,
                    getPlayer(),
                    FilterType.COLOR,
                    RPGFilter.COOLDOWN.setReplacement(skill.getCooldown() + ""),
                    RPGFilter.SKILL.setReplacement(skill.getData().getName())
            );
            return PlayerSkillCastFailedEvent.invoke(skill, ON_COOLDOWN);
        }

        // Not enough mana
        else if (status == SkillStatus.MISSING_MANA && mana) {
            SkillAPI.getLanguage().sendMessage(
                    ErrorNodes.MANA,
                    getPlayer(),
                    FilterType.COLOR,
                    RPGFilter.SKILL.setReplacement(skill.getData().getName()),
                    RPGFilter.MANA.setReplacement(getMana() + ""),
                    RPGFilter.COST.setReplacement((int) Math.ceil(cost) + ""),
                    RPGFilter.MISSING.setReplacement((int) Math.ceil(cost - getMana()) + "")
            );
            return PlayerSkillCastFailedEvent.invoke(skill, NO_MANA);
        } else { return true; }
    }

    /**
     * Initializes the application of the data for the player
     *
     * @param player player to set up for
     */
    public void init(Player player) {
        if (!SkillAPI.getSettings().isWorldEnabled(player.getWorld())) {
            return;
        }
        AttributeListener.updatePlayer(this);
        getEquips().update(player);
        this.updateHealthAndMana(player);
        this.startPassives(player);
        if (this.getLastHealth() > 0 && !player.isDead()) {
            player.setHealth(Math.min(this.getLastHealth(), player.getMaxHealth()));
        }
    }
}
