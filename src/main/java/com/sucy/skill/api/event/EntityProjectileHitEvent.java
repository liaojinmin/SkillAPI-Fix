package com.sucy.skill.api.event;

import com.sucy.skill.api.projectile.CustomProjectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class EntityProjectileHitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final CustomProjectile projectile;

    private final LivingEntity hit;

    public EntityProjectileHitEvent(CustomProjectile projectile, LivingEntity hit) {
        this.projectile = projectile;
        this.hit = hit;
    }

    public CustomProjectile getProjectile() {
        return projectile;
    }

    public LivingEntity getHit() {
        return hit;
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
