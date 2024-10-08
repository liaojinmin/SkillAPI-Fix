package com.sucy.skill.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rit.sucy.config.CommentedConfig;
import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.config.parse.NumberParser;
import com.rit.sucy.text.TextFormatter;
import com.rit.sucy.version.VersionManager;
import com.sucy.party.Parties;
import com.sucy.party.Party;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.CombatProtection;
import com.sucy.skill.api.DefaultCombatProtection;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.data.formula.Formula;
import com.sucy.skill.data.formula.value.CustomValue;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.log.Logger;
import me.geek.team.common.TeamHandler;
import me.geek.team.common.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * <p>The management class for SkillAPI's config.yml settings.</p>
 */
public class Settings {

    private final HashMap<String, GroupSettings> groups = new HashMap<>();

    private final SkillAPI    plugin;
    private final DataSection config;

    /**
     * <p>Initializes a new settings manager.</p>
     * <p>This is already set up by SkillAPI and shouldn't be
     * instantiated elsewhere. Instead, get it from SkillAPI
     * using the SkillAPI.getSettings() method.</p>
     *
     * @param plugin SkillAPI plugin reference
     */
    public Settings(SkillAPI plugin) {
        this.plugin = plugin;
        CommentedConfig file = new CommentedConfig(plugin, "config");
        file.checkDefaults();
        file.save();
        config = file.getConfig();
        reload();
    }

    /**
     * <p>Reloads the settings from SkillAPI's config.yml file.</p>
     * <p>This will fill in any missing values with default values
     * and trim any values that aren't supposed to be there.</p>
     */
    public void reload() {
        loadExperienceSettings();
        loadAccountSettings();
        loadClassSettings();
        loadManaSettings();
        loadSkillSettings();
        loadItemSettings();
        loadGUISettings();
        loadExpSettings();
        loadLoggingSettings();
        loadWorldSettings();
        loadSaveSettings();
        loadTargetingSettings();
        loadWorldGuardSettings();
    }


    ///////////////////////////////////////////////////////
    //                                                   //
    //               Experience Settings                 //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String DEFAULT_YIELD = "default";

    private Map<String, Map<String, Double>> breakYields;
    private Map<String, Map<String, Double>> placeYields;
    private Map<String, Map<String, Double>> craftYields;

    private boolean trackBreak;
    private boolean yieldsEnabled;

    public void loadExperienceSettings() {
        CommentedConfig file = new CommentedConfig(plugin, "exp");
        file.checkDefaults();
        file.save();
        DataSection config = file.getConfig();

        DataSection breakData = config.getSection("break");
        yieldsEnabled = config.getBoolean("enabled", false);
        trackBreak = breakData.getBoolean("allow-replace", true);
        breakYields = loadYields(breakData.getSection("types"));
        placeYields = loadYields(config.getSection("place"));
        craftYields = loadYields(config.getSection("craft"));
    }

    private Map<String, Map<String, Double>> loadYields(DataSection config) {
        Map<String, Map<String, Double>> yields = new HashMap<>();
        for (String className : config.keys()) {
            HashMap<String, Double> map = new HashMap<>();
            DataSection classYields = config.getSection(className);
            for (String type : classYields.keys()) {
                map.put(type.toUpperCase().replace(" ", "_"), classYields.getDouble(type));
            }
            yields.put(className, map);
        }
        return yields;
    }

    public boolean trackBreaks() {
        return trackBreak;
    }

    public boolean yieldsEnabled() {
        return yieldsEnabled;
    }

    public double getBreakYield(PlayerClass playerClass, Material mat) {
        return getYield(breakYields, playerClass, mat.name());
    }

    public double getPlaceYield(PlayerClass playerClass, Material mat) {
        return getYield(placeYields, playerClass, mat.name());
    }

    public double getCraftYield(PlayerClass playerClass, Material mat) {
        return getYield(craftYields, playerClass, mat.name());
    }

    private double getYield(Map<String, Map<String, Double>> yields, PlayerClass playerClass, String key) {
        double yield = getYield(yields.get(playerClass.getData().getName()), key);
        return yield > 0 ? yield : getYield(yields.get(DEFAULT_YIELD), key);
    }

    private double getYield(Map<String, Double> yields, String key) {
        return yields == null ? 0 : (yields.containsKey(key) ? yields.get(key) : 0);
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                  Group Settings                   //
    //                                                   //
    ///////////////////////////////////////////////////////

    public void loadGroupSettings() {
        CommentedConfig file = new CommentedConfig(plugin, "groups");
        DataSection config = file.getConfig();
        groups.clear();

        for (String key : config.keys()) {
            groups.put(key.toLowerCase(), new GroupSettings(config.getSection(key)));
        }
        for (String group : SkillAPI.getGroups()) {
            if (!groups.containsKey(group.toLowerCase())) {
                GroupSettings settings = new GroupSettings();
                groups.put(group.toLowerCase(), settings);
                settings.save(config.createSection(group.toLowerCase()));
            }
            config.setComments(group.toLowerCase(), ImmutableList.of(
                    "",
                    " Settings for classes with the group " + group,
                    " If new classes are loaded with different groups,",
                    " the new groups will show up in this file after the first load."));
        }

        file.save();
    }

    /**
     * Retrieves the settings for a class group
     *
     * @param group name of the group to retrieve the settings for
     *
     * @return settings for the class group
     */
    public GroupSettings getGroupSettings(String group) {
        if (!groups.containsKey(group.toLowerCase())) {
            return new GroupSettings();
        }
        return groups.get(group.toLowerCase());
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                 Account Settings                  //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String ACCOUNT_BASE = "Accounts.";
    private static final String ACCOUNT_MAIN = ACCOUNT_BASE + "com-class-group";

    private String  mainGroup;

    /**
     * Retrieves the com class group for displaying prefixes
     * or showing account information
     *
     * @return com class group
     */
    public String getMainGroup() {
        return mainGroup;
    }

    private void loadAccountSettings() {
        mainGroup = config.getString(ACCOUNT_MAIN);
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                 Targeting Settings                //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String TARGET_BASE    = "Targeting.";
    private static final String TARGET_MONSTER = TARGET_BASE + "monsters-enemy";
    private static final String TARGET_PASSIVE = TARGET_BASE + "passive-ally";
    private static final String TARGET_PLAYER  = TARGET_BASE + "player-ally";
    private static final String TARGET_PARTIES = TARGET_BASE + "parties-ally";
    private static final String TARGET_NPC     = TARGET_BASE + "affect-npcs";
    private static final String TARGET_STANDS  = TARGET_BASE + "affect-armor-stands";

    private final ArrayList<String> monsterWorlds = new ArrayList<>();
    private final ArrayList<String> passiveWorlds = new ArrayList<>();
    private final ArrayList<String> playerWorlds  = new ArrayList<>();

    private boolean monsterEnemy;
    private boolean passiveAlly;
    private boolean playerAlly;
    private boolean partiesAlly;
    private boolean affectNpcs;
    private boolean affectArmorStands;

    private CombatProtection combatProtection = new DefaultCombatProtection();

    /**
     * Checks whether or not something can be attacked
     *
     * @param attacker the attacking entity
     * @param target   the target entity
     *
     * @return true if can be attacked, false otherwise
     */
    public boolean canAttack(LivingEntity attacker, LivingEntity target) {
        //召唤物的判断依赖于召唤者
        if (!attacker.getMetadata(AttributeAPI.FX_SKILL_API_MASTER).isEmpty()) {
            UUID masterId = UUID.fromString(attacker.getMetadata(AttributeAPI.FX_SKILL_API_MASTER).get(0).asString());
            Entity master = Bukkit.getEntity(masterId);
            if (master != null && !master.isEmpty()) {
                attacker = (LivingEntity) master;
            }
        }

        if (attacker instanceof Player) {
            final Player player = (Player) attacker;
            if (!player.getWorld().getPVP() && target instanceof Player) {
                return false;
            }
            if (target instanceof Animals && !(target instanceof Tameable)) {
                if (passiveAlly || passiveWorlds.contains(attacker.getWorld().getName())) { return false; }
            } else if (target instanceof Monster) {
                if (monsterEnemy || monsterWorlds.contains(attacker.getWorld().getName())) { return true; }
            } else if (target instanceof Player) {

                if (playerAlly || playerWorlds.contains(attacker.getWorld().getName())) {
                    return false;
                }

                if (partiesAlly) {
                    final Parties parties = Parties.getPlugin(Parties.class);
                    final Party p1 = parties.getJoinedParty(player);
                    final Party p2 = parties.getJoinedParty((Player) target);
                    return p1 == null || p1 != p2;
                }
                try {
                    // 检查 GeekTeamPlus 队伍
                    TeamHandler teamHandler = TeamManager.INSTANCE.getTeamByPlayerID(player.getUniqueId());
                    if (teamHandler != null) {
                        if (teamHandler.getPart().containPlayer(target.getUniqueId())) {
                            return false;
                        }
                    }
                } catch (NoClassDefFoundError ignored) {}

                // 世界PVP设置，最后
                if (!player.getWorld().getPVP()) {
                    return false;
                }
                return combatProtection.canAttack(player, (Player) target);
            }

            return combatProtection.canAttack(player, target);
        } else if (attacker instanceof Tameable) {
            Tameable tameable = (Tameable) attacker;
            if (tameable.isTamed() && (tameable.getOwner() instanceof LivingEntity)) {
                return (tameable.getOwner() != target)
                        && canAttack((LivingEntity) tameable.getOwner(), target);
            }
        } else { return !(target instanceof Monster); }

        return combatProtection.canAttack(attacker, target);
    }

    /**
     * Checks whether or not a target is a valid target.
     *
     * @param target target to check
     * @return true if a valid target, false otherwise
     */
    public boolean isValidTarget(final LivingEntity target) {
        return (!target.hasMetadata("NPC") || affectNpcs)
                && (!target.getType().name().equals("ARMOR_STAND") || affectArmorStands);
    }

    /**
     * Swaps out the default combat protection for a custom one
     *
     * @param combatProtection combat protection to use
     */
    public void setCombatProtection(final CombatProtection combatProtection) {
        this.combatProtection = combatProtection;
    }

    private void loadTargetingSettings() {
        if (config.isList(TARGET_MONSTER)) {
            monsterWorlds.addAll(config.getList(TARGET_MONSTER));
            monsterEnemy = false;
        } else { monsterEnemy = config.getBoolean(TARGET_MONSTER); }

        if (config.isList(TARGET_PASSIVE)) {
            passiveWorlds.addAll(config.getList(TARGET_PASSIVE));
            passiveAlly = false;
        } else { passiveAlly = config.getBoolean(TARGET_PASSIVE); }

        if (config.isList(TARGET_PLAYER)) {
            playerWorlds.addAll(config.getList(TARGET_PLAYER));
            playerAlly = false;
        } else { playerAlly = config.getBoolean(TARGET_PLAYER); }

        partiesAlly = config.getBoolean(TARGET_PARTIES);
        affectArmorStands = config.getBoolean(TARGET_STANDS);
        affectNpcs = config.getBoolean(TARGET_NPC);
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                  Saving Settings                  //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String SAVE_BASE = "Saving.";
    private static final String SAVE_AUTO = SAVE_BASE + "auto-save";
    private static final String SAVE_MINS = SAVE_BASE + "minutes";
    private static final String SAVE_SQL  = SAVE_BASE + "sql-database";
    private static final String SAVE_SQLD = SAVE_BASE + "sql-details";

    private boolean useSql;
    private int     minutes;
    private int     sqlDelay;

    private String sqlHost;
    private String sqlPort;
    private String sqlDatabase;
    private String sqlUser;
    private String sqlPass;

    /**
     * Retrieves the amount of ticks in between each auto save
     *
     * @return frequency of saves
     */
    public int getSaveFreq() {
        return minutes * 60 * 20;
    }

    /**
     * Checks whether or not the plugin is using SQL Database saving
     *
     * @return true if enabled, false otherwise
     */
    public boolean isUseSql() {
        return useSql;
    }

    /**
     * Retrieves the host IP for the database
     *
     * @return host IP for SQL database
     */
    public String getSQLHost() {
        return sqlHost;
    }

    /**
     * Retrieves the host port for the database
     *
     * @return host port for SQL database
     */
    public String getSQLPort() {
        return sqlPort;
    }

    /**
     * Retrieves the name of the SQL database
     *
     * @return SQL database name
     */
    public String getSQLDatabase() {
        return sqlDatabase;
    }

    /**
     * Retrieves the username for the database credentials
     *
     * @return SQL database username
     */
    public String getSQLUser() {
        return sqlUser;
    }

    /**
     * Retrieves the password for the database credentials
     *
     * @return SQL database password
     */
    public String getSQLPass() {
        return sqlPass;
    }

    /**
     * @return time in milliseconds to wait before loading SQL data
     */
    public int getSqlDelay() {
        return sqlDelay;
    }

    private void loadSaveSettings() {
        minutes = config.getInt(SAVE_MINS);
        useSql = config.getBoolean(SAVE_SQL);

        DataSection details = config.getSection(SAVE_SQLD);
        sqlDelay = details.getInt("delay");

        if (useSql) {
            sqlHost = details.getString("host");
            sqlPort = details.getString("port");
            sqlDatabase = details.getString("database");
            sqlUser = details.getString("username");
            sqlPass = details.getString("password");
        }
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                  Class Settings                   //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String CLASS_BASE   = "Classes.";
    private static final String CLASS_MODIFY = CLASS_BASE + "modify-health";
    private static final String CLASS_HP     = CLASS_BASE + "classless-hp";
    private static final String CLASS_SHOW   = CLASS_BASE + "show-auto-skills";
    private static final String CLASS_REFUND = CLASS_BASE + "attributes-downgrade";
    private static final String CLASS_LEVEL  = CLASS_BASE + "level-up-skill";

    private boolean modifyHealth;
    private int     defaultHealth;
    private boolean showAutoSkills;
    private boolean attributesDowngrade;
    private String  levelUpSkill;

    /**
     * Checks whether or not SkillAPI should modify the max health of players
     *
     * @return true if enabled, false otherwise
     */
    public boolean isModifyHealth() {
        return modifyHealth;
    }

    /**
     * <p>Retrieves the default health for players that do not have a class.</p>
     *
     * @return default health for classless players
     */
    public int getDefaultHealth() {
        return defaultHealth;
    }

    /**
     * Checks whether or not auto-leveled skills are to be shown.
     *
     * @return true if shown, false otherwise
     */
    public boolean isShowingAutoSkills() {
        return showAutoSkills;
    }



    /**
     * Checks whether or not attribute points can be refunded
     *
     * @return true if can refund, false otherwise
     */
    public boolean isAttributesDowngrade() {
        return attributesDowngrade;
    }

    /**
     * Checks whether or not the plugin has a valid skill for
     * level up effects loaded.
     *
     * @return true if one is available, false otherwise
     */
    public boolean hasLevelUpEffect() {
        return getLevelUpSkill() != null;
    }

    /**
     * Retrieves the skill used for level up effects
     *
     * @return skill for level up effects
     */
    public DynamicSkill getLevelUpSkill() {
        Skill skill = SkillAPI.getSkill(levelUpSkill);
        return (skill instanceof DynamicSkill) ? (DynamicSkill) skill : null;
    }

    private void loadClassSettings() {
        modifyHealth = config.getBoolean(CLASS_MODIFY);
        defaultHealth = config.getInt(CLASS_HP);
        showAutoSkills = config.getBoolean(CLASS_SHOW);
        attributesDowngrade = config.getBoolean(CLASS_REFUND);
        levelUpSkill = config.getString(CLASS_LEVEL);
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                   Mana Settings                   //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String MANA_BASE    = "Mana.";
    private static final String MANA_ENABLED = MANA_BASE + "enabled";
    private static final String MANA_FREQ    = MANA_BASE + "freq";

    private boolean manaEnabled;
    private int     gainFreq;

    /**
     * Checks whether or not mana is enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isManaEnabled() {
        return manaEnabled;
    }

    /**
     * Retrieves the frequency of mana gain
     *
     * @return the frequency of mana gain
     */
    public int getGainFreq() {
        return gainFreq;
    }

    private void loadManaSettings() {
        manaEnabled = config.getBoolean(MANA_ENABLED);
        gainFreq = (int) (config.getDouble(MANA_FREQ) * 20);
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                  Skill Settings                   //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String SKILL_BASE      = "Skills.";
    private static final String SKILL_DOWNGRADE = SKILL_BASE + "allow-downgrade";
    private static final String SKILL_MESSAGE   = SKILL_BASE + "show-messages";
    private static final String SKILL_RADIUS    = SKILL_BASE + "message-radius";
    private static final String SKILL_BLOCKS    = SKILL_BASE + "block-filter";
    private static final String SKILL_KNOCKBACK = SKILL_BASE + "knockback-no-damage";

    private static final String SKILL_MODEL_DATA = SKILL_BASE + "use-custommodeldata";

    private ArrayList<Material> filteredBlocks;

    private boolean allowDowngrade;
    private boolean showSkillMessages;
    private boolean knockback;
    private int     messageRadius;

    private boolean skillModelData;

    /**
     * Checks whether or not downgrades are allowed
     *
     * @return true if allowed, false otherwise
     */
    public boolean isAllowDowngrade() {
        return allowDowngrade;
    }

    /**
     * Checks whether or not skill messages are enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isShowSkillMessages() {
        return showSkillMessages;
    }

    /**
     * @return whether or not knockback should be applied when dealing 0 damage
     */
    public boolean isKnockback() {
        return knockback;
    }

    /**
     * Gets the radius in which skill messages are sent out
     *
     * @return skill message radius
     */
    public int getMessageRadius() {
        return messageRadius;
    }

    /**
     * Return whether skill mechanics should use 'data' values as CustomModelData
     *
     * @return skill mechanics use CustomModelData
     */
    public boolean useSkillModelData() {
        return skillModelData;
    }

    /**
     * Retrieves the list of filtered blocks
     *
     * @return list of blocks
     */
    public List<Material> getFilteredBlocks() {
        return filteredBlocks;
    }

    private void loadSkillSettings() {
        allowDowngrade = config.getBoolean(SKILL_DOWNGRADE);
        showSkillMessages = config.getBoolean(SKILL_MESSAGE);
        messageRadius = config.getInt(SKILL_RADIUS);
        knockback = config.getBoolean(SKILL_KNOCKBACK);
        skillModelData = config.getBoolean(SKILL_MODEL_DATA);
        if (skillModelData) {
            try {
                ItemMeta.class.getMethod("hasCustomModelData");
            } catch (NoSuchMethodException e) {
                skillModelData = false;
                Logger.log("CustomModelData not supported below 1.14+. Using item durability/data instead.");
            }
        }

        filteredBlocks = new ArrayList<>();
        List<String> list = config.getList(SKILL_BLOCKS);
        for (String item : list) {
            item = item.toUpperCase().replace(' ', '_');
            if (item.endsWith("*")) {
                item = item.substring(0, item.length() - 1);
                for (Material mat : Material.values()) {
                    if (mat.name().contains(item)) {
                        filteredBlocks.add(mat);
                    }
                }
            } else {
                try {
                    Material mat = Material.valueOf(item);
                    filteredBlocks.add(mat);
                } catch (Exception ex) {
                    Logger.invalid("Invalid block type \"" + item + "\"");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                   Item Settings                   //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String ITEM_BASE    = "Items.";
    private static final String ITEM_LORE    = ITEM_BASE + "lore-requirements";
    private static final String ITEM_DROP    = ITEM_BASE + "drop-weapon";
    private static final String ITEM_SKILLS  = ITEM_BASE + "skill-requirements";
    private static final String ITEM_ATTRIBS = ITEM_BASE + "lore-attributes";
    private static final String ITEM_CLASS   = ITEM_BASE + "lore-class-text";
    private static final String ITEM_SKILL   = ITEM_BASE + "lore-skill-text";
    private static final String ITEM_LEVEL   = ITEM_BASE + "lore-level-text";
    private static final String ITEM_EXCLUDE = ITEM_BASE + "lore-exclude-text";
    private static final String ITEM_ATTR    = ITEM_BASE + "lore-attribute-text";
    private static final String ITEM_STATS   = ITEM_BASE + "attribute-text";
    private static final String ITEM_SLOTS   = ITEM_BASE + "slots";

    private boolean checkLore;
    private boolean checkAttribs;
    private boolean checkSkills;
    private boolean dropWeapon;
    private String  loreClassText;
    private String  loreLevelText;
    private String  loreExcludeText;
    private int[]   slots;

    private String skillPre, skillPost;
    private String attrReqPre, attrReqPost;
    private String attrPre, attrPost;

    /**
     * Checks whether or not lore requirements are enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isCheckLore() {
        return checkLore;
    }

    /**
     * @return true if should check for skill requirements
     */
    public boolean isCheckSkillLore() {
        return checkSkills;
    }

    /**
     * @return true if should check for attribute bonuses
     */
    public boolean isCheckAttributes() {
        return checkAttribs;
    }

    /**
     * @return checks if weapons are dropped when hovered
     */
    public boolean isDropWeapon() {
        return dropWeapon;
    }

    /**
     * @return lore for skill requirements
     */
    public String getSkillText(String skill) {
        return skillPre + skill + skillPost;
    }

    /**
     * Retrieves the text used for class requirements on items
     *
     * @return lore text for class requirements
     */
    public String getLoreClassText() {
        return loreClassText;
    }

    /**
     * Retrieves the text used for level requirements on items
     *
     * @return lore text for level requirements
     */
    public String getLoreLevelText() {
        return loreLevelText;
    }

    /**
     * Retrieves the text used for excluded classes on items
     *
     * @return lore text for excluded classes
     */
    public String getLoreExcludeText() {
        return loreExcludeText;
    }

    /**
     * Retrieves the text used for attribute requirements on items
     *
     * @return lore text for attributes
     */
    public String getAttrReqText(String attr) {
        return attrReqPre + attr + attrReqPost;
    }

    /**
     * @return lore text for giving attributes
     */
    public String getAttrGiveText(String attr) {
        return attrPre + attr + attrPost;
    }

    /**
     * @return slots checked for requirements and attributes
     */
    public int[] getSlots() {
        return slots;
    }

    private void loadItemSettings() {
        checkLore = config.getBoolean(ITEM_LORE);
        dropWeapon = config.getBoolean(ITEM_DROP);
        checkSkills = config.getBoolean(ITEM_SKILLS);
        checkAttribs = config.getBoolean(ITEM_ATTRIBS);
        loreClassText = config.getString(ITEM_CLASS).toLowerCase();
        loreLevelText = config.getString(ITEM_LEVEL).toLowerCase();
        loreExcludeText = config.getString(ITEM_EXCLUDE).toLowerCase();

        String temp = config.getString(ITEM_SKILL).toLowerCase();
        int index = temp.indexOf('{');
        skillPre = temp.substring(0, index);
        skillPost = temp.substring(index + 7);

        temp = config.getString(ITEM_ATTR).toLowerCase();
        index = temp.indexOf('{');
        attrReqPre = temp.substring(0, index);
        attrReqPost = temp.substring(index + 6);

        temp = config.getString(ITEM_STATS).toLowerCase();
        index = temp.indexOf('{');
        attrPre = temp.substring(0, index);
        attrPost = temp.substring(index + 6);

        List<String> slotList = config.getList(ITEM_SLOTS);
        if (!VersionManager.isVersionAtLeast(VersionManager.V1_9_0)) { slotList.remove("40"); }
        slots = new int[slotList.size()];
        for (int i = 0; i < slots.length; i++) { slots[i] = NumberParser.parseInt(slotList.get(i)); }
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                   GUI Settings                    //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String
            GUI_BASE   = "GUI.",
            GUI_OLD    = GUI_BASE + "old-health-bar",
            GUI_FORCE  = GUI_BASE + "force-scaling",
            GUI_LVLBAR = GUI_BASE + "level-bar",
            GUI_FOOD   = GUI_BASE + "food-bar",
            GUI_ACTION = GUI_BASE + "use-action-bar",
            GUI_TEXT   = GUI_BASE + "action-bar-text",
            GUI_NAME   = GUI_BASE + "show-class-name",
            GUI_LEVEL  = GUI_BASE + "show-class-level",
            GUI_BINDS  = GUI_BASE + "show-binds",
            GUI_BIND_TEXT = GUI_BASE + "show-bind-text",
            GUI_LVLTXT = GUI_BASE + "class-level-text",
            GUI_TITLE  = GUI_BASE + "title-enabled",
            GUI_DUR    = GUI_BASE + "title-duration",
            GUI_FADEI  = GUI_BASE + "title-fade-in",
            GUI_FADEO  = GUI_BASE + "title-fade-out",
            GUI_LIST   = GUI_BASE + "title-messages";

    private List<String> titleMessages;

    private boolean oldHealth;
    private boolean forceScaling;
    private String  levelBar;
    private String  foodBar;
    private String  levelText;
    private boolean useActionBar;
    private String  actionText;
    private boolean showClassName;
    private boolean showClassLevel;
    private boolean showBinds;
    private String  bindText;
    private boolean useTitle;
    private int     titleDuration;
    private int     titleFadeIn;
    private int     titleFadeOut;

    /**
     * Checks whether or not old health bars (fixed 10 hearts) are enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isOldHealth() {
        return oldHealth;
    }

    /**
     * @return true if forces the SkillAPI health scaling, false otherwise
     */
    public boolean isForceScaling() {
        return forceScaling;
    }

    /**
     * Gets the setting for using the level bar
     *
     * @return level bar setting
     */
    public String getLevelBar() {
        return levelBar;
    }

    /**
     * Gets the setting for using the food bar
     *
     * @return food bar setting
     */
    public String getFoodBar() {
        return foodBar;
    }

    /**
     * Checks whether or not the action bar is being used
     *
     * @return true if used, false otherwise
     */
    public boolean isUseActionBar() {
        return useActionBar;
    }

    /**
     * Gets the text to display on the action bar
     *
     * @return action bar text
     */
    public String getActionText() {
        return actionText;
    }


    /**
     * Checks whether or not a player's class name is to be
     * shown next to their name
     *
     * @return true if shown, false otherwise
     */
    public boolean isShowClassName() {
        return showClassName;
    }

    /**
     * Checks whether or not a player's class level is to be
     * shown below their name
     *
     * @return true if shown, false otherwise
     */
    public boolean isShowClassLevel() {
        return showClassLevel;
    }

    public boolean isShowBinds() {
        return showBinds;
    }

    public String getBindText() {
        return bindText;
    }

    /**
     * @return text shown alongside the class level
     */
    public String getLevelText() {
        return levelText;
    }

    /**
     * Check whether or not to use the title display
     * on the given message type
     *
     * @param type type of message to check for
     *
     * @return true if should use title display, false otherwise
     */
    public boolean useTitle(TitleType type) {
        return useTitle && type != null && titleMessages.contains(type.name().toLowerCase());
    }

    /**
     * @return duration of the title display in ticks
     */
    public int getTitleDuration() {
        return titleDuration;
    }

    /**
     * @return fade in time of the title display in ticks
     */
    public int getTitleFadeIn() {
        return titleFadeIn;
    }

    /**
     * @return fade out time of the title display in ticks
     */
    public int getTitleFadeOut() {
        return titleFadeOut;
    }

    private void loadGUISettings() {
        oldHealth = config.getBoolean(GUI_OLD);
        forceScaling = config.getBoolean(GUI_FORCE);
        levelBar = config.getString(GUI_LVLBAR);
        levelText = TextFormatter.colorString(config.getString(GUI_LVLTXT, "Level"));
        foodBar = config.getString(GUI_FOOD);
        useActionBar = config.getBoolean(GUI_ACTION);
        actionText = config.getString(GUI_TEXT);
        showClassName = config.getBoolean(GUI_NAME);
        showClassLevel = config.getBoolean(GUI_LEVEL);
        showBinds = config.getBoolean(GUI_BINDS);
        bindText = config.getString(GUI_BIND_TEXT);
        useTitle = config.getBoolean(GUI_TITLE);
        titleDuration = (int) (20 * config.getFloat(GUI_DUR));
        titleFadeIn = (int) (20 * config.getFloat(GUI_FADEI));
        titleFadeOut = (int) (20 * config.getFloat(GUI_FADEO));
        titleMessages = config.getList(GUI_LIST);
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                   Exp Settings                    //
    //                                                   //
    ///////////////////////////////////////////////////////

    private final HashMap<String, Double> yields = new HashMap<>();

    private ExpFormula expFormula;
    private Formula    expCustom;
    private boolean    useCustomExp;
    private boolean    useOrbs;
    private boolean    blockSpawner;
    private boolean    blockEgg;
    private boolean    blockCreative;
    private boolean    showExpMessages;
    private boolean    showLevelMessages;
    private boolean    showLossMessages;
    private Set<String> expLostBlacklist;

    /**
     * Gets the required amount of experience at a given level
     *
     * @param level level of the class
     *
     * @return required experience to gain a level
     */
    public int getRequiredExp(int level) {
        if (useCustomExp) { return (int) expCustom.compute(level, 0); } else { return expFormula.calculate(level); }
    }

    /**
     * Gets the experience yield of a mob
     *
     * @param mob mob to get the yield of
     *
     * @return experience yield
     */
    public double getYield(String mob) {
        mob = mob.toLowerCase();
        if (!yields.containsKey(mob)) {
            return 0;
        } else {
            return yields.get(mob);
        }
    }

    /**
     * Checks whether or not experience is to be gained through
     * vanilla experience orbs
     *
     * @return true if enabled, false otherwise
     */
    public boolean isUseOrbs() {
        return useOrbs;
    }

    /**
     * Checks whether or not experience from mobs spawned
     * via a mob spawner is to be blocked.
     *
     * @return true if blocked, false otherwise
     */
    public boolean isBlockSpawner() {
        return blockSpawner;
    }

    /**
     * Checks whether or not experience from mobs spawned
     * via eggs are to be blocked
     *
     * @return true if blocked, false otherwise
     */
    public boolean isBlockEgg() {
        return blockEgg;
    }

    /**
     * Checks whether or not players in creative mode
     * are blocked from receiving experience.
     *
     * @return true if blocked, false otherwise
     */
    public boolean isBlockCreative() {
        return blockCreative;
    }

    /**
     * Checks whether or not messages should
     * be displayed when a player gains experience
     *
     * @return true if enabled, false otherwise
     */
    public boolean isShowExpMessages() {
        return showExpMessages;
    }

    /**
     * Checks whether or not messages should be displayed
     * when a player gains a level
     *
     * @return true if enabled, false otherwise
     */
    public boolean isShowLevelMessages() {
        return showLevelMessages;
    }

    /**
     * Checks whether or not messages should be displayed
     * when a player dies and loses experience
     *
     * @return true if enabled, false otherwise
     */
    public boolean isShowLossMessages() {
        return showLossMessages;
    }

    /**
     * @param world world a player died in
     * @return true if the world is blacklisted for losing experience
     */
    public boolean shouldIgnoreExpLoss(final World world) {
        return expLostBlacklist.contains(world.getName());
    }

    private static final String EXP_BASE = "Experience.";

    private void loadExpSettings() {
        this.useOrbs = config.getBoolean(EXP_BASE + "use-exp-orbs");
        this.blockSpawner = config.getBoolean(EXP_BASE + "block-mob-spawner");
        this.blockEgg = config.getBoolean(EXP_BASE + "block-mob-egg");
        this.blockCreative = config.getBoolean(EXP_BASE + "block-creative");
        this.showExpMessages = config.getBoolean(EXP_BASE + "exp-message-enabled");
        this.showLevelMessages = config.getBoolean(EXP_BASE + "level-message-enabled");
        this.showLossMessages = config.getBoolean(EXP_BASE + "lose-exp-message");
        this.expLostBlacklist = new HashSet<>(config.getList(EXP_BASE + "lose-exp-blacklist"));

        DataSection formula = config.getSection(EXP_BASE + "formula");
        int x = formula.getInt("x");
        int y = formula.getInt("y");
        int z = formula.getInt("z");
        expFormula = new ExpFormula(x, y, z);

        expCustom = new Formula(config.getString(EXP_BASE + "custom-formula"), new CustomValue("lvl"));
        useCustomExp = config.getBoolean(EXP_BASE + "use-custom") && expCustom.isValid();

        DataSection yields = config.getSection(EXP_BASE + "yields");
        this.yields.clear();
        for (String key : yields.keys()) {
            this.yields.put(key, yields.getDouble(key));
        }
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                 Logging Settings                  //
    //                                                   //
    ///////////////////////////////////////////////////////

    private void loadLoggingSettings() {
        Logger.loadLevels(config.getSection("Logging"));
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //                  World Settings                   //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String WORLD_BASE   = "Worlds.";
    private static final String WORLD_ENABLE = WORLD_BASE + "enable";
    private static final String WORLD_TYPE   = WORLD_BASE + "use-as-enabling";
    private static final String WORLD_LIST   = WORLD_BASE + "worlds";

    private List<String> worlds;
    private boolean      worldEnabled;
    private boolean      worldEnableList;

    /**
     * Checks whether or not SkillAPI is active in the world
     *
     * @param world world to check
     *
     * @return true if active, false otherwise
     */
    public boolean isWorldEnabled(World world) {
        return isWorldEnabled(world.getName());
    }

    /**
     * Checks whether or not SkillAPI is active in the world with
     * the given name.
     *
     * @param world world name
     *
     * @return true if active, false otherwise
     */
    public boolean isWorldEnabled(String world) {
        return !worldEnabled || (worldEnableList == worlds.contains(world));
    }

    private void loadWorldSettings() {
        worldEnabled = config.getBoolean(WORLD_ENABLE);
        worldEnableList = config.getBoolean(WORLD_TYPE);
        worlds = config.getList(WORLD_LIST);
    }

    ///////////////////////////////////////////////////////
    //                                                   //
    //               WorldGuard Settings                 //
    //                                                   //
    ///////////////////////////////////////////////////////

    private static final String WG_SKILLS = "disable-skills";
    private static final String WG_EXP    = "disable-exp";

    private Set<String> skillDisabledRegions;
    private Set<String> expDisabledRegions;

    public boolean areSkillsDisabledForRegion(final String region) {
        return skillDisabledRegions.contains(region);
    }

    public boolean isExpDisabledForRegion(final String region) {
        return expDisabledRegions.contains(region);
    }

    private void loadWorldGuardSettings() {
        final CommentedConfig config = new CommentedConfig(plugin, "worldGuard");
        config.checkDefaults();
        config.trim();
        config.save();
        final DataSection data = config.getConfig();

        skillDisabledRegions = ImmutableSet.copyOf(data.getList(WG_SKILLS));
        expDisabledRegions = ImmutableSet.copyOf(data.getList(WG_EXP));
    }

    public boolean isAttributeMobEnabled() {
        return true;
    }
}
