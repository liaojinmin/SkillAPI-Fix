package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.armorstand.ArmorStandInstance;
import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.listener.MechanicListener;
import com.sucy.skill.task.RemoveTask;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Summons an armor stand that can be used as a marker or for item display. Applies child components on the armor stand
 */
public class ArmorStandMechanic extends MechanicComponent {

    private static final Vector UP = new Vector(0, 1, 0);
    private static final String KEY = "key"; // 标记名称，默认是盔甲架名称
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
    private static final String SKILLS = "skills"; // 盔甲架为施法者使用的技能

    @Override
    public String getKey() {
        return "armor stand";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        int duration = (int) (20 * parseValues(caster, DURATION, level, 5));
        String name = settings.getString(NAME, "Armor Stand");
        String key = settings.getString(KEY, name);
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

        List<String> skills = settings.getStringList(SKILLS);

        List<LivingEntity> armorStands = new ArrayList<>();
        for (LivingEntity target : targets) {
            Location loc = target.getLocation().clone();
            Vector dir = loc.getDirection().setY(0).normalize();
            Vector side = dir.clone().crossProduct(UP);
            loc.add(dir.multiply(forward)).add(0, upward, 0).add(side.multiply(right));

            ArmorStand armorStand = target.getWorld().spawn(loc, ArmorStand.class, as -> {
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
            });
            SkillAPI.setMeta(armorStand, MechanicListener.ARMOR_STAND, true);
            //设置一下主人
            SkillAPI.setMeta(armorStand, AttributeAPI.FX_SKILL_API_MASTER, caster.getUniqueId());

            for (String skillName : skills) {
                Skill skill = SkillAPI.getSkill(skillName);
                if (skill != null) {
                    SkillCastAPI.cast(armorStand, skill, level);
                }
            }
            armorStands.add(armorStand);
            ArmorStandInstance instance;
            if (follow) {
                instance = new ArmorStandInstance(armorStand, target, forward, upward, right);
            } else {
                instance = new ArmorStandInstance(armorStand, target);
            }
            ArmorStandManager.register(instance, target, key);
        }
        executeChildren(caster, level, armorStands);
        new RemoveTask(armorStands, duration);
        return targets.size() > 0;
    }
}
