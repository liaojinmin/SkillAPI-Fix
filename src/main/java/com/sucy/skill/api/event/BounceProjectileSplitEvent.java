package com.sucy.skill.api.event;

import com.sucy.skill.api.projectile.BounceProjectile;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Called after a bounce projectile creates its split children.
 */
public class BounceProjectileSplitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final BounceProjectile projectile;
    private final List<BounceProjectile> children;
    private final Location location;
    private final int generation;

    public BounceProjectileSplitEvent(
            BounceProjectile projectile,
            List<BounceProjectile> children,
            Location location,
            int generation
    ) {
        this.projectile = projectile;
        this.children = Collections.unmodifiableList(new ArrayList<>(children));
        this.location = location.clone();
        this.generation = generation;
    }

    /** Returns the projectile that split. */
    public BounceProjectile getProjectile() {
        return projectile;
    }

    /** Returns the newly created projectiles. The list cannot be structurally modified. */
    public List<BounceProjectile> getChildren() {
        return children;
    }

    public Location getLocation() {
        return location.clone();
    }

    /** Returns the generation of the children; the initial projectile is generation zero. */
    public int getGeneration() {
        return generation;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
