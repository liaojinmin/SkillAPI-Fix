package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.Map;

/**
 * SkillAPI © 2018
 * com.sucy.trigger.dynamic.skill.BlockBreakTrigger
 */
public class LaunchTrigger implements Trigger<ProjectileLaunchEvent> {

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "LAUNCH";
    }

    /** {@inheritDoc} */
    @Override
    public Class<ProjectileLaunchEvent> getEvent() {
        return ProjectileLaunchEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final ProjectileLaunchEvent event, final int level, final Settings settings) {
        final String type = settings.getString("type", "any");
        return type.equalsIgnoreCase("ANY") || type.equalsIgnoreCase(event.getEntity().getType().name());
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final ProjectileLaunchEvent event, final SkillContext context, final boolean useGlobal) {
        context.set(getCaster(event), "api-velocity", event.getEntity().getVelocity().length(), useGlobal);
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof LivingEntity) {
            return (LivingEntity) event.getEntity().getShooter();
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final ProjectileLaunchEvent event, final Settings settings) {
        return getCaster(event);
    }
}
