package com.sucy.skill;

import com.rit.sucy.config.CommentedConfig;
import com.rit.sucy.config.CommentedLanguageConfig;
import com.rit.sucy.version.VersionManager;
import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.particle.EffectManager;
import com.sucy.skill.api.particle.Particle;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.util.BuffManager;
import com.sucy.skill.api.util.Combat;
import com.sucy.skill.api.util.FlagManager;
import com.sucy.skill.data.PlayerStats;
import com.sucy.skill.data.Settings;
import com.sucy.skill.data.io.IOManager;
import com.sucy.skill.data.io.SQLImpl;
import com.sucy.skill.dynamic.DynamicClass;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.gui.tool.GUITool;
import com.sucy.skill.hook.PlaceholderAPIHook;
import com.sucy.skill.hook.mechanic.MythicListener;
import com.sucy.skill.listener.*;
import com.sucy.skill.task.ManaTask;
import com.sucy.skill.task.MobAttributeTask;
import com.sucy.skill.thread.MainThread;
import com.sucy.skill.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * <p>The com class of the plugin which has the accessor methods into most of the API.</p>
 * <p>You can retrieve a reference to this through Bukkit the same way as any other plugin.</p>
 */
public class SkillAPI extends JavaPlugin {

    private static SkillAPI singleton;
    private final HashMap<String, Skill> skills = new HashMap<>();
    private final HashMap<String, RPGClass> classes = new HashMap<>();
    private final ConcurrentHashMap<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    private final ArrayList<String> groups = new ArrayList<>();
    private final List<SkillAPIListener> listeners = new ArrayList<>();
    private CommentedLanguageConfig language;
    private Settings settings;
    private IOManager ioManager;
    private CmdManager cmd;
    private RegistrationManager registrationManager;
    private AttributeManager attributeManager;
    private MainThread mainThread;
    private BukkitTask manaTask;

    public SkillAPI() {
        singleton = this;
    }

    /**
     * <p>Enables SkillAPI, setting up listeners, managers, and loading data. This
     * should not be called by other plugins.</p>
     */
    @Override
    public void onEnable() {
        singleton = this;
        mainThread = new MainThread();
        Particle.init();
        EffectManager.init();
        ArmorStandManager.init();

        // Load settings
        settings = new Settings(this);
        language = new CommentedLanguageConfig(this, "language");
        language.checkDefaults();
        language.trim();
        language.save();

        registrationManager = new RegistrationManager(this);
        cmd = new CmdManager(this);
        ioManager = new SQLImpl(this);
        PlayerStats.init();
        attributeManager = new AttributeManager(this);
        // Load classes and skills
        registrationManager.initialize();
        // Load group settings after groups are determined
        settings.loadGroupSettings();

        // Set up listeners
        listen(new BuffListener(), true);
        listen(new MainListener(), true);
        listen(new MechanicListener(), true);
        listen(new StatusListener(), true);
        listen(new ToolListener(), true);
        listen(new KillListener(), true);
        listen(new AddonListener(), true);
        listen(new ItemListener(), settings.isCheckLore());
        listen(new AttributeListener(), true);
        listen(new DeathListener(), !VersionManager.isVersionAtLeast(11000));
        listen(new LingeringPotionListener(), true);
        listen(new ExperienceListener(), settings.yieldsEnabled());
        // MM 事件
        listen(new MobListener(), settings.isAttributeMobEnabled());
        listen(new MythicListener(), true);
        MainThread.register(new MobAttributeTask());
        // Set up tasks
        if (settings.isManaEnabled()) {
            manaTask = Bukkit.getScheduler().runTaskTimer(this, new ManaTask(),
                        SkillAPI.getSettings().getGainFreq(),
                        SkillAPI.getSettings().getGainFreq()
                );
        }
        GUITool.init();
        // 加载数据
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player, it -> {
                it.init(player);
                it.autoLevel();
                MainListener.callJoinHandlers(player);
            });
        }

        for (SkillAPIListener listener : listeners) {
            listener.init();
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIHook.init();
            Bukkit.getLogger().info("ProSkillAPI hook into PlaceholderAPI: " + ChatColor.GREEN + "success.");
        }
    }

    private void listen(SkillAPIListener listener, boolean enabled) {
        if (enabled) {
            Bukkit.getPluginManager().registerEvents(listener, this);
            listeners.add(listener);
        }
    }


    @Override
    public void onDisable() {
        // 禁用前清除技能栏并停止被动
        for (Player player : Bukkit.getOnlinePlayers()) {
            unloadPlayerData(player, false, true);
        }
        playerDataMap.clear();
        
        GUITool.cleanUp();
        EffectManager.cleanUp();
        ArmorStandManager.cleanUp();
        mainThread.disable();
        mainThread = null;
        if (manaTask != null) {
            manaTask.cancel();
            manaTask = null;
        }
        for (SkillAPIListener listener : listeners) {
            listener.cleanup();
        }
        listeners.clear();
        // 关闭连接池
        if (ioManager instanceof SQLImpl) {
            ((SQLImpl) ioManager).close();
        }
        skills.clear();
        classes.clear();
        HandlerList.unregisterAll(this);
        cmd.clear();
        singleton = null;
    }


    public static SkillAPI singleton() {
        return singleton;
    }

    public static IOManager getIoManager() {
        return singleton.ioManager;
    }


    public static Settings getSettings() {
        return singleton.settings;
    }

    public static CommentedLanguageConfig getLanguage() {
        return singleton().language;
    }


    /**
     * Retrieves the attribute manager for SkillAPI
     
     * @return attribute manager
     */
    public static AttributeManager getAttributeManager() {
        return singleton().attributeManager;
    }

    /**
     * Retrieves a skill by name. If no skill is found with the name, null is
     * returned instead.
     *
     * @param name name of the skill
     *
     * @return skill with the name or null if not found
     */
    public static Skill getSkill(String name) {
        if (name == null) { return null; }
        return singleton().skills.get(name.toLowerCase());
    }

    /**
     * Retrieves the registered skill data for SkillAPI. It is recommended that you
     * don't edit this map. Instead, use "addSkill" and "addSkills" instead.
     *
     * @return the map of registered skills
     */
    public static HashMap<String, Skill> getSkills() {
        return singleton().skills;
    }

    /**
     * Checks whether or not a skill is registered.
     *
     * @param name name of the skill
     *
     * @return true if registered, false otherwise
     */
    public static boolean isSkillRegistered(String name) {
        return getSkill(name) != null;
    }


    /**
     * Checks whether or not a skill is registered
     *
     * @param skill the skill to check
     *
     * @return true if registered, false otherwise
     */
    public static boolean isSkillRegistered(Skill skill) {
        return isSkillRegistered(skill.getName());
    }

    /**
     * Retrieves a class by name. If no skill is found with the name, null is
     * returned instead.
     *
     * @param name name of the class
     *
     * @return class with the name or null if not found
     */
    public static RPGClass getClass(String name) {
        if (name == null) { return null; }
        return singleton().classes.get(name.toLowerCase());
    }

    /**
     * Retrieves the registered class data for SkillAPI. It is recommended that you
     * don't edit this map. Instead, use "addClass" and "addClasses" instead.
     *
     * @return the map of registered skills
     */
    public static HashMap<String, RPGClass> getClasses() {
        return singleton().classes;
    }

    /**
     * Retrieves a list of base classes that don't profess from another class
     *
     * @return the list of base classes
     */
    public static ArrayList<RPGClass> getBaseClasses(String group) {
        ArrayList<RPGClass> list = new ArrayList<>();
        for (RPGClass c : singleton.classes.values()) {
            if (!c.hasParent() && c.getGroup().equals(group)) { list.add(c); }
        }
        return list;
    }

    /**
     * Checks whether or not a class is registered.
     *
     * @param name name of the class
     *
     * @return true if registered, false otherwise
     */
    public static boolean isClassRegistered(String name) {
        return getClass(name) != null;
    }

    @Nullable
    public static PlayerData getPlayerData(OfflinePlayer player) {
        if (player == null || !player.isOnline()) {
            return null;
        }
        return singleton.playerDataMap.get(player.getUniqueId());
    }

    @Nullable
    public static PlayerData getPlayerData(UUID uuid) {
        return singleton.playerDataMap.get(uuid);
    }

    public static void loadPlayerData(Player player, Consumer<PlayerData> func) {
        PlayerData data = singleton.ioManager.loadData(player);
        System.out.println("加载数据中...");
        if (data != null) {
            singleton.playerDataMap.remove(player.getUniqueId());
            singleton.playerDataMap.put(player.getUniqueId(), data);
            func.accept(data);
        }
    }

    public static void asyncLoadPlayerData(OfflinePlayer player, Consumer<PlayerData> func, int delay) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(singleton, () -> {
            PlayerData data = singleton.ioManager.loadData(player);
            if (data != null) {
                singleton.playerDataMap.remove(player.getUniqueId());
                singleton.playerDataMap.put(player.getUniqueId(), data);
                Bukkit.getScheduler().runTask(singleton, () -> func.accept(data));
            }
        }, delay);
    }

    public static void unloadPlayerData(final Player player, final boolean async, final Boolean clear) {
        if (singleton == null || player == null) return;
        PlayerData playerData;
        if (clear) {
            playerData = singleton.playerDataMap.remove(player.getUniqueId());
        } else {
            playerData = singleton.playerDataMap.get(player.getUniqueId());
        }
        if (playerData != null) {
            if (async) {
                CompletableFuture.runAsync(() -> singleton.ioManager.saveData(playerData));
            } else {
                singleton.ioManager.saveData(playerData);
            }
        }
        if (clear && playerData != null) {
            if (SkillAPI.getSettings().isWorldEnabled(player.getWorld())) {
                playerData.record(player);
                playerData.stopPassives(player);
            }
            FlagManager.clearFlags(player);
            BuffManager.clearData(player);
            Combat.clearData(player);
            DynamicSkill.clearCastData(player);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
            player.setWalkSpeed(0.2f);
        }
    }

    public static void asyncUnloadPlayerData(final Player player, final Boolean clear) {
        unloadPlayerData(player, true, clear);
    }

    public static ConcurrentHashMap<UUID, PlayerData> getPlayerDataMap() {
        return singleton().playerDataMap;
    }


    public static List<String> getGroups() {
        return singleton().groups;
    }

    public void addDynamicSkill(DynamicSkill skill) {
        if (registrationManager.isAddingDynamicSkills()) { skills.put(skill.getName().toLowerCase(), skill); } else {
            throw new IllegalStateException("Cannot add dynamic skills from outside SkillAPI");
        }
    }

    public void addDynamicClass(DynamicClass rpgClass) {
        String key;
        if (rpgClass != null && !classes.containsKey(key = rpgClass.getName().toLowerCase())) {
            classes.put(key, rpgClass);
            if (!groups.contains(rpgClass.getGroup())) { groups.add(rpgClass.getGroup()); }
        }
    }


    public static BukkitTask schedule(BukkitRunnable runnable, int delay) {
        return runnable.runTaskLater(singleton(), delay);
    }

    /**
     * Schedules a delayed task
     *
     * @param runnable the task to schedule
     * @param delay    the delay in ticks
     */
    public static BukkitTask schedule(Runnable runnable, int delay) {
        return Bukkit.getScheduler().runTaskLater(singleton, runnable, delay);
    }

    /**
     * Schedules a repeating task
     *
     * @param runnable the task to schedule
     * @param delay    the delay in ticks before the first tick
     * @param period   how often to run in ticks
     */
    public static BukkitTask schedule(BukkitRunnable runnable, int delay, int period) {
        return runnable.runTaskTimer(singleton(), delay, period);
    }

    /**
     * Sets a value to an entity's metadata
     *
     * @param target entity to set to
     * @param key    key to store under
     * @param value  value to store
     */
    public static void setMeta(Metadatable target, String key, Object value) {
        target.setMetadata(key, new FixedMetadataValue(singleton(), value));
    }

    /**
     * Retrieves metadata from an entity
     *
     * @param target entity to retrieve from
     * @param key    key the value was stored under
     *
     * @return the stored value
     */
    public static Object getMeta(Metadatable target, String key) {
        List<MetadataValue> meta = target.getMetadata(key);
        return meta == null || meta.size() == 0 ? null : meta.get(0).value();
    }

    /**
     * Retrieves metadata from an entity
     *
     * @param target entity to retrieve from
     * @param key    key the value was stored under
     *
     * @return the stored value
     */
    public static int getMetaInt(Metadatable target, String key) {
        return target.getMetadata(key).get(0).asInt();
    }

    /**
     * Retrieves metadata from an entity
     *
     * @param target entity to retrieve from
     * @param key    key the value was stored under
     *
     * @return the stored value
     */
    public static double getMetaDouble(Metadatable target, String key) {
        return target.getMetadata(key).get(0).asDouble();
    }

    /**
     * Removes metadata from an entity
     *
     * @param target entity to remove from
     * @param key    key metadata was stored under
     */
    public static void removeMeta(Metadatable target, String key) {
        target.removeMetadata(key, singleton());
    }

    /**
     * Grabs a config for SkillAPI
     *
     * @param name config file name
     *
     * @return config data
     */
    public static CommentedConfig getConfig(String name) {
        return new CommentedConfig(singleton, name);
    }

    /**
     * Reloads the plugin
     */
    public static void reload() {
        SkillAPI inst = singleton();
        inst.onDisable();
        inst.onEnable();
    }
}
