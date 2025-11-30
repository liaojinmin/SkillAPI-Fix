package com.sucy.skill.dynamic.trigger

import com.sucy.skill.api.Settings
import com.sucy.skill.api.event.PlayerKillerEntityEvent
import me.neon.core.event.HealthRestoreEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class PlayerKillerEntityTrigger: Trigger<PlayerKillerEntityEvent> {

    override fun getKey(): String {
        return "PLAYER_KILLER_ENTITY"
    }

    override fun getEvent(): Class<PlayerKillerEntityEvent> {
        return PlayerKillerEntityEvent::class.java
    }

    override fun getTarget(event: PlayerKillerEntityEvent, settings: Settings): LivingEntity {
        return event.target
    }

    override fun getCaster(event: PlayerKillerEntityEvent): LivingEntity {
        return event.player
    }

    override fun setValues(event: PlayerKillerEntityEvent, data: MutableMap<String, Any>) {
        data[key + "_classification"] = event.class_
        data[key + "_skill"] = event.skill
    }

    override fun shouldTrigger(event: PlayerKillerEntityEvent, level: Int, settings: Settings): Boolean {
        val skill = settings.getString("skill", "")
        val classification = settings.getString("classification", "")
        return (skill.isEmpty() || skill == event.skill)
                && (classification.isEmpty() || classification == event.class_)
    }


}