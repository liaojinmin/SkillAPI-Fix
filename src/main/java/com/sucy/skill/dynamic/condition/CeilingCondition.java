package com.sucy.skill.dynamic.condition;

import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public class CeilingCondition extends ConditionComponent
{
    private static final String DISTANCE = "distance";
    private static final String AT_LEAST = "at-least";

    @Override
    public boolean test(final SkillContext context, final LivingEntity caster, final int level, final LivingEntity target) {
        final boolean atLeast = settings.getBool(AT_LEAST, true);
        final int distance = (int) parseValues(context, caster, DISTANCE, level, 5);

        final Block block = target.getLocation().getBlock();
        boolean ceiling = false;
        for (int i = 2; i < distance; i++) {
            if (block.getRelative(0, i, 0).getType().isSolid()) {
                ceiling = true;
                break;
            }
        }
        return ceiling != atLeast;
    }

    @Override
    public String getKey() {
        return "ceiling";
    }
}
