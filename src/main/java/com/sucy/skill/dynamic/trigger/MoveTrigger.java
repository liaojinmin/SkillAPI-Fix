package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;

/**
 * SkillAPI © 2018
 * com.sucy.trigger.dynamic.skill.BlockBreakTrigger
 */
public class MoveTrigger implements Trigger<PlayerMoveEvent> {

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "MOVE";
    }

    /** {@inheritDoc} */
    @Override
    public Class<PlayerMoveEvent> getEvent() {
        return PlayerMoveEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final PlayerMoveEvent event, final int level, final Settings settings) {
        return event.getFrom().getWorld() == event.getTo().getWorld();
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final PlayerMoveEvent event, final SkillContext context) {
        final double distance = event.getTo().distance(event.getFrom());
        context.put("api-distance", distance);
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final PlayerMoveEvent event) {
        return event.getPlayer();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final PlayerMoveEvent event, final Settings settings) {
        return event.getPlayer();
    }
}
