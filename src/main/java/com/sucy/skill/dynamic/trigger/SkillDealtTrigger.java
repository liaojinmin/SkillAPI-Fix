package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;

import java.util.Map;

/**
 * SkillAPI © 2018
 * com.sucy.trigger.dynamic.skill.BlockBreakTrigger
 */
public class SkillDealtTrigger extends SkillTrigger<SkillDamageEvent> {

    public SkillDealtTrigger() {
        super(SkillDamageEvent.class);
    }

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "SKILL_DAMAGE";
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final SkillDamageEvent event) {
        return event.getDamager();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final SkillDamageEvent event, final Settings settings) {
        return isUsingTarget(settings) ? event.getTarget() : event.getDamager();
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final SkillDamageEvent event, final SkillContext context, boolean useGlobal) {
        context.set(getCaster(event), "api-dealt", event.getDamage(), useGlobal);
    }
}
