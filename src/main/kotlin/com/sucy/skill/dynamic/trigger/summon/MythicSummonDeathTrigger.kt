package com.sucy.skill.dynamic.trigger.summon

import com.sucy.skill.api.Settings
import com.sucy.skill.api.event.MythicSummonDeathEvent
import com.sucy.skill.dynamic.trigger.Trigger
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class MythicSummonDeathTrigger: Trigger<MythicSummonDeathEvent> {

    override fun getKey(): String {
        return "MYTHIC_SUMMON_DEATH"
    }

    override fun getEvent(): Class<MythicSummonDeathEvent> {
        return MythicSummonDeathEvent::class.java
    }

    override fun getTarget(event: MythicSummonDeathEvent, settings: Settings): LivingEntity {
        return event.summon.bukkitEntity
    }

    override fun getCaster(event: MythicSummonDeathEvent): LivingEntity {
        return event.summon.owner
    }

    override fun setValues(event: MythicSummonDeathEvent, data: MutableMap<String, Any>) {

    }

    override fun shouldTrigger(event: MythicSummonDeathEvent, level: Int, settings: Settings): Boolean {
        val types = settings.getStringList("mythicType")
        if (types.isEmpty()) return true
        val type= event.summon.activeMob.mobType
        return types.contains(type)
    }


}