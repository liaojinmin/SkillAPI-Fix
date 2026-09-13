package com.sucy.skill.api.projectile;

import com.germ.germplugin.api.GermPacketAPI;
import com.sucy.skill.api.Settings;
import com.sucy.skill.dynamic.ArmorStandCarrier;
import me.neon.libs.carrier.minecraft.meta.ArmorStandMeta;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.projectile
 *
 * @author 老廖
 * @since 2025/5/26 23:35
 */
public class CarrierProjectile extends ParticleProjectile {

    public static final String ARMOR_STAND = "armor-stand";

    private static final String NAME = "name"; // 盔甲架名称

    private static final String NAME_VISIBLE = "name-visible"; // 盔甲架名称是否可见 true、false

    private static final String SMALL = "small"; // // 是否是小型盔甲架 true、false

    private static final String VISIBLE = "visible"; // 是否隐身 true、false

    private static final String MARKER = "marker"; // 是否标记 true、false

    private static final String ITEM_TYPE = "item_type";

    private static final String ENTITY_TYPE = "entity_type";

    private static final String ITEM_NAME = "item_name";

    private static final String GERM_EFFECT = "germ_effect";

    private final ArmorStandCarrier armorStandCarrier;


    public CarrierProjectile(LivingEntity thrower, int level, Location loc, Settings settings) {
        super(thrower, level, loc, settings);
        if (settings.getBool(ARMOR_STAND, false)) {
            String name = settings.getString(NAME, "Armor Stand Packet");
            boolean small = settings.getBool(SMALL, false);
            boolean visible = settings.getBool(VISIBLE, true);
            boolean marker = settings.getBool(MARKER, false);
            EntityType entityType = EntityType.valueOf(settings.getString(ENTITY_TYPE, EntityType.ARMOR_STAND.name()));
            if (entityType == EntityType.ARMOR_STAND) {
                armorStandCarrier = new ArmorStandCarrier(loc, entityType,new ArmorStandMeta(visible, false, small, false, true, marker));
            } else {
                armorStandCarrier = new ArmorStandCarrier(loc, entityType);
            }

            String type = settings.getString(ITEM_TYPE, "AIR");
            if (type != null && !type.equalsIgnoreCase("AIR") && !type.equalsIgnoreCase("ARROW")) {
                ItemStack itemStack = new ItemStack(Material.valueOf(type));
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(settings.getString(ITEM_NAME, "null"));
                itemStack.setItemMeta(meta);
                armorStandCarrier.addItemsStack(itemStack);
            }
            armorStandCarrier.setDisplayName(name);
            armorStandCarrier.setDead(false);
            String effect = settings.getString(GERM_EFFECT, "");
            // System.out.println("effect: "+effect);
            if (effect != null && !effect.isEmpty()) {
                for (Player it : loc.getNearbyPlayers(32, 12, 32)) {
                    // System.out.println("send "+it.getName() + " effect: "+effect);
                    GermPacketAPI.sendEffectToEntity(it, effect, armorStandCarrier.getUniqueId().toString(), armorStandCarrier.getEntityId());
                }
            }
            armorStandCarrier.setLocation(loc);

        } else {
            armorStandCarrier = null;
        }
    }

    @Override
    public void run() {
        // 尝试修复因为世界卸载，tick也就运行导致的问题。2026/8/15
        World world = loc.getWorld();
        if (world == null || Bukkit.getWorld(world.getUID()) == null) {
            System.out.println("世界不存在，中断，任务ID: "+getTaskId());
            cancel();
            Bukkit.getPluginManager().callEvent(expire());
            return;
        }

        applySteps();

        // Particle along path
        count++;
        if (count >= freq) {
            count = 0;
            if (armorStandCarrier != null) {
                armorStandCarrier.teleport(loc);
            }
        }
        // Lifespan
        life--;
        if (life <= 0) {
            Bukkit.getPluginManager().callEvent(expire());
            cancel();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        if (armorStandCarrier != null) {
            armorStandCarrier.setDead(true);
        }
    }

}
