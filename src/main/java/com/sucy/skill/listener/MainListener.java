package com.sucy.skill.listener;

import com.rit.sucy.version.VersionManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.event.PhysicalDamageEvent;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.util.BuffManager;
import com.sucy.skill.api.util.Combat;
import com.sucy.skill.api.util.FlagManager;
import com.sucy.skill.data.Permissions;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.mechanic.ImmunityMechanic;
import com.sucy.skill.dynamic.mechanic.ReturnMechanic;
import me.neon.core.event.PlayerKneeEvent;
import me.neon.core.revive.DungeonData;
import me.neon.core.revive.ReviveManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;

/**
 * The com listener for SkillAPI  that handles general mechanics
 * such as loading/clearing data, controlling experience gains, and
 * enabling/disabling passive abilities.
 */
public class MainListener extends SkillAPIListener {
    private static final List<Consumer<Player>> JOIN_HANDLERS = new ArrayList<>();
    private static final List<Consumer<Player>> CLEAR_HANDLERS = new ArrayList<>();

    public static final Map<UUID, BukkitTask> loadingPlayers = new HashMap<>();

    public static void registerJoin(final Consumer<Player> joinHandler) {
        JOIN_HANDLERS.add(joinHandler);
    }

    public static void registerClear(final Consumer<Player> joinHandler) {
        CLEAR_HANDLERS.add(joinHandler);
    }

    public static void callJoinHandlers(Player player) {
        JOIN_HANDLERS.forEach(it -> it.accept(player));
    }

    @Override
    public void cleanup() {
        JOIN_HANDLERS.clear();
    }

    /**
     * 登录时异步加载，
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(event.getUniqueId());
        final int delay = SkillAPI.getSettings().getSqlDelay();

        SkillAPI.asyncLoadPlayerData(player, it -> {
            Player p = Bukkit.getPlayer(event.getUniqueId());
            // 如果玩家仍然不存在，则搁置到加入事件处理
            if (p == null) return;
            // 如果已经初始化则不继续
            if (!it.getInit()) {
                it.setInit(true);
                // 同步任务
                it.init(p);
                it.autoLevel();
                JOIN_HANDLERS.forEach(handler -> handler.accept(p));
            }
        }, delay);
    }

    /** 在加入游戏时，尝试获取数据并初始化，如果登录时的异步线程更早则不需要继续初始化 **/
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = SkillAPI.getPlayerData(player);
        // 如果已经初始化则不继续
        if (playerData != null && !playerData.getInit()) {
            playerData.setInit(true);
            playerData.init(player);
            playerData.autoLevel();
            JOIN_HANDLERS.forEach(handler -> handler.accept(player));
            ReturnMechanic.joinPlayer(player);
        }
    }


    private void init(final Player player) {
        final PlayerData data = SkillAPI.getPlayerData(player.getUniqueId());
        if (data == null) return;
        data.init(player);
        data.autoLevel();
        JOIN_HANDLERS.forEach(handler -> handler.accept(player));
    }

    /**
     * Saves player data when they log out and stops passives
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        //System.out.println("PlayerQuitEvent -> 储存玩家 "+ event.getPlayer().getName() +"数据");
        SkillAPI.asyncUnloadPlayerData(event.getPlayer(), true);
        ReturnMechanic.quitPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        // 切换世界则清理缩减，适配副本
        FlagManager.clearFlagReduces(event.getPlayer());
    }

    /**
     * Stops passives an applies death penalties when a player dies.
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity().hasMetadata("NPC")) return;

        World world = event.getEntity().getWorld();
        DungeonData data = ReviveManager.INSTANCE.getDungeonCache().get(world.getName());
        if (data != null && !ReviveManager.INSTANCE.getDisableDungeon().contains(data.getDungeon().getDungeonName())) {
            // 不进行清理
            return;
        }
        deathPlayer(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKnee(PlayerKneeEvent event) {
        deathPlayer(event.getPlayer());
    }

    private void deathPlayer(Player player) {
        // 此处不删除flag容器，而是清理flag,要保护冷却缩减功能
        FlagManager.clearFlags(player);
        BuffManager.clearData(player);
        DynamicSkill.clearCastData(player);

        PlayerData data = SkillAPI.getPlayerData(player.getUniqueId());
        if (data == null) return;
        if (data.hasClass() && SkillAPI.getSettings().isWorldEnabled(player.getWorld())) {
            data.stopPassives(player);
            if (!SkillAPI.getSettings().shouldIgnoreExpLoss(player.getWorld())) {
                data.loseExp();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(final EntityDeathEvent event) {
        DynamicSkill.clearCastData(event.getEntity());
        FlagManager.removeFlags(event.getEntity());
        BuffManager.clearData(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onUnload(final ChunkUnloadEvent event) {
        for (final Entity entity : event.getChunk().getEntities()) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                final LivingEntity livingEntity = (LivingEntity) entity;
                DynamicSkill.clearCastData(livingEntity);
                FlagManager.removeFlags(livingEntity);
                BuffManager.clearData(livingEntity);
            }
        }
    }

    /**
     * Handles experience when a block is broken
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().hasMetadata("NPC"))
            return;

        Player player = event.getPlayer();
        if (SkillAPI.getSettings().isUseOrbs() && player != null && SkillAPI.getSettings().isWorldEnabled(player.getWorld())) {
            PlayerData data = SkillAPI.getPlayerData(player.getUniqueId());
            if (data != null) {
                data.giveExp(event.getExpToDrop(), ExpSource.BLOCK_BREAK);
            }
        }
    }

    /**
     * Handles experience when ore is smelted in a furnace
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSmelt(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        if (SkillAPI.getSettings().isUseOrbs() && player != null && SkillAPI.getSettings().isWorldEnabled(player.getWorld())) {
            PlayerData data = SkillAPI.getPlayerData(player.getUniqueId());
            if (data != null) {
                data.giveExp(event.getExpToDrop(), ExpSource.SMELT);
            }
        }
    }

    /**
     * Handles experience when a Bottle o' Enchanting breaks
     *
     * @param event event details
     */
    @EventHandler
    public void onExpBottleBreak(ExpBottleEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player) || !SkillAPI.getSettings().isWorldEnabled(((Player) event.getEntity().getShooter()).getWorld()))
            return;
        Player player = (Player) event.getEntity().getShooter();
        if (SkillAPI.getSettings().isUseOrbs()) {
            PlayerData data = SkillAPI.getPlayerData(player.getUniqueId());
            if (data != null) {
                data.giveExp(event.getExperience(), ExpSource.EXP_BOTTLE);
            }
        }

    }

    /**
     * Prevents experience orbs from modifying the level bar when it
     * is used for displaying class level.
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExpChange(PlayerExpChangeEvent event) {
        // Prevent it from changing the level bar when that is being used to display class level
        if (!SkillAPI.getSettings().getLevelBar().equalsIgnoreCase("none")
            && event.getPlayer().hasPermission(Permissions.EXP)
            && SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld())) {
            event.setAmount(0);
        }
    }


    /**
     * Starts passive abilities again after respawning
     *
     * @param event event details
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (event.getPlayer().hasMetadata("NPC"))
            return;

        PlayerData data = SkillAPI.getPlayerData(event.getPlayer().getUniqueId());
        if (data == null) return;
        if (data.hasClass() && SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld())) {
            //data.startPassives(event.getPlayer());
            Bukkit.getScheduler().runTaskLater(SkillAPI.singleton(),
                    () -> data.startPassives(event.getPlayer()), 21);
        }
    }


    public void printEventListeners() {
        HandlerList handlers = EntityDamageEvent.getHandlerList();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();

        for (RegisteredListener listener : listeners) {
            Listener instance = listener.getListener();
            Plugin plugin = listener.getPlugin();
            EventPriority priority = listener.getPriority();

            System.out.println("插件: " + plugin.getName() +
                    " 监听器: " + instance.getClass().getName() +
                    " 优先级: " + priority.name() +
                    " 忽略已取消: " + listener.isIgnoringCancelled());
        }
    }

    /**
     * Damage type immunities
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
      //  System.out.println("EntityDamageEvent "+event.getCause());
        if (event.getEntity() instanceof LivingEntity && FlagManager.hasFlag((LivingEntity) event.getEntity(), "immune:" + event.getCause().name())) {
            double multiplier = SkillAPI.getMetaDouble(event.getEntity(), ImmunityMechanic.META_KEY);
            if (multiplier <= 0.0) {
           //     System.out.println("setCancelled");
                event.setCancelled(true);
                event.setDamage(0.0);
            } else {
                event.setDamage(event.getDamage() * multiplier);
            }
          //  printEventListeners();
        }
    }

    /**
     * Cancels food damaging the player when the bar is being used
     * for GUI features instead of normal hunger.
     *
     * @param event event details
     */
    @EventHandler
    public void onStarve(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.STARVATION
            && !SkillAPI.getSettings().getFoodBar().equalsIgnoreCase("none")) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels saturation heal
     *
     * @param event event details
     */
    @EventHandler
    public void onSaturationHeal(EntityRegainHealthEvent event) {
        String foodBar = SkillAPI.getSettings().getFoodBar().toLowerCase();
        if ((foodBar.equals("mana") || foodBar.equals("exp"))
            && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED)
        {
            event.setCancelled(true);
        }
    }

    /**
     * Launches physical damage events to differentiate skill damage from physical damage
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPhysicalDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;

        if (Skill.isSkillDamage() || event.getCause() == EntityDamageEvent.DamageCause.CUSTOM
            || !(event.getEntity() instanceof LivingEntity)
            || event.getDamage() <= 0.0) {
            return;
        }
      //  System.out.println("EntityDamageByEntityEvent Damage "+event.getDamage() +" start "+event.isCancelled());

        PhysicalDamageEvent e = new PhysicalDamageEvent(ListenerUtil.getDamager(event), (LivingEntity) event.getEntity(), event.getDamage(), event.getDamager() instanceof Projectile);
        Bukkit.getPluginManager().callEvent(e);
        event.setDamage(e.getDamage());
        if (e.isCancelled()) {
         //   event.setCancelled(true);
        }
    }

    /**
     * Handles marking players as in combat
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM
            || !(event.getEntity() instanceof LivingEntity)) return;

        if (event.getEntity() instanceof Player) {
            Combat.applyCombat((Player) event.getEntity());
        }

        LivingEntity damager = ListenerUtil.getDamager(event);
        if (damager instanceof Player) {
            Combat.applyCombat((Player) damager);
        }
    }

    /**
     * Applies or removes SkillAPI features from a player upon switching worlds
     *
     * @param event event details
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (event.getPlayer().hasMetadata("NPC"))
            return;

        boolean oldEnabled = SkillAPI.getSettings().isWorldEnabled(event.getFrom());
        boolean newEnabled = SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld());
        if (oldEnabled && !newEnabled) {
            PlayerData data = SkillAPI.getPlayerData(event.getPlayer().getUniqueId());
            if (data == null) return;
            data.clearBonuses();
            data.stopPassives(event.getPlayer());
            //.getPlayer().setMaxHealth(SkillAPI.getSettings().getDefaultHealth());
            //event.getPlayer().setHealth(SkillAPI.getSettings().getDefaultHealth());
            if (!SkillAPI.getSettings().getLevelBar().equalsIgnoreCase("none")) {
                event.getPlayer().setLevel(0);
                event.getPlayer().setExp(0);
            }
            if (!SkillAPI.getSettings().getFoodBar().equalsIgnoreCase("none")) {
                event.getPlayer().setFoodLevel(20);
            }
        } else if (!oldEnabled && newEnabled) {
            init(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(final PlayerCommandPreprocessEvent event) {
        if (!SkillAPI.getSettings().isWorldEnabled(event.getPlayer().getWorld()))
            return;

        if (event.getMessage().equals("/clear")) {
            handleClear(event.getPlayer());
        } else if (event.getMessage().startsWith("/clear ")) {
            handleClear(VersionManager.getPlayer(event.getMessage().substring(7)));
        }
    }

    @EventHandler
    public void onCommand(final ServerCommandEvent event) {
        if (event.getCommand().startsWith("clear ")) {
            handleClear(VersionManager.getPlayer(event.getCommand().substring(6)));
        }
    }

    private void handleClear(final Player player) {
        if (player != null) {
            SkillAPI.schedule(() -> {
                final PlayerData data = SkillAPI.getPlayerData(player.getUniqueId());
                if (data == null) return;
                CLEAR_HANDLERS.forEach(handler -> handler.accept(player));
            }, 1);
        }
    }
}
