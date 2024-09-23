package com.sucy.skill.hook.mythic.mechanic;

import com.sucy.skill.hook.mythic.AgentSkill;
import com.sucy.skill.hook.mythic.MythicListener;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mechanic
 *
 * @author 老廖
 * @since 2024/7/3 17:22
 */
@MythicMechanic(
        author = "老廖",
        name = "return",
        aliases = {"r"},
        description = "标记上下文可中断"
)
public class MythicReturnMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected String[] mark;

    public MythicReturnMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        this.mark = mlc.getString(new String[]{"mark", "mk"}, "null").split(",");
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (mark != null) {
            //System.out.println("添加标记: "+mark2+" target: "+target.getName());
            for (String a : mark) {
                AgentSkill.cancelMark(target, a);
            }
            //MythicListener.addMark(target, mark);
        }
        return true;
    }

}
