package com.sucy.skill.api.event.value;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.event.value
 *
 * @author 老廖
 * @since 2024/2/26 20:35
 */
public class ValueMechanicChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();


    private final ValueAction action;
    private final LivingEntity caster;
    private final String key;
    private final double value;
    private boolean cancelled = false;


    public ValueMechanicChangeEvent(ValueAction action, LivingEntity caster, String key, double value) {
        this.action = action;
        this.caster = caster;
        this.key = key;
        this.value = value;
    }

    public ValueAction getAction() {
        return action;
    }

    public LivingEntity getCaster() {
        return caster;
    }

    public String getKey() {
        return key;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum ValueAction {
        ADD, SET, MULTIPLY
    }

}
