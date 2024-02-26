package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.hook.MythicMobsHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2023/9/28 1:10
 */
public class MythicCastTargetMechanic extends MechanicComponent {
    private static final String SKILLNAME = "skillname";

    private static final String POWER = "power";

    @Override
    public String getKey() {
        return "mythic cast target";
    }


    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (!settings.has(SKILLNAME)) {
            return false;
        }

        String key = settings.getString(SKILLNAME).replace("{uuid}", caster.getUniqueId().toString());
        float power = (float) parseValues(caster, POWER, level, 1);
        Collection<Entity> etarget = new ArrayList<>(targets);
        MythicMobsHook.castSkill(caster, key, etarget, power);
        return true;
    }
}
