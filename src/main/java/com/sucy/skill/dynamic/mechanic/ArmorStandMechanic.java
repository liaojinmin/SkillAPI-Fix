package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.armorstand.ArmorStandInstance;
import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.listener.MechanicListener;
import com.sucy.skill.task.RemoveTask;
import net.Indyuce.mmoitems.api.event.ItemEquipEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
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
        if (type != null && !type.equalsIgnoreCase("AIR")) {
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

            /*
            LivingEntity livingEntity = target.getWorld().spawn(loc, Husk.class, as -> {
                as.setCustomName(name.replace("{player}", caster.getName()));
                as.setCustomNameVisible(nameVisible);
                as.setAI(false);
                as.setInvulnerable(true);
            });

             */

            ArmorStand livingEntity = target.getWorld().spawn(loc, ArmorStand.class, as -> {
                try {
                    as.setMarker(marker);
                    as.setInvulnerable(true);
                    as.setSilent(true);
                } catch (NoSuchMethodError ignored) {
                }

                as.setGravity(gravity);
                as.setCustomName(name.replace("{player}", caster.getName()));
                as.setCustomNameVisible(nameVisible);
                as.setSmall(small);
                as.setArms(arms);
                as.setBasePlate(base);
                as.setVisible(visible);
                as.setHelmet(itemStack);
            });
            livingEntity.teleport(loc);
            livingEntity.setHeadPose(
                    new EulerAngle(Math.toDegrees(loc.getPitch()), 0, 0)
            );
            SkillAPI.setMeta(livingEntity, MechanicListener.ARMOR_STAND, true);
            //设置一下主人
            SkillAPI.setMeta(livingEntity, AttributeAPI.FX_SKILL_API_MASTER, caster.getUniqueId());

            armorStands.add(livingEntity);
            ArmorStandInstance instance;
            if (follow) {
                instance = new ArmorStandInstance(livingEntity, caster, forward, upward, right);
            } else {
                instance = new ArmorStandInstance(livingEntity, caster);
            }
            instance.setRunnable(it ->
                    Bukkit.getScheduler().runTask(SkillAPI.singleton(), () -> {
                        if (it.get()) {
                            executeChildren(caster, context, level, armorStands);
                        }
                    })
            );
            keys.add(instance.indexID);
            ArmorStandManager.register(instance, caster, instance.indexID);
        }
        new RemoveTask(armorStands, duration);
        return targets.size() > 0;
    }
}
