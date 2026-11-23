
package com.sucy.skill.api.skills;

import com.rit.sucy.config.FilterType;
import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.text.TextFormatter;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.ReadOnlySettings;
import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.PlayerKillerEntityEvent;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.api.event.TrueDamageEvent;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.player.PlayerSkill;
import com.sucy.skill.data.Permissions;
import com.sucy.skill.dynamic.TempEntity;
import com.sucy.skill.gui.IconHolder;
import com.sucy.skill.hook.mythic.MythicManager;
import com.sucy.skill.language.NotificationNodes;
import com.sucy.skill.utils.target.TargetHelper;
import me.neon.libs.util.MetaKt;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a template for a skill used in the RPG system. This is
 * the class to extend when creating your own custom skills.
 */
public abstract class Skill implements IconHolder {
    private static final DecimalFormat FORMAT = new DecimalFormat("#########0.0#");

    private final ArrayList<String> description = new ArrayList<>();

    private String       key;
    private String       name;
    private String       type;
    private String       message;
    private String       skillReq;
    private int          maxLevel;
    private int          skillReqLevel;
    private boolean      needsPermission;

    /**
     * The settings for the skill which include configurable stats
     * for your mechanics and the defaults such as mana cost, level
     * requirement, skill point cost, and cooldown.
     */
    protected final Settings         settings         = new Settings();
    private final   ReadOnlySettings readOnlySettings = new ReadOnlySettings(settings);

    public Skill(String name, String type, int maxLevel) {
        this(name, type, maxLevel, null, 0);
    }

    public Skill(String name, String type, int maxLevel, String skillReq, int skillReqLevel) {
        if (name == null)
        {
            throw new IllegalArgumentException("Skill name cannot be null");
        }

        // Default values
        if (type == null)
        {
            type = "Unknown type";
        }
        if (maxLevel < 1)
        {
            maxLevel = 1;
        }

        this.key = name.toLowerCase();
        this.type = type;
        this.name = name;
        this.maxLevel = maxLevel;
        this.skillReq = skillReq;
        this.skillReqLevel = skillReqLevel;
        this.needsPermission = false;

        this.message = SkillAPI.getLanguage().getMessage(NotificationNodes.CAST, true, FilterType.COLOR).get(0);
    }


    /**
     * Checks whether or not the skill can automatically
     * level up to the next stage.
     *
     * @return true if can level up automatically, false otherwise
     * @deprecated use {@link Skill#canAutoLevel(int)} instead
     */
    @Deprecated
    public boolean canAutoLevel()
    {
        return getCost(0) == 0 && getCost(1) == 0;
    }

    /**
     * Checks whether or not the skill can automatically
     * level up to the next stage.
     *
     * @param level - the current level of the skill
     * @return true if can level up automatically to the next level, false otherwise
     */
    public boolean canAutoLevel(final int level) {
        return getCost(level) == 0;
    }

    /**
     * Retrieves the configuration key for the skill
     *
     * @return configuration key for the skill
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Retrieves the name of the skill
     *
     * @return skill name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Retrieves the max level the skill can reach
     *
     * @return max skill level
     */
    public int getMaxLevel()
    {
        return maxLevel;
    }

    /**
     * Checks whether or not the skill has a message to display when cast.
     *
     * @return true if has a message, false otherwise
     */
    public boolean hasMessage()
    {
        return message != null && message.length() > 0;
    }


    /**
     * Retrieves the message for the skill to display when cast.
     *
     * @return cast message of the skill
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Checks whether or not the skill needs a permission for a player to use it.
     *
     * @return true if requires a permission, false otherwise
     */
    public boolean needsPermission()
    {
        return needsPermission;
    }

    /**
     * Retrieves the descriptive type of the skill
     *
     * @return descriptive type of the skill
     */
    public String getType()
    {
        return type;
    }

    /**
     * Checks whether or not the skill requires another before leveling up
     *
     * @return true if requires another skill, false otherwise
     */
    public boolean hasSkillReq()
    {
        return SkillAPI.getSkill(skillReq) != null && skillReqLevel > 0;
    }

    /**
     * Retrieves the skill required to be upgraded before this one
     *
     * @return required skill
     */
    public String getSkillReq()
    {
        return skillReq;
    }

    /**
     * Retrieves the level of the required skill needed to be obtained
     * before this one can be upgraded.
     *
     * @return required skill level
     */
    public int getSkillReqLevel()
    {
        return skillReqLevel;
    }

    /**
     * Retrieves the skill's description
     *
     * @return description of the skill
     */
    public List<String> getDescription()
    {
        return description;
    }

