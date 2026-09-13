package com.sucy.skill.dynamic.trigger

import com.sucy.skill.api.Settings
import me.neon.core.event.PlayerEndKneeEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI © 2018
 * com.sucy.trigger.dynamic.skill.BlockBreakTrigger
 */
class KneeEndTrigger : Trigger<PlayerEndKneeEvent> {
    /** {@inheritDoc}  */
    override fun getKey(): String {
        return "KNEE_END"
    }

    /** {@inheritDoc}  */
    override fun getEvent(): Class<PlayerEndKneeEvent> {
        return PlayerEndKneeEvent::class.java
    }

    /** {@inheritDoc}  */
    override fun shouldTrigger(event: PlayerEndKneeEvent, level: Int, settings: Settings): Boolean {
        return true
    }

    /** {@inheritDoc}  */
    override fun setValues(event: PlayerEndKneeEvent, data: Map<String, Any>) {}

    /** {@inheritDoc}  */
    override fun getCaster(event: PlayerEndKneeEvent): LivingEntity {
        return event.player
    }

    /** {@inheritDoc}  */
    override fun getTarget(event: PlayerEndKneeEvent, settings: Settings): LivingEntity {
        return event.player
    }
}
