package com.sucy.skill.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.event
 *
 * @author 老廖
 * @since 2025/11/29 16:47
 */
public class PlayerKillerEntityEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final LivingEntity livingEntity;

    @NotNull
    private final String class_;

    @NotNull
    private final String skill;
    
    public PlayerKillerEntityEvent(Player who, @NotNull LivingEntity target, @NotNull String classification, @NotNull String killSkill) {
        super(who);
        this.livingEntity = target;
        this.class_ = classification;
        this.skill = killSkill;
    }

    @NotNull
    public LivingEntity getTarget() {
        return livingEntity;
    }

    @NotNull
    public String getClass_() {
        return class_;
    }

    @NotNull
    public String getSkill() {
        return skill;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * @return gets the handlers for the event
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }


}
