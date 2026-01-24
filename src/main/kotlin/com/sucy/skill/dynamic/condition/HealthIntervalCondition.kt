package com.sucy.skill.dynamic.condition

import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity

class HealthIntervalCondition : ConditionComponent() {

    override fun getKey(): String {
        return "health interval"
    }

    override fun test(caster: LivingEntity, level: Int, target: LivingEntity): Boolean {
        val min: Double = parseValues(caster,  "min", level, 1.0)
        val max: Double = parseValues(caster,  "max", level, 1.0)

        if (max <= 0.0 || min < 0.0 || min > max) return false

        if (caster.entityId == target.entityId) return false
        if (!caster.isValid || caster.isDead) return false
        if (!target.isValid || target.isDead) return false

        val targetRatio = target.health / (target.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0)
        val casterRatio = caster.health / (caster.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0)

        return (targetRatio - casterRatio) in min..max
    }

}
