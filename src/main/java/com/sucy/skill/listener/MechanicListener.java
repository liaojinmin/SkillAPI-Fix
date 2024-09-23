package com.sucy.skill.listener;

import com.rit.sucy.version.VersionManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.FlagApplyEvent;
import com.sucy.skill.api.event.FlagExpireEvent;
import com.sucy.skill.api.event.PlayerLandEvent;
import com.sucy.skill.api.projectile.ItemProjectile;
import com.sucy.skill.dynamic.mechanic.BlockMechanic;
import com.sucy.skill.dynamic.mechanic.BlockWallMechanic;
import com.sucy.skill.dynamic.mechanic.PotionProjectileMechanic;
import com.sucy.skill.dynamic.mechanic.ProjectileMechanic;
import com.sucy.skill.hook.DisguiseHook;
import com.sucy.skill.hook.PluginChecker;
import com.sucy.skill.hook.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The listener for handling events related to dynamic mechanics
 */
public class MechanicListener extends SkillAPIListener {
    public static final String SUMMON_DAMAGE     = "sapiSumDamage";
    public static final String P_CALL            = "pmCallback";
    public static final String POTION_PROJECTILE = "potionProjectile";
    public static final String ITEM_PROJECTILE = "itemProjectile";
    public static final String SKILL_LEVEL       = "skill_level";
    public static final String SKILL_CASTER      = "caster";
    public static final String SPEED_KEY         = "sapiSpeedKey";
    public static final String DISGUISE_KEY      = "sapiDisguiseKey";

    public static final String ARMOR_STAND = "asMechanic";

    private static final HashMap<UUID, Integer> flying = new HashMap<>();
    private static final DecimalFormat df = new DecimalFormat("#0.00");


    /**
     * Cleans up listener data on shutdown
     */
    @Override
    public void cleanup() {
        flying.clear();
    }


    /**
     * Checks for landing on the ground
     *
     * @param event event details
     */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        boolean inMap = flying.containsKey(player.getUniqueId());
        if (inMap == player.isOnGround()) {
            if (inMap) {
                double maxHeight = flying.remove(player.getUniqueId());
            //    player.sendMessage("你落地了 ");
                Bukkit.getPluginManager().callEvent(
                        new PlayerLandEvent(player, Double.parseDouble(df.format(maxHeight - player.getLocation().getY())))
                );
            } else {
                flying.put(player.getUniqueId(), player.getLocation().getBlockY());
            }
        } else if (inMap) {
            int y = flying.get(player.getUniqueId());
            flying.put(player.getUniqueId(), Math.max(y, player.getLocation().getBlockY()));
        }
    }

    /**
     * Resets walk speed and clears them from the map when quitting
     *
     * @param event event details
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        flying.remove(event.getPlayer().getUniqueId());
        event.getPlayer().setWalkSpeed(0.2f);
    }

    /**
     * Applies effects when specific flag keys are set
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onApply(FlagApplyEvent event) {
        if (event.getEntity() instanceof Player)
        {
            if (event.getFlag().startsWith("perm:") && PluginChecker.isVaultActive())
                VaultHook.add((Player) event.getEntity(), event.getFlag().substring(5));
        }
    }

    /**
     * Clears speed modifiers when the flag expires
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpire(FlagExpireEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getFlag().startsWith("perm:") && PluginChecker.isVaultActive())
                VaultHook.remove((Player) event.getEntity(), event.getFlag().substring(5));
            else if (event.getFlag().equals(SPEED_KEY)) {
                AttributeListener.refreshSpeed((Player) event.getEntity());
            }
        }
        if (event.getFlag().equals(DISGUISE_KEY))
            DisguiseHook.removeDisguise(event.getEntity());
    }

    /**
     * Applies projectile callbacks when landing on the ground
     *
     * @param event event details
     */
    @EventHandler
    public void onLand(final ProjectileHitEvent event)
    {
        if (event.getEntity().hasMetadata(P_CALL))
            SkillAPI.schedule(() -> {
                final Object obj = SkillAPI.getMeta(event.getEntity(), P_CALL);
                if (obj != null)
                    ((ProjectileMechanic) obj).callback(event.getEntity(), null);
            }, 1);
    }

    /**
     * Prevent item projectiles from being absorbed by hoppers
     *
     * @param event event details
     */
    @EventHandler
    public void onItemLand(final InventoryPickupItemEvent event) {
        final Object meta = SkillAPI.getMeta(event.getItem(), ITEM_PROJECTILE);
        if (meta != null) {
            event.setCancelled(true);
            ((ItemProjectile) meta).applyLanded();
        }
    }

    /**
     * Stop explosions of projectiles fired from skills
     *
     * @param event event details
     */
    @EventHandler
    public void onExplode(EntityExplodeEvent event)
    {
        if (event.getEntity().hasMetadata(P_CALL))
            event.setCancelled(true);
    }

    /**
     * Applies projectile callbacks when striking an enemy
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onShoot(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile) {
            Projectile p = (Projectile) event.getDamager();
            if (p.hasMetadata(P_CALL) && event.getEntity() instanceof LivingEntity) {
                ((ProjectileMechanic) SkillAPI.getMeta(p, P_CALL))
                    .callback(p, (LivingEntity) event.getEntity());
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles when summoned monsters deal damage
     *
     * @param event event details
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onSummonDamage(EntityDamageByEntityEvent event)
    {
        if (event.getDamager().hasMetadata(SUMMON_DAMAGE))
            VersionManager.setDamage(event, SkillAPI.getMetaDouble(event.getDamager(), SUMMON_DAMAGE));
    }

    /**
     * Handles when a potion projectile hits things
     *
     * @param event event details
     */
    @EventHandler
    public void onSplash(PotionSplashEvent event)
    {
        if (event.getEntity().hasMetadata(POTION_PROJECTILE))
        {
            event.setCancelled(true);
            ((PotionProjectileMechanic) SkillAPI.getMeta(event.getEntity(), POTION_PROJECTILE))
                .callback(event.getEntity(), event.getAffectedEntities());
            event.getAffectedEntities().clear();
        }
    }

    /**
     * Can't break blocks from block mechanics
     *
     * @param event event details
     */
    @EventHandler
    public void onBreak(BlockBreakEvent event)
    {
        Location location = event.getBlock().getLocation();
        if (BlockMechanic.isPending(location)
                || BlockWallMechanic.isPending(location))
            event.setCancelled(true);
    }

    /**
     * Cancels damage to armor stands corresponding to an Armor Stand Mechanic
     *
     * @param event event details
     */
    @EventHandler
    public void onArmorStandDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ArmorStand && SkillAPI.getMeta(entity, ARMOR_STAND) != null) {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels interactions with  armor stands corresponding to an Armor Stand Mechanic
     *
     * @param event event details
     */
    @EventHandler
    public void onArmorStandInteract(PlayerArmorStandManipulateEvent event) {
        Entity entity = event.getRightClicked();
        if (SkillAPI.getMeta(entity, ARMOR_STAND) != null) {
            event.setCancelled(true);
        }
    }
}
