package com.sucy.skill.dynamic.trigger.team

import com.sucy.skill.api.Settings
import com.sucy.skill.api.event.MythicSummonDeathEvent
import com.sucy.skill.dynamic.trigger.Trigger
import me.geek.team.api.event.PlayerStartGameEvent
import me.neon.arena.api.event.PlayerStartArenaEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class PlayerStartArenaTrigger: Trigger<PlayerStartArenaEvent> {

    override fun getKey(): String {
        return "PLAYER_ARENA_START"
    }

    override fun getEvent(): Class<PlayerStartArenaEvent> {
        return PlayerStartArenaEvent::class.java
    }

    override fun getTarget(event: PlayerStartArenaEvent, settings: Settings): LivingEntity {
        return event.player
    }

    override fun getCaster(event: PlayerStartArenaEvent): LivingEntity {
        return event.player
    }

    override fun setValues(event: PlayerStartArenaEvent, data: MutableMap<String, Any>) {
    }

    override fun shouldTrigger(event: PlayerStartArenaEvent, level: Int, settings: Settings): Boolean {
        return true
    }


}