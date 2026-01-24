package com.sucy.skill.dynamic.condition

import com.rit.sucy.config.parse.DataSection
import com.sucy.skill.SkillAPI
import com.sucy.skill.dynamic.DynamicSkill
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import kotlin.math.max

class HealthScaleCondition : ConditionComponent() {

    /** true = 血量低于 scale; false = 高于 scale */
    private var much: Boolean = false

    /** 目标血量比例（0.0 ~ 1.0） */
    private var scale: Double = 0.0

    override fun getKey(): String {
        return "health scale"
    }

    override fun test(caster: LivingEntity, level: Int, target: LivingEntity): Boolean {
        much = settings.getBool("much", much)
        scale = parseValues(caster, "scale", level, 0.0)
        if (scale <= 0.0) return false
        if (!target.isValid || target.isDead) return false

        val max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).value
        val currentHealth = max(target.health, 1.0) / max

        return if (much) {
            currentHealth <= scale   // 血量低于 scale
        } else {
            currentHealth >= scale   // 血量高于 scale
        }

    }

}
