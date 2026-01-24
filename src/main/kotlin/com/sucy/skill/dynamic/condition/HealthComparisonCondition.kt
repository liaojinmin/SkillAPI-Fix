package com.sucy.skill.dynamic.condition

import com.rit.sucy.config.parse.DataSection
import com.sucy.skill.dynamic.DynamicSkill
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import kotlin.math.max

class HealthComparisonCondition : ConditionComponent() {

    /** 比较方向：higher / lower */
    private var direction: String = "higher"

    /** 比例阈值，例如 1.2 表示目标比施法者高 20% */
    private var scale: Double = 1.0

    override fun getKey(): String {
        return "health comparison"
    }

    override fun test(caster: LivingEntity, level: Int, target: LivingEntity): Boolean {
        direction = settings.getString("direction", "higher").lowercase()
        scale = parseValues(caster,  "scale", level, 1.0)
        if (scale <= 0.0) return false
        if (caster.entityId == target.entityId) return false
        if (!caster.isValid || caster.isDead) return false
        if (!target.isValid || target.isDead) return false

        val casterMax = caster.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
        val targetMax = target.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0

        val casterRatio = max(caster.health, 1.0) / casterMax
        val targetRatio = max(target.health, 1.0) / targetMax

        return when (direction) {
            "higher" -> targetRatio >= casterRatio * (1.0 + scale)  // 高于 20%
            "lower" -> targetRatio <= casterRatio * (1.0 - scale)   // 低于 20%
            else -> false
        }
    }

}
