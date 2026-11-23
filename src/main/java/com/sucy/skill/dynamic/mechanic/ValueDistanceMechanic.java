package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * SkillAPI © 2017
 * com.sucy.mechanic.dynamic.skill.ValueDistanceMechanic
 */
public class ValueDistanceMechanic extends MechanicComponent
{
    private static final String KEY  = "key";

    @Override
    public String getKey() {
        return "value distance";
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
    public boolean execute(final SkillContext context, final LivingEntity caster, final int level, final List<LivingEntity> targets)
    {
        if (!settings.has(KEY) || !(caster instanceof Player))
        {
            return false;
        }

        final String key = settings.getString(KEY);
        context.set(caster, key, targets.get(0).getLocation().distance(caster.getLocation()), useGlobalContext);
        return true;
    }
}
