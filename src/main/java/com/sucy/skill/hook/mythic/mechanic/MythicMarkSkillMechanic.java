package com.sucy.skill.hook.mythic.mechanic;

import com.sucy.skill.hook.mythic.AgentSkill;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.*;
import io.lumine.xikage.mythicmobs.skills.mechanics.MetaSkillMechanic;

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mechanic
 *
 * @author 老廖
 * @since 2024/7/3 17:22
 */
public class MythicMarkSkillMechanic extends MetaSkillMechanic {

    protected String mark;

    protected AgentSkill onSkill = null;

    public MythicMarkSkillMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        //System.out.println("load skill "+skill);
        getPlugin().getSkillManager().queueSecondPass(new Runnable() {
            @Override
            public void run() {
                //System.out.println("重载 reload");
                metaskill.ifPresent(value -> onSkill = new AgentSkill(value));
            }

        });
        this.mark = mlc.getString(new String[]{"mark", "mk"}, "null");
        //Bukkit.getScheduler().runTaskLater(getPlugin(), () -> getPlugin().getSkillManager().runSecondPass(), 20);
        getPlugin().getSkillManager().runSecondPass();
    }

    public MythicMarkSkillMechanic(String skill, String skillName, MythicLineConfig mlc) {
        super(skill, skillName, mlc);
        getPlugin().getSkillManager().queueSecondPass(() -> {
            metaskill.ifPresent(value -> onSkill = new AgentSkill(value));
        });
        this.mark = mlc.getString(new String[]{"mark", "mk"}, "null");
    }

    @Override
    public AgentSkill getSkill() {
        if (onSkill == null) {
            //System.out.println("onSkill == null");
            metaskill.ifPresent(value -> onSkill = new AgentSkill(value));
            //System.out.println("reload onSkill");
        }
        //if (onSkill != null) {
            //System.out.println("onSkill != null");
        //}
        return onSkill;
    }

    @Override
    public boolean cast(SkillMetadata skillMetadata) {
        try {
            SkillMetadata data;
            if (!this.parameters.isEmpty()) {
                data = this.injectParameters(skillMetadata);
            } else {
                data = skillMetadata;
            }
            AgentSkill skill = getSkill();
            if (skill != null && skill.isUsable(data)) {
                skill.execute(data);
                return true;
            }
        } catch (Throwable e) {
            AgentSkill skill = getSkill();
            if (skill != null && skill.isUsable(skillMetadata)) {
                skill.execute(skillMetadata);
                return true;
            }
        }
        return false;
    }


    private SkillMetadata injectParameters(SkillMetadata data) {
        data = data.deepClone();
        data.getParameters().putAll(this.parameters);
        return data;
    }




}
