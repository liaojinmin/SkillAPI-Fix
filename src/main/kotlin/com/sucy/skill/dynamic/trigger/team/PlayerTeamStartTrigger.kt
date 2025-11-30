package com.sucy.skill.dynamic.trigger.team

import com.sucy.skill.api.Settings
import com.sucy.skill.api.event.MythicSummonDeathEvent
import com.sucy.skill.dynamic.trigger.Trigger
import me.geek.team.api.event.PlayerStartGameEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class PlayerTeamStartTrigger: Trigger<PlayerStartGameEvent> {

    override fun getKey(): String {
        return "PLAYER_TEAM_START"
    }

    override fun getEvent(): Class<PlayerStartGameEvent> {
        return PlayerStartGameEvent::class.java
    }

    override fun getTarget(event: PlayerStartGameEvent, settings: Settings): LivingEntity {
        return event.player
    }

    override fun getCaster(event: PlayerStartGameEvent): LivingEntity {
        return event.player
    }

    override fun setValues(event: PlayerStartGameEvent, data: MutableMap<String, Any>) {
    }

    override fun shouldTrigger(event: PlayerStartGameEvent, level: Int, settings: Settings): Boolean {
        val types = settings.getStringList("dungeon")
        val difficulty = settings.getStringList("difficulty")
        if (types.isEmpty()) return true
        if (types.contains(event.teamHandler.dungeon.dungeonName)) {
            if (difficulty.isNotEmpty()) {
                return difficulty.contains(event.teamHandler.dungeon.difficulty)
            }
            return true
        }
        return false
    }


}