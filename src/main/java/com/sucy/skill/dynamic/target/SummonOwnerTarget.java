package com.sucy.skill.dynamic.target;

import com.google.common.collect.ImmutableList;
import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.hook.mythic.MythicManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Applies child components to the caster
 */
public class SummonOwnerTarget extends TargetComponent {

    /** {@inheritDoc} */
    @Override
    public void makeIndicators(List<IIndicator> list, Player caster, LivingEntity target, int level) {
        makeCircleIndicator(list, caster, 0.5);
    }

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(final LivingEntity caster, final int level, final List<LivingEntity> targets) {
        LivingEntity owner = MythicManager.INSTANCE.getSummonAscription().get(caster.getUniqueId());
        if (owner != null) {
            return ImmutableList.of(owner);
        }
        return ImmutableList.of();
    }

    @Override
    public String getKey() {
        return "summon owner";
    }

}
