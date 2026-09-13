package com.sucy.skill.dynamic.trigger

import com.sucy.skill.api.Settings
import me.neon.core.event.PlayerKneeEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.PlayerDeathEvent

/**
 * SkillAPI © 2018
 * com.sucy.trigger.dynamic.skill.BlockBreakTrigger
 */
class KneeTrigger : Trigger<PlayerKneeEvent> {
    /** {@inheritDoc}  */
    override fun getKey(): String {
        return "KNEE"
    }

    /** {@inheritDoc}  */
    override fun getEvent(): Class<PlayerKneeEvent> {
        return PlayerKneeEvent::class.java
    }

    /** {@inheritDoc}  */
    override fun shouldTrigger(event: PlayerKneeEvent, level: Int, settings: Settings): Boolean {
        return true
    }

    /** {@inheritDoc}  */
    override fun setValues(event: PlayerKneeEvent, data: Map<String, Any>) {

    }

    /** {@inheritDoc}  */
    override fun getCaster(event: PlayerKneeEvent): LivingEntity {
        return event.player
    }

    /** {@inheritDoc}  */
    override fun getTarget(event: PlayerKneeEvent, settings: Settings): LivingEntity {
        return event.player
    }

}