    /**
     * Retrieves the level requirement for the skill to reach the next level
     *
     * @param level current level of the skill
     *
     * @return level requirement for the next level
     */
    public int getLevelReq(int level)
    {
        return (int) settings.getAttr(SkillAttribute.LEVEL, level + 1);
    }

    /**
     * Retrieves the mana cost of the skill
     *
     * @param level current level of the skill
     *
     * @return mana cost
     */
    public double getManaCost(int level) {
        return settings.getAttr(SkillAttribute.MANA, level);
    }

    public double getManaRatioCost(int level) {
        return settings.getAttr(SkillAttribute.MANA_RATIO, level);
    }

    public long getManaTick(int level) {
        return settings.getManaTick(level, 0);
    }

    public int getKeyTimer() {
        return settings.getKeyTimer();
    }

    /**
     * Retrieves the cooldown of the skill in seconds
     *
     * @param level current level of the skill
     *
     * @return cooldown
     */
    public double getCooldown(int level)
    {
        return settings.getAttr(SkillAttribute.COOLDOWN, level);
    }

    /**
     * Retrieves the range of the skill in blocks
     *
     * @param level current level of the skill
     *
     * @return target range
     */
    public double getRange(int level)
    {
        return settings.getAttr(SkillAttribute.RANGE, level);
    }

    /**
     * Retrieves the skill point cost of the skill
     *
     * @param level current level of the skill
     *
     * @return skill point cost
     */
    public int getCost(int level)
    {
        return (int) settings.getAttr(SkillAttribute.COST, level + 1);
    }

    /**
     * Retrieves the settings for the skill in a read-only format
     *
     * @return settings for the skill in a read-only format
     */
    public ReadOnlySettings getSettings()
    {
        return readOnlySettings;
    }

    @Override
    public boolean isAllowed(final Player player) {
        return !needsPermission()
                || player.hasPermission(Permissions.SKILL)
                || player.hasPermission(Permissions.SKILL + "." + name.toLowerCase().replace(" ", "-"));
    }

    public boolean hasDependency(final PlayerData playerData) {
        // Must meet any skill requirements
        if (getSkillReq() != null) {
            PlayerSkill req = playerData.getSkill(getSkillReq());
            return req == null || req.getLevel() >= getSkillReqLevel();
        }
        return true;
    }

