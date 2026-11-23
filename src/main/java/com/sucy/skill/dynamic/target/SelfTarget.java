package com.sucy.skill.dynamic.target;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Applies child components to the caster
 */
public class SelfTarget extends TargetComponent {

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(
            final SkillContext context,
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        return ImmutableList.of(caster);
    }

    @Override
    public String getKey() {
        return "self";
    }

}
