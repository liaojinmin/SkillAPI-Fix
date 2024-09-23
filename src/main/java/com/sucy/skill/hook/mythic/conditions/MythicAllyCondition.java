package com.sucy.skill.hook.mythic.conditions;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mythic.conditions
 *
 * @author 老廖
 * @since 2024/8/17 15:49
 */
public class MythicAllyCondition extends SkillCondition implements IEntityCondition {

    public MythicAllyCondition(String line) {
        super(line);
    }

    @Override
    public boolean check(AbstractEntity target) {

        return false;
    }


}
