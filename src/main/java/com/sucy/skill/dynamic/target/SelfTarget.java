package com.sucy.skill.dynamic.target;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.cast.IIndicator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Applies child components to the caster
 */
public class SelfTarget extends TargetComponent {

    /** {@inheritDoc} */
    @Override
    public void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) {
        makeCircleIndicator(list, caster, 0.5);

    }

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        return ImmutableList.of(caster);
    }

    @Override
    public String getKey() {
        return "self";
    }

}
