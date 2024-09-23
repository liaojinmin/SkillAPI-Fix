package com.sucy.skill.api.event;

import com.sucy.skill.api.projectile.CustomProjectile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class EntityProjectileExpireEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final CustomProjectile projectile;

    public EntityProjectileExpireEvent(CustomProjectile projectile) {
        this.projectile = projectile;
    }

    public CustomProjectile getProjectile() {
        return projectile;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }


    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
