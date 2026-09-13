package com.sucy.skill.dynamic.trigger

import com.sucy.skill.CombatManager
import com.sucy.skill.api.Settings
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class CombatLeaveTrigger: Trigger<CombatManager.PlayerLeaveCombatEvent> {

    override fun getKey(): String {
        return "COMBAT_LEAVE"
    }

    override fun getEvent(): Class<CombatManager.PlayerLeaveCombatEvent> {
        return CombatManager.PlayerLeaveCombatEvent::class.java
    }

    override fun getTarget(event: CombatManager.PlayerLeaveCombatEvent, settings: Settings): LivingEntity {
        return event.player
    }

    override fun getCaster(event: CombatManager.PlayerLeaveCombatEvent): LivingEntity {
        return event.player
    }

    override fun setValues(event: CombatManager.PlayerLeaveCombatEvent, data: MutableMap<String, Any>) {
    }

    override fun shouldTrigger(event: CombatManager.PlayerLeaveCombatEvent, level: Int, settings: Settings): Boolean {
        return true
    }


}