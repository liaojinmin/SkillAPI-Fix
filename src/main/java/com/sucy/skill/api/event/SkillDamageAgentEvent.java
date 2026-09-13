package com.sucy.skill.api.event;

import com.sucy.skill.api.skills.Skill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * An event for when an entity is damaged by
 * another entity with the use of a skill.
 */
public class SkillDamageAgentEvent extends SkillDamageEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public SkillDamageAgentEvent(Skill skill, LivingEntity damager, LivingEntity target, double damage, String classification, boolean range) {
        super(skill, damager, target, damage, classification, range);
    }

    /**

    /**
     * Retrieves the handlers for the event
     *
     * @return list of event handlers
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Retrieves the handlers for the event
     *
     * @return list of event handlers
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
