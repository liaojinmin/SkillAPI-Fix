package com.sucy.skill.hook.mythic.mechanic;

import com.google.common.collect.Lists;
import com.sucy.skill.hook.mythic.AgentSkill;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.logging.MythicLogger;
import io.lumine.xikage.mythicmobs.skills.*;
import io.lumine.xikage.mythicmobs.skills.mechanics.RandomSkillMechanic;
import io.lumine.xikage.mythicmobs.utils.numbers.Numbers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mechanic
 *
 * @author 老廖
 * @since 2024/7/3 17:22
 */
public class MythicMarkRandomSkillMechanic extends SkillMechanic implements IMetaSkill {

    protected List<AgentSkill> agentSkills = new ArrayList<>();

    public MythicMarkRandomSkillMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        try {
            String s3 = mlc.getString(new String[]{"skills", "skill", "s", "metas", "meta", "m"});
            String[] ss = s3.split(",");
            for (String s : ss) {
                Optional<Skill> maybeSkill = MythicMobs.inst().getSkillManager().getSkill(s);
                if (maybeSkill.isPresent()) {
                    Skill s2 = maybeSkill.get();
                    AgentSkill agentSkill;
                    if (s2 instanceof AgentSkill) {
                        agentSkill = (AgentSkill) s2;
                    } else {
                        agentSkill = new AgentSkill(s2);
                    }
                    agentSkills.add(agentSkill);
                } else {
                    MythicLogger.debug(MythicLogger.DebugLevel.MECHANIC, "+ 无法获取技能 "+s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean cast(SkillMetadata data) {
        List<AgentSkill> skills = Lists.newArrayList(this.agentSkills);
        while (!skills.isEmpty()) {
            AgentSkill ms;
            if (skills.size() > 1) {
                ms = skills.get(Numbers.randomInt(skills.size()));
            } else {
                ms = skills.get(0);
            }
            if (ms.usable(data, null)) {
                ms.executeAgent(data);
                return true;
            }
            skills.remove(ms);
        }
        return false;
    }
}
