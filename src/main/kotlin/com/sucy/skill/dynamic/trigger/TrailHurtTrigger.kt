package com.sucy.skill.dynamic.trigger

import com.sucy.skill.api.Settings
import com.sucy.skill.api.event.TrailHurtEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class TrailHurtTrigger: Trigger<TrailHurtEvent> {

    override fun getKey(): String {
        return "TRAIL_HURT"
    }

    override fun getEvent(): Class<TrailHurtEvent> {
        return TrailHurtEvent::class.java
    }

    override fun getTarget(event: TrailHurtEvent, settings: Settings): LivingEntity {
        return event.target
    }

    override fun getCaster(event: TrailHurtEvent): LivingEntity {
        return event.trailEntity.livingEntity
    }

    override fun setValues(event: TrailHurtEvent, data: MutableMap<String, Any>) {
    }

    override fun shouldTrigger(event: TrailHurtEvent, level: Int, settings: Settings): Boolean {
        return event.trailEntity.effectName == settings.getString("effectName")
    }


}