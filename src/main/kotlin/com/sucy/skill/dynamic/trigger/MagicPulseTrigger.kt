package com.sucy.skill.dynamic.trigger

import com.sucy.skill.api.Settings
import me.neon.core.event.MagicPulseUseEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class MagicPulseTrigger: Trigger<MagicPulseUseEvent> {

    override fun getKey(): String {
        return "MAGIC_PULSE"
    }

    override fun getEvent(): Class<MagicPulseUseEvent> {
        return MagicPulseUseEvent::class.java
    }

    override fun getTarget(event: MagicPulseUseEvent, settings: Settings): LivingEntity {
        return event.player
    }

    override fun getCaster(event: MagicPulseUseEvent): LivingEntity {
        return event.player
    }

    override fun setValues(event: MagicPulseUseEvent, data: MutableMap<String, Any>) {
    }

    override fun shouldTrigger(event: MagicPulseUseEvent, level: Int, settings: Settings): Boolean {
        val minAmount = settings.getInt("minAmount", 1)
        val maxAmount = settings.getInt("maxAmount", 9999)
        return event.amount in minAmount..maxAmount
    }


}