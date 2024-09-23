package com.sucy.skill.hook.mythic.mechanic;

import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.IDummySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;

@MythicMechanic(
        author = "廖爷爷",
        name = "markDelay",
        description = "Delays the execution of the next mechanic"
)
public class MythicDelayMechanic extends SkillMechanic implements IDummySkill {

    protected int ticks;

    protected String mark;

    public MythicDelayMechanic(String line, MythicLineConfig mlc) {
        super(line, mlc);
        this.mark = mlc.getString(new String[]{"mark", "mk"}, "default");
        this.ticks = mlc.getInteger(new String[]{"timer", "t", "delay"}, 20);
    }

    public int getTicks() {
        return this.ticks;
    }

    public String getMark() {
        return mark;
    }

}