    public boolean isCompatible(final PlayerData playerData) {
        for (final String skillName : settings.getStringList(SkillAttribute.INCOMPATIBLE)) {
            final PlayerSkill skill = playerData.getSkill(skillName);
            if (skill != null && skill.getLevel() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean hasInvestedEnough(final PlayerData playerData) {
        final PlayerSkill skill = playerData.getSkill(name);
        if (skill == null) {
            return false;
        }

        final double reqPoints = settings.getAttr(SkillAttribute.POINTS_SPENT_REQ, skill.getLevel(), 0);
        return playerData.getInvestedSkillPoints() >= reqPoints;
    }


    /**
     * Formats an attribute name for applying to the indicator
     *
     * @param key attribute key
     *
     * @return formatted attribute name
     */
    protected String getAttrName(String key) {
        return TextFormatter.format(key);
    }

    /**
     * Retrieves an attribute value for using in the icon lore
     *
     * @param caster owner of the skill
     * @param key    attribute key
     * @param level  skill level
     *
     * @return attribute value
     */
    protected Object getAttr(final SkillContext context, LivingEntity caster, String key, int level) {
        Object result = settings.getObj(key, level);
        if (result instanceof Double)
        {
            return format((Double) result);
        }
        return result;
    }

    /**
     * Formats a double value to prevent excessive decimals
     *
     * @param value double value to format
     *
     * @return formatted double value
     */
    protected String format(double value) {
        String result = FORMAT.format(value);
        if (result.endsWith(".0"))
            return result.substring(0, result.length() - 2);
        else
            return result;
    }

    /**
     * Applies skill damage to the target, launching the skill damage event
     *
     * @param target target to receive the damage
     * @param damage amount of damage to deal
     * @param source source of the damage (skill caster)
     */
    public void damage(LivingEntity target, double damage, LivingEntity source) {
        damage(target, damage, source, "default");
    }

    public void damage(LivingEntity target, double damage, LivingEntity source, String classification) {
        damageAndBack(target, damage, source, classification, true, false, null);
    }

    public void damage(LivingEntity target, double damage, LivingEntity source, String classification, boolean knockback, boolean range) {
        damageAndBack(target, damage, source, classification, knockback, range, null);
    }
    public void damageAndBack(LivingEntity target, double damage, LivingEntity source, String classification, boolean knockback, boolean range, Consumer<Double> callback) {
        if (target instanceof TempEntity) {
            return;
        }
        MetaKt.setMeta(target, "SkillAPI-skill", this);
        MetaKt.setMeta(target, "SkillAPI-classification", classification);
        SkillDamageEvent event = new SkillDamageEvent(this, source, target, damage, classification, range);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {

            // 如果是召唤物，
            MythicManager.INSTANCE.trigger(event);
            skillDamage = true;
            int ticks = target.getNoDamageTicks();
            target.setNoDamageTicks(0);
            if (callback != null) {
                callback.accept(event.getDamage());
            }

            if (knockback) {
                target.damage(event.getDamage(), source);
            } else {
                target.damage(event.getDamage());
            }
            //System.out.println("伤害成功造成...");
            target.setNoDamageTicks(ticks);
            MetaKt.removeMeta(target, "SkillAPI-skill");
            MetaKt.removeMeta(target, "SkillAPI-classification");
            skillDamage = false;
            // 如果目标死亡，则触发击杀
            if (source instanceof Player) {
                if (!target.isValid() || target.isDead() || target.getHealth() <= 0.0) {
                    PlayerKillerEntityEvent entityEvent = new PlayerKillerEntityEvent(
                            (Player) source, target, classification, name, TargetHelper.isBehind(source, target)
                    );
                    Bukkit.getPluginManager().callEvent(entityEvent);
                }
            }
        } else {
            MetaKt.removeMeta(target, "SkillAPI-skill");
            MetaKt.removeMeta(target, "SkillAPI-classification");
        }
           // System.out.println("SkillDamageEvent isCancelled");
           // if (event.cancelStack != null) {
             //   event.cancelStack.printStackTrace();
           // }
    }

    /**
     * Applies skill damage to the target, launching the skill damage event
     * and keeping the damage version compatible.
     *
     * @param target target to receive the damage
     * @param damage amount of damage to deal
     * @param source source of the damage (skill caster)
     */
    public void trueDamage(LivingEntity target, double damage, LivingEntity source) {
        trueDamageAndBack(target, damage, source, "", null);
    }

    public void trueDamage(LivingEntity target, double damage, LivingEntity source, String classification) {
        trueDamageAndBack(target, damage, source, classification, null);
    }

    public void trueDamageAndBack(LivingEntity target, double damage, LivingEntity source, String classification, Consumer<Double> callback) {
        if (target instanceof TempEntity) return;

        TrueDamageEvent event = new TrueDamageEvent(this, source, target, damage);

        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled() && event.getDamage() != 0) {
            if (callback != null) {
                callback.accept(event.getDamage());
            }
            target.setHealth(Math.max(Math.min(target.getHealth() - event.getDamage(), target.getMaxHealth()), 0));
            // 如果目标死亡，则触发击杀
            if (source instanceof Player) {
                if (!target.isValid() || target.isDead() || target.getHealth() <= 0.0) {
                    PlayerKillerEntityEvent entityEvent = new PlayerKillerEntityEvent(
                            (Player) source, target, classification, name, TargetHelper.isBehind(source, target)
                    );
                    Bukkit.getPluginManager().callEvent(entityEvent);
                }
            }
        }
    }


    private static boolean skillDamage = false;

    /**
     * Checks whether or not the current damage event is due to
     * skills damaging an entity. This method is used by the API
     * and shouldn't be used by other plugins.
     *
     * @return true if caused by a skill, false otherwise
     */
    public static boolean isSkillDamage() {
        return skillDamage;
    }

    private static final String NAME      = "name";
    private static final String TYPE      = "type";
    private static final String MAX       = "max-level";
    private static final String REQ       = "skill-req";
    private static final String REQLVL    = "skill-req-lvl";
    private static final String MSG       = "msg";
    private static final String PERM      = "needs-permission";
    private static final String DESC      = "desc";
    private static final String ATTR      = "attributes";


    /**
     * Loads skill data from the configuration
     *
     * @param config config to load from
     */
    public void load(DataSection config) {
        name = config.getString(NAME, name);
        type = TextFormatter.colorString(config.getString(TYPE, name));
        maxLevel = config.getInt(MAX, maxLevel);
        skillReq = config.getString(REQ);
        if (skillReq == null || skillReq.length() == 0) skillReq = null;
        skillReqLevel = config.getInt(REQLVL, skillReqLevel);
        message = TextFormatter.colorString(config.getString(MSG, message));
        needsPermission = config.getString(PERM, needsPermission + "").equalsIgnoreCase("true");

        if (config.isList(DESC)) {
            description.clear();
            description.addAll(config.getList(DESC));
        }
        settings.load(config.getSection(ATTR));
    }
}
