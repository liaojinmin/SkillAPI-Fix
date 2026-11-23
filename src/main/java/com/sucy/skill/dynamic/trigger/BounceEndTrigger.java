package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.BounceProjectileEndEvent;
import com.sucy.skill.api.projectile.BounceProjectile;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.TempEntity;
import org.bukkit.entity.LivingEntity;

/**
 * Triggered once when a {@link BounceProjectile} ends for any reason.
 *
 * <p>Settings:
 * <ul>
 *     <li>{@code reason} - any (default) or one of {@code BOUNCE_LIMIT},
 *     {@code UNSUPPORTED_BLOCK}, {@code NO_REFLECTION}, {@code SPLIT},
 *     {@code EXPIRED}, {@code UNLOADED_CHUNK}, {@code CANCELLED}</li>
 * </ul></p>
 *
 * <p>Values: {@code api-bounce-reason}, {@code api-bounce-generation},
 * {@code api-bounce-location}.</p>
 */
public class BounceEndTrigger implements Trigger<BounceProjectileEndEvent> {

    private static final String REASON = "reason";

    private static final String VALUE_REASON = "api-bounce-reason";
    private static final String VALUE_GENERATION = "api-bounce-generation";
    private static final String VALUE_LOCATION = "api-bounce-location";

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "BOUNCE_END";
    }

    /** {@inheritDoc} */
    @Override
    public Class<BounceProjectileEndEvent> getEvent() {
        return BounceProjectileEndEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(
            final BounceProjectileEndEvent event,
            final int level,
            final Settings settings
    ) {
        final String reason = settings.getString(REASON, "any");
        return reason == null
                || reason.trim().isEmpty()
                || reason.equalsIgnoreCase("any")
                || reason.equalsIgnoreCase(event.getReason().name());
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(
            final BounceProjectileEndEvent event,
            final SkillContext context,
            final boolean useGlobal
    ) {
        final LivingEntity caster = getCaster(event);
        if (caster == null) {
            return;
        }

        context.set(caster, VALUE_REASON, event.getReason().name(), useGlobal);
        context.set(caster, VALUE_GENERATION, event.getProjectile().getGeneration(), useGlobal);
        context.set(caster, VALUE_LOCATION, event.getLocation(), useGlobal);
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final BounceProjectileEndEvent event) {
        return event.getProjectile() == null ? null : event.getProjectile().getShooter();
    }

    /** The projectile no longer exists, so the end position is the target. */
    @Override
    public LivingEntity getTarget(final BounceProjectileEndEvent event, final Settings settings) {
        return new TempEntity(event.getLocation());
    }
}
