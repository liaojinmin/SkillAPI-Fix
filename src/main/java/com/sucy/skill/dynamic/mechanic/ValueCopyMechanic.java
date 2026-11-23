package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * SkillAPI © 2018
 * com.sucy.mechanic.dynamic.skill.ValueCopyMechanic
 */
public class ValueCopyMechanic extends MechanicComponent {
    private static final String KEY       = "key";
    private static final String TARGET    = "destination";
    private static final String TO_TARGET = "to-target";

    @Override
    public String getKey() {
        return "value copy";
    }

    @Override
    public boolean execute(final SkillContext context, final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        if (targets.isEmpty() || !settings.has(KEY)) {
            return false;
        }

        final String key = settings.getString(KEY);
        final String destination = settings.getString(TARGET, key);
        final boolean toTarget = settings.getString(TO_TARGET, "true").equalsIgnoreCase("true");

        if (toTarget) {
            targets.forEach(target -> apply(context, caster, target, key, destination));
        } else {
            apply(context, targets.get(0), caster, key, destination);
        }

        return true;
    }

    private boolean apply(final SkillContext context, final LivingEntity from, final LivingEntity to, final String key, final String destination) {
        final Object value = context.get(from, key, useGlobalContext);
        if (value == null) return false;
        context.set(from, destination, value, useGlobalContext);
        return true;
    }
}
