package com.sucy.skill.dynamic.trigger

import com.sucy.skill.api.Settings
import me.neon.core.event.MagicPulseUseEvent
import me.neon.core.event.PlayerUseConsumeEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class PlayerUseConsumeTrigger: Trigger<PlayerUseConsumeEvent> {

    override fun getKey(): String {
        return "USE_CONSUME"
    }

    override fun getEvent(): Class<PlayerUseConsumeEvent> {
        return PlayerUseConsumeEvent::class.java
    }

    override fun getTarget(event: PlayerUseConsumeEvent, settings: Settings): LivingEntity {
        return event.player
    }

    override fun getCaster(event: PlayerUseConsumeEvent): LivingEntity {
        return event.player
    }

    override fun setValues(event: PlayerUseConsumeEvent, data: MutableMap<String, Any>) {
        data["consume_name"] = event.itemStack.itemMeta.displayName
    }

    override fun shouldTrigger(event: PlayerUseConsumeEvent, level: Int, settings: Settings): Boolean {
        return true
    }


}