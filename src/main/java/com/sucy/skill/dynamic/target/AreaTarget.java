
package com.sucy.skill.dynamic.target;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.util.Nearby;
import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.dynamic.TempEntity;
import com.sucy.skill.listener.MechanicListener;
import com.sucy.skill.utils.target.TargetHelper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Applies child components to the closest all nearby entities around
 * each of the current targets.
 */
public class AreaTarget extends TargetComponent {
    private static final String RADIUS = "radius";
    private static final String RANDOM = "random";

    private final Random random = new Random();

    /** {@inheritDoc} */
    @Override
    List<LivingEntity> getTargets(final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        final double radius = parseValues(caster, RADIUS, level, 3.0);
        final boolean random = settings.getBool(RANDOM, false);
        return determineTargets(caster, level, targets, t -> shuffle(Nearby.getLivingNearby(t.getLocation(), radius), random));
    }

    /** {@inheritDoc} */
    @Override
    void makeIndicators(final List<IIndicator> list, final Player caster, final LivingEntity target, final int level) {
        makeCircleIndicator(list, target, parseValues(caster, RADIUS, level, 3.0));
    }

    private boolean isValidTargets(final LivingEntity caster, final LivingEntity from, final LivingEntity target) {
        if (SkillAPI.getMeta(target, MechanicListener.ARMOR_STAND) != null) {
            return false;
        }
        if (target instanceof TempEntity) {
            return true;
        }
        if (self && target == caster) {
            return true;
        }
        return target != caster && SkillAPI.getSettings().isValidTarget(target)
                && (throughWall || ! TargetHelper.isObstructed(from.getEyeLocation(), target.getEyeLocation()))
                && (everyone || allies == TargetHelper.isAlly(caster, target));
    }

    @Override
    List<LivingEntity> determineTargets(
            final LivingEntity caster,
            final int level,
            final List<LivingEntity> from,
            final Function<LivingEntity, List<LivingEntity>> conversion
    ) {

        final double max = parseValues(caster, MAX, level, 99);

        final List<LivingEntity> list = new ArrayList<>();
        from.forEach(target -> {
            final int count = list.size();
            for (LivingEntity entity : conversion.apply(target)) {
               // System.out.println("检查目标: "+entity.getName());
                if (this.isValidTargets(caster, target, entity)) {
                 //   Logger.log("  添加目标 "+entity.getName());
                    list.add(entity);
                    if (list.size() - count >= max) {
                        break;
                    }
                }
            }
        });
        //System.out.println("list: "+list.stream().map(it -> it.getName() != null ? it.getName() : it.getUniqueId().toString()).collect(Collectors.joining()));
        return list;
    }

    @Override
    public String getKey() {
        return "area";
    }

    private List<LivingEntity> shuffle(final List<LivingEntity> targets, final boolean random) {
        if (!random) return targets;

        final List<LivingEntity> list = new ArrayList<>();
        while (!targets.isEmpty()) {
            list.add(targets.remove(this.random.nextInt(targets.size())));
        }
        return list;
    }
}
