package com.sucy.skill.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.event
 *
 * @author 老廖
 * @since 2024/6/8 20:00
 */
public class ManaRestoreTickStartEvent extends Event implements Cancellable {

    private final Player player;

    private long timer;

    private boolean cancelled = false;

    public ManaRestoreTickStartEvent(Player player, long timer) {
        this.player = player;
        this.timer = timer;
    }

    public long getTimer() {
        return timer;
    }

    public Player getPlayer() {
        return player;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
