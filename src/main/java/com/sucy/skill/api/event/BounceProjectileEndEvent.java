package com.sucy.skill.api.event;

import com.sucy.skill.api.projectile.BounceProjectile;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called once when a bounce projectile ends for any reason.
 */
public class BounceProjectileEndEvent extends Event {

    public enum EndReason {
        BOUNCE_LIMIT,
        UNSUPPORTED_BLOCK,
        NO_REFLECTION,
        SPLIT,
        EXPIRED,
        UNLOADED_CHUNK,
        CANCELLED
    }

    private static final HandlerList handlers = new HandlerList();

    private final BounceProjectile projectile;
    private final EndReason reason;
    private final Location location;

    public BounceProjectileEndEvent(BounceProjectile projectile, EndReason reason) {
        this.projectile = projectile;
        this.reason = reason;
        this.location = projectile.getLocation().clone();
    }

    public BounceProjectile getProjectile() {
        return projectile;
    }

    public EndReason getReason() {
        return reason;
    }

    public Location getLocation() {
        return location.clone();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
