package com.sucy.skill.api.event;

import com.sucy.skill.api.skills.Skill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event for when an entity is damaged by
 * another entity with the use of a skill.
 */
public class SkillDamageEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private LivingEntity damager;
    private LivingEntity target;
    private String classification;
    private Skill skill;
    private double       damage;
    private boolean range;
    private boolean      cancelled;

    public Throwable cancelStack; // 记录取消调用堆栈

    /**
     * Initializes a new event
     *
     * @param damager entity dealing the damage
     * @param target  entity receiving the damage
     * @param damage  the amount of damage dealt
     */
    public SkillDamageEvent(Skill skill, LivingEntity damager, LivingEntity target, double damage, String classification, boolean range) {
        this.skill = skill;
        this.damager = damager;
        this.target = target;
        this.damage = damage;
        this.classification = classification;
        this.cancelled = false;
    }

    /**
     * @return skill used to deal the damage
     */
    public Skill getSkill() {
        return skill;
    }


    public boolean isRange() {
        return range;
    }

    /**
     * Retrieves the entity that dealt the damage
     *
     * @return entity that dealt the damage
     */
    public LivingEntity getDamager()
    {
        return damager;
    }

    /**
     * Retrieves the entity that received the damage
     *
     * @return entity that received the damage
     */
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * Retrieves the amount of damage dealt
     *
     * @return amount of damage dealt
     */
    public double getDamage() {
        return damage;
    }

    public String getClassification() {
        return classification;
    }

    /**
     * Sets the amount of damage dealt
     *
     * @param amount amount of damage dealt
     */
    public void setDamage(double amount) {
        this.damage = amount;
    }

    /**
     * Checks whether or not the event is cancelled
     *
     * @return true if cancelled, false otherwise
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancelled state of the event
     *
     * @param cancelled the cancelled state of the event
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if (cancelled) {
            this.cancelStack = new Throwable("SkillDamageEvent 被取消的位置");
        }
    }

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
    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
