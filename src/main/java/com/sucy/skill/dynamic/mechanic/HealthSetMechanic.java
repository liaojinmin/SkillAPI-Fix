package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * SkillAPI © 2017
 * com.sucy.mechanic.dynamic.skill.HealthSetMechanic
 */
public class HealthSetMechanic extends MechanicComponent {

    private static final String HEALTH = "health";

    @Override
    public String getKey() {
        return "health set";
    }

    @Override
    public boolean execute(final LivingEntity caster, SkillContext context, final int level, final List<LivingEntity> targets) {
        final double health = Math.max(1, parseValues(caster, HEALTH, level, 1));

        for (final LivingEntity target : targets) {
            target.setHealth(Math.min(health, target.getMaxHealth()));
        }

        return true;
    }
}
