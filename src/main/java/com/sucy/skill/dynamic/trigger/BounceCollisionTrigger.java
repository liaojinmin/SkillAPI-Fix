package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.BounceProjectileCollisionEvent;
import com.sucy.skill.api.projectile.BounceProjectile;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.TempEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * Triggered when a {@link BounceProjectile} collides with a block or a living entity.
 *
 * <p>Settings:
 * <ul>
 *     <li>{@code type} - any (default), block or entity</li>
 *     <li>{@code generation} - required projectile generation; -1 (default) matches all</li>
 * </ul></p>
 *
 * <p>Values: {@code api-bounce-type}, {@code api-bounce-location},
 * {@code api-bounce-generation}, {@code api-bounce-block},
 * {@code api-bounce-target}, {@code api-bounce-normal-x/y/z}.</p>
 */
public class BounceCollisionTrigger implements Trigger<BounceProjectileCollisionEvent> {

    private static final String TYPE = "type";
    private static final String GENERATION = "generation";

    private static final String VALUE_TYPE = "api-bounce-type";
    private static final String VALUE_LOCATION = "api-bounce-location";
    private static final String VALUE_GENERATION = "api-bounce-generation";
    private static final String VALUE_BLOCK = "api-bounce-block";
    private static final String VALUE_TARGET = "api-bounce-target";
    private static final String VALUE_NORMAL_X = "api-bounce-normal-x";
    private static final String VALUE_NORMAL_Y = "api-bounce-normal-y";
    private static final String VALUE_NORMAL_Z = "api-bounce-normal-z";

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "BOUNCE_COLLISION";
    }

    /** {@inheritDoc} */
    @Override
    public Class<BounceProjectileCollisionEvent> getEvent() {
        return BounceProjectileCollisionEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(
            final BounceProjectileCollisionEvent event,
            final int level,
            final Settings settings
    ) {
        final BounceProjectile projectile = event.getProjectile();
        if (projectile == null || !matchesGeneration(projectile, settings)) {
            return false;
        }

        final String type = settings.getString(TYPE, "any");
        if (type == null || type.trim().isEmpty() || type.equalsIgnoreCase("any")) {
            return true;
        }
        if (type.equalsIgnoreCase("block")) {
            return event.isBlockCollision();
        }
        if (type.equalsIgnoreCase("entity")) {
            return !event.isBlockCollision();
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(
            final BounceProjectileCollisionEvent event,
            final SkillContext context,
            final boolean useGlobal
    ) {
        final LivingEntity caster = getCaster(event);
        if (caster == null) {
            return;
        }

        final BounceProjectile projectile = event.getProjectile();
        final boolean block = event.isBlockCollision();

        context.set(caster, VALUE_TYPE, block ? "BLOCK" : "ENTITY", useGlobal);
        context.set(caster, VALUE_LOCATION, event.getLocation(), useGlobal);
        context.set(caster, VALUE_GENERATION, projectile.getGeneration(), useGlobal);

        if (block) {
            if (event.getBlock() != null) {
                context.set(caster, VALUE_BLOCK, event.getBlock().getType().name(), useGlobal);
            }
            final Vector normal = event.getNormal();
            if (normal != null) {
                context.set(caster, VALUE_NORMAL_X, normal.getX(), useGlobal);
                context.set(caster, VALUE_NORMAL_Y, normal.getY(), useGlobal);
                context.set(caster, VALUE_NORMAL_Z, normal.getZ(), useGlobal);
            }
        } else if (event.getTarget() != null) {
            context.set(caster, VALUE_TARGET, event.getTarget().getName(), useGlobal);
        }
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final BounceProjectileCollisionEvent event) {
        return event.getProjectile() == null ? null : event.getProjectile().getShooter();
    }

    /**
     * Entity collisions target the hit entity; block collisions target a dummy
     * entity at the contact point so location based components still work.
     */
    @Override
    public LivingEntity getTarget(
            final BounceProjectileCollisionEvent event,
            final Settings settings
    ) {
        if (!event.isBlockCollision() && event.getTarget() != null) {
            return event.getTarget();
        }
        return new TempEntity(event.getLocation());
    }

    private boolean matchesGeneration(final BounceProjectile projectile, final Settings settings) {
        final int required = (int) settings.getDouble(GENERATION, -1.0);
        return required < 0 || projectile.getGeneration() == required;
    }
}
