package com.sucy.skill.dynamic.condition;

import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.EffectComponent;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SkillAPI © 2018
 * com.sucy.condition.dynamic.skill.ConditionComponent
 */
public abstract class ConditionComponent extends EffectComponent {

    /** {@inheritDoc} */
    @Override
    public ComponentType getType() {
        return ComponentType.CONDITION;
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(
            final LivingEntity caster, SkillContext context, final int level, final List<LivingEntity> targets) {

        final List<LivingEntity> filtered = targets.stream()
                .filter(t -> test(caster, level, t))
                .collect(Collectors.toList());

        return filtered.size() > 0 && executeChildren(caster, context, level, filtered);
    }

    abstract boolean test(final LivingEntity caster, final int level, final LivingEntity target);
}
