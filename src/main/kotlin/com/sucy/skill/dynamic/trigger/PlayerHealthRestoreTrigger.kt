package com.sucy.skill.dynamic.trigger

import com.sucy.skill.api.Settings
import com.sucy.skill.api.skills.SkillContext
import me.neon.core.event.HealthRestoreEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class PlayerHealthRestoreTrigger: Trigger<HealthRestoreEvent> {

    override fun getKey(): String {
        return "PLAYER_HEALTH_RESTORE"
    }

    override fun getEvent(): Class<HealthRestoreEvent> {
        return HealthRestoreEvent::class.java
    }

    override fun getTarget(event: HealthRestoreEvent, settings: Settings): LivingEntity {
        return event.player
    }

    override fun getCaster(event: HealthRestoreEvent): LivingEntity {
        return event.player
    }

    override fun setValues(event: HealthRestoreEvent, data: SkillContext, useGlobal: Boolean) {
        data.set(getCaster(event), key, event.amount.toDouble(), useGlobal)
    }

    override fun shouldTrigger(event: HealthRestoreEvent, level: Int, settings: Settings): Boolean {
        val minAmount = settings.getInt("minAmount", 1)
        val maxAmount = settings.getInt("maxAmount", 9999)
        return event.amount.toInt() in minAmount..maxAmount
    }


}