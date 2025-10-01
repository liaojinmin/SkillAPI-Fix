package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.armorstand.ArmorStandEntity;
import com.sucy.skill.api.armorstand.ArmorStandInstance;
import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.ArmorStandCarrier;
import com.sucy.skill.task.RemoveTask;
import me.neon.libs.carrier.minecraft.meta.ArmorStandMeta;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Summons an armor stand that can be used as a marker or for item display. Applies child components on the armor stand
 */
public class ArmorStandMechanic extends MechanicComponent {

    private static final Vector UP = new Vector(0, 1, 0);
    private static final String DURATION = "duration"; // 移除时间
    private static final String NAME = "name"; // 盔甲架名称
    private static final String NAME_VISIBLE = "name-visible"; // 盔甲架名称是否可见 true、false
    private static final String FOLLOW = "follow";  // 是否跟随施法者 true、false
    private static final String GRAVITY = "gravity";  // 是否在地面 true、false
    private static final String SMALL = "tiny"; // // 是否是小型盔甲架 true、false
    private static final String ARMS = "arms"; // 是否有盔甲 true、false
    private static final String BASE = "base"; // 是否有地板 true、false
    private static final String VISIBLE = "visible"; // 是否隐身 true、false
    private static final String MARKER = "marker"; // 是否标记 true、false
    private static final String FORWARD = "forward"; // 向前 0.1、0.5
    private static final String UPWARD = "upward"; // 向上 0.1、0.5
    private static final String RIGHT = "right"; // 向右 0.1、0.5

    private static final String ITEM_TYPE = "item_type";

    private static final String ITEM_NAME = "item_name";

    @Override
    public String getKey() {
        return "armor stand";
    }

    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        int duration = (int) (20 * parseValues(caster, DURATION, level, 5));
        String name = settings.getString(NAME, "Armor Stand");
        boolean nameVisible = settings.getBool(NAME_VISIBLE, false);
        boolean follow = settings.getBool(FOLLOW, false);
        boolean gravity = settings.getBool(GRAVITY, false);
        boolean small = settings.getBool(SMALL, false);
        boolean arms = settings.getBool(ARMS, false);
        boolean base = settings.getBool(BASE, false);
        boolean visible = settings.getBool(VISIBLE, true);
        boolean marker = settings.getBool(MARKER, false);
        double forward = parseValues(caster, FORWARD, level, 0);
        double upward = parseValues(caster, UPWARD, level, 0);
        double right = parseValues(caster, RIGHT, level, 0);

        List<LivingEntity> armorStands = new ArrayList<>();
        List<Integer> keys = context.getIntegerList(getKey());
        ItemStack itemStack;
        String type = settings.getString(ITEM_TYPE, "AIR");
        if (type != null && !type.equalsIgnoreCase("AIR") && !type.equalsIgnoreCase("ARROW")) {
            itemStack = new ItemStack(Material.valueOf(type));
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(settings.getString(ITEM_NAME, "null"));
            itemStack.setItemMeta(meta);
        } else itemStack =  new ItemStack(Material.AIR);
        for (LivingEntity target : targets) {
            Location loc = target.getLocation();
            Vector dir = loc.getDirection().setY(0).normalize();
            Vector side = dir.clone().crossProduct(UP);
            loc.add(dir.multiply(forward)).add(0, upward, 0).add(side.multiply(right));

            ArmorStandCarrier armorStandCarrier = new ArmorStandCarrier(
                    loc,
                    EntityType.ARMOR_STAND,
                    new ArmorStandMeta(false, false, small, arms, base, marker)
            );
            armorStandCarrier.setDisplayName(
                    name.replace("{player}", caster.getName())
            );
            if (itemStack.getType() != Material.AIR) {
                armorStandCarrier.addItemsStack(itemStack);
            }
            armorStandCarrier.setLocation(loc);

            //livingEntity.setHeadPose(
                  //  new EulerAngle(Math.toDegrees(loc.getPitch()), 0, 0)
           // );

            ArmorStandEntity entity = new ArmorStandEntity(armorStandCarrier, caster);
            armorStands.add(entity);

            ArmorStandInstance instance;
            if (follow) {
                instance = new ArmorStandInstance(entity, caster, forward, upward, right);
            } else {
                instance = new ArmorStandInstance(entity, caster);
            }
            keys.add(instance.indexID);
            armorStandCarrier.setDead(false);

            instance.setRunnable(it ->
                    Bukkit.getScheduler().runTask(SkillAPI.singleton(), () -> {
                        if (it.get()) {
                            executeChildren(caster, context, level, armorStands);
                        }
                    })
            );
            ArmorStandManager.register(instance, caster, instance.indexID);
        }
        new RemoveTask(armorStands, duration);
        return !targets.isEmpty();
    }
}
