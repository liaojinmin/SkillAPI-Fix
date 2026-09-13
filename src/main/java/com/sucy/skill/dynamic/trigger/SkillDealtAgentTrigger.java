package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.SkillDamageAgentEvent;
import com.sucy.skill.api.event.SkillDamageEvent;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/**
 * SkillAPI © 2018
 * com.sucy.trigger.dynamic.skill.BlockBreakTrigger
 */
public class SkillDealtAgentTrigger extends SkillTrigger<SkillDamageAgentEvent> {

    public SkillDealtAgentTrigger() {
        super(SkillDamageAgentEvent.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "SKILL_DAMAGE_AGENT";
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final SkillDamageAgentEvent event) {
        return event.getDamager();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final SkillDamageAgentEvent event, final Settings settings) {
        return isUsingTarget(settings) ? event.getTarget() : event.getDamager();
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final SkillDamageAgentEvent event, final Map<String, Object> data) {
        data.put("api-dealt", event.getDamage());
    }
}
