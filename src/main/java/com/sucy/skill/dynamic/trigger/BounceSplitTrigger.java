package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.BounceProjectileSplitEvent;
import com.sucy.skill.api.projectile.BounceProjectile;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.TempEntity;
import org.bukkit.entity.LivingEntity;

/**
 * Triggered after a {@link BounceProjectile} creates its split children.
 *
 * <p>Settings:
 * <ul>
 *     <li>{@code generation} - required children generation; -1 (default) matches all</li>
 * </ul></p>
 *
 * <p>Values: {@code api-bounce-generation}, {@code api-bounce-count},
 * {@code api-bounce-location}.</p>
 */
public class BounceSplitTrigger implements Trigger<BounceProjectileSplitEvent> {

    private static final String GENERATION = "generation";

    private static final String VALUE_GENERATION = "api-bounce-generation";
    private static final String VALUE_COUNT = "api-bounce-count";
    private static final String VALUE_LOCATION = "api-bounce-location";

    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "BOUNCE_SPLIT";
    }

    /** {@inheritDoc} */
    @Override
    public Class<BounceProjectileSplitEvent> getEvent() {
        return BounceProjectileSplitEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(
            final BounceProjectileSplitEvent event,
            final int level,
            final Settings settings
    ) {
        final int required = (int) settings.getDouble(GENERATION, -1.0);
        return required < 0 || event.getGeneration() == required;
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(
            final BounceProjectileSplitEvent event,
            final SkillContext context,
            final boolean useGlobal
    ) {
        final LivingEntity caster = getCaster(event);
        if (caster == null) {
            return;
        }

        context.set(caster, VALUE_GENERATION, event.getGeneration(), useGlobal);
        context.set(caster, VALUE_COUNT, event.getChildren().size(), useGlobal);
        context.set(caster, VALUE_LOCATION, event.getLocation(), useGlobal);
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final BounceProjectileSplitEvent event) {
        return event.getProjectile() == null ? null : event.getProjectile().getShooter();
    }

    /** Splits have no single entity target, so they target the split position. */
    @Override
    public LivingEntity getTarget(final BounceProjectileSplitEvent event, final Settings settings) {
        return new TempEntity(event.getLocation());
    }
}
