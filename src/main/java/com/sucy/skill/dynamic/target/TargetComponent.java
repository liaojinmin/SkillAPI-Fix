package com.sucy.skill.dynamic.target;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.armorstand.ArmorStandEntity;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.EffectComponent;
import com.sucy.skill.dynamic.TempEntity;
import com.sucy.skill.utils.target.TargetHelper;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * SkillAPI © 2018
 * com.sucy.target.dynamic.skill.TargetComponent
 */
public abstract class TargetComponent extends EffectComponent {

    private static final String ALLY   = "group";
    private static final String WALL   = "wall";
    private static final String CASTER = "caster";
    protected static final String MAX    = "max";

    boolean everyone;
    boolean allies;
    boolean throughWall;
    boolean self;

    @Override
    public ComponentType getType() {
        return ComponentType.TARGET;
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(final SkillContext context, LivingEntity caster, int level, List<LivingEntity> targets) {
        List<LivingEntity> list = getTargets(context, caster, level, targets);
        return (!list.isEmpty() && executeChildren(context, caster, level, list));
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        final String group = settings.getString(ALLY, "enemy").toLowerCase();
        everyone = group.equalsIgnoreCase("both");
        allies = group.equalsIgnoreCase("ally");
        throughWall = settings.getString(WALL, "false").equalsIgnoreCase("true");
        self = settings.getString(CASTER, "false").equalsIgnoreCase("true");
    }

    abstract List<LivingEntity> getTargets(
            final SkillContext context,
            final LivingEntity caster,
            final int level,
            final List<LivingEntity> targets);


    List<LivingEntity> determineTargets(
            final SkillContext context,
            final LivingEntity caster,
            final int level,
            final List<LivingEntity> from,
            final Function<LivingEntity,
                    List<LivingEntity>> conversion
    ) {

        final double max = parseValues(context, caster, MAX, level, 99);

        final List<LivingEntity> list = new ArrayList<>();
        from.forEach(target -> {
            final int count = list.size();
            final List<LivingEntity> found = conversion.apply(target);
            for (LivingEntity entity : found) {
                if (isValidTarget(caster, target, entity)) {
                    list.add(entity);
                    if (list.size() - count >= max) {
                        break;
                    }
                }
            }
        });
        if (self) {
            list.add(caster);
        }
        return list;
    }

    boolean isValidTarget(final LivingEntity caster, final LivingEntity from, final LivingEntity target) {
        if (target instanceof ArmorStandEntity) return false;

        if (target instanceof TempEntity) {
            return true;
        }

        return target != caster && SkillAPI.getSettings().isValidTarget(target)
                && (throughWall || !TargetHelper.isObstructed(from.getEyeLocation(), target.getEyeLocation()))
                && (everyone || allies == TargetHelper.isAlly(caster, target));
    }
}
