package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;

import java.util.List;


public class SkillCastMechanic extends MechanicComponent {

    private static final String KEY  = "skill";

    @Override
    public String getKey() {
        return "skill cast";
    }

    @Override
    public boolean execute(final SkillContext context, LivingEntity caster, int level, List<LivingEntity> targets) {
        if (!settings.has(KEY)) {
            return false;
        }

        Skill skill = SkillAPI.getSkill(settings.getString(KEY));
        if (skill == null) {
            return false;
        }

        for (LivingEntity livingEntity : targets) {
            SkillCastAPI.cast(context, livingEntity, skill, level, null);
        }

        return true;
    }
}
