package com.sucy.skill.api.event;

import com.sucy.skill.api.projectile.BounceProjectile;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

/**
 * Called when a bounce projectile collides with a block or living entity.
 */
public class BounceProjectileCollisionEvent extends Event {

    public enum CollisionType {
        BLOCK,
        ENTITY
    }

    private static final HandlerList handlers = new HandlerList();

    private final BounceProjectile projectile;
    private final CollisionType collisionType;
    private final Block block;
    private final LivingEntity target;
    private final Location location;
    private final Vector normal;
    private final Vector velocity;

    public BounceProjectileCollisionEvent(
            BounceProjectile projectile,
            Block block,
            Location location,
            Vector normal,
            Vector velocity
    ) {
        this.projectile = projectile;
        this.collisionType = CollisionType.BLOCK;
        this.block = block;
        this.target = null;
        this.location = location.clone();
        this.normal = normal == null ? null : normal.clone();
        this.velocity = velocity.clone();
    }

    public BounceProjectileCollisionEvent(
            BounceProjectile projectile,
            LivingEntity target,
            Location location,
            Vector velocity
    ) {
        this.projectile = projectile;
        this.collisionType = CollisionType.ENTITY;
        this.block = null;
        this.target = target;
        this.location = location.clone();
        this.normal = null;
        this.velocity = velocity.clone();
    }

    public BounceProjectile getProjectile() {
        return projectile;
    }

    public CollisionType getCollisionType() {
        return collisionType;
    }

    public boolean isBlockCollision() {
        return collisionType == CollisionType.BLOCK;
    }

    /** Returns the hit block, or null for an entity collision. */
    public Block getBlock() {
        return block;
    }

    /** Returns the hit entity, or null for a block collision. */
    public LivingEntity getTarget() {
        return target;
    }

    public Location getLocation() {
        return location.clone();
    }

    /** Returns the block contact normal, or null for an entity collision. */
    public Vector getNormal() {
        return normal == null ? null : normal.clone();
    }

    /** Returns the incoming velocity in blocks per tick. */
    public Vector getVelocity() {
        return velocity.clone();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
