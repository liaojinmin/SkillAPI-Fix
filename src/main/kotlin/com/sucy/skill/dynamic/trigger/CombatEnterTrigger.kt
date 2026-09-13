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
class CombatEnterTrigger: Trigger<CombatManager.PlayerEnterCombatEvent> {

    override fun getKey(): String {
        return "COMBAT_ENTER"
    }

    override fun getEvent(): Class<CombatManager.PlayerEnterCombatEvent> {
        return CombatManager.PlayerEnterCombatEvent::class.java
    }

    override fun getTarget(event: CombatManager.PlayerEnterCombatEvent, settings: Settings): LivingEntity {
        return event.player
    }

    override fun getCaster(event: CombatManager.PlayerEnterCombatEvent): LivingEntity {
        return event.player
    }

    override fun setValues(event: CombatManager.PlayerEnterCombatEvent, data: MutableMap<String, Any>) {
    }

    override fun shouldTrigger(event: CombatManager.PlayerEnterCombatEvent, level: Int, settings: Settings): Boolean {
        return true
    }


}