package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.EffectComponent;
import com.sucy.skill.dynamic.mechanic.ReturnMechanic;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SkillAPI © 2018
 * com.sucy.trigger.dynamic.skill.TriggerComponent
 */
public class TriggerComponent extends EffectComponent {

    private boolean running = false;

    public boolean isRunning() {
        return running;
    }

    public boolean trigger(final LivingEntity caster, final LivingEntity target, final int level) {
        return execute(caster, new SkillContext(""), level, new ArrayList<>(Collections.singletonList(target)));
    }

    @Override
    public String getKey() {
        return "trigger";
    }

    @Override
    public ComponentType getType() {
        return ComponentType.TRIGGER;
    }

    @Override
    public boolean execute(final LivingEntity caster, SkillContext context, final int level, final List<LivingEntity> targets) {
        try {
            if (context.get("trigger_use") != null) {
                if (context.remove("trigger_mark") == null) {
                    return false;
                }
            }
            running = true;
            return executeChildren(caster, context, level, targets);
        } finally {
            running = false;
        }
    }
}
