package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * SkillAPI © 2018
 * com.sucy.trigger.dynamic.skill.BlockBreakTrigger
 */
public abstract class SkillTrigger<T extends SkillDamageEvent> implements Trigger<T> {

    private static final String PLAYER = "PLAYER";
    private static final String ENTITY = "ENTITY";
    private static final String ALL = "ALL";

    private final Class<T> eventClass;

    protected SkillTrigger(Class<T> eventClass) {
        this.eventClass = eventClass;
    }

    /** {@inheritDoc} */
    @Override
    public Class<T> getEvent() {
        return eventClass;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final T event, final int level, final Settings settings) {
        if (event.isCancelled()) return false;
        final double damage = event.getDamage();
        final double min = settings.getDouble("dmg-min");
        final double max = settings.getDouble("dmg-max");
        // 提前检查伤害范围，避免不必要的判断
        if (damage < min || damage > max) return false;

        final String damageType = settings.getString("damageType", ALL);
        if (PLAYER.equalsIgnoreCase(damageType) && !(event.getDamager() instanceof Player)) {
            return false;
        }
        if (ENTITY.equalsIgnoreCase(damageType) && event.getDamager() instanceof Player) {
            return false;
        }
        final String targetType = settings.getString("targetType", ALL);
        if (PLAYER.equalsIgnoreCase(targetType) && !(event.getTarget() instanceof Player)) {
            return false;
        }
        if (ENTITY.equalsIgnoreCase(targetType) && event.getTarget() instanceof Player) {
            return false;
        }
        final List<String> types = settings.getStringList("category");
        return (types.isEmpty() || types.get(0).isEmpty()) || types.contains(event.getClassification());
    }

    /**
     * Handles applying other effects after the skill resolves
     *
     * @param event event details
     * @param skill skill to resolve
     */
    @Override
    public void postProcess(final T event, final DynamicSkill skill) {
        final double damage = skill.applyImmediateBuff(event.getDamage());
        event.setDamage(damage);
    }

    boolean isUsingTarget(final Settings settings) {
        return settings.getString("target", "true").equalsIgnoreCase("false");
    }

}
