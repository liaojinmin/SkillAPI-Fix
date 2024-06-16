package com.sucy.skill.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.event
 *
 * @author 老廖
 * @since 2024/6/8 20:00
 */
public class ManaRestoreTickEndEvent extends Event {

    private final Player player;

    public ManaRestoreTickEndEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
