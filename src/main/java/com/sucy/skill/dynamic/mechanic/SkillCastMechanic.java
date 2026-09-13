package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Adds to a cast data value
 */
public class SkillCastMechanic extends MechanicComponent {

    private static final String KEY  = "skill";

    @Override
    public String getKey() {
        return "skill cast";
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
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        if (!settings.has(KEY)) {
            return false;
        }

        String skillName = settings.getString(KEY);

        Skill skill = SkillAPI.getSkill(skillName);

        if (skill == null) {
            return false;
        }

        for (LivingEntity livingEntity : targets) {
           // System.out.println("SkillCastMechanic >>> "+livingEntity.getName() + " skill: "+skillName);
            SkillCastAPI.cast(livingEntity, skill, level);
        }

        return true;
    }
}
