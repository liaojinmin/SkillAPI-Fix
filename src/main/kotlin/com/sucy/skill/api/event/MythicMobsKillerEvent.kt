package com.sucy.skill.api.event

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.event
 *
 * @author 老廖
 * @since 2025/11/29 16:47
 */
class MythicMobsKillerEvent(
    killer: Player,
    private val originalKiller: LivingEntity?,
    val target: ActiveMob,
    val sourceEvent: MythicMobDeathEvent
): PlayerEvent(killer) {

    override fun getHandlers(): HandlerList {
        return _handlersList
    }

    fun isSummonKiller(): Boolean {
        if (originalKiller == null) return false
        return originalKiller.uniqueId != player.uniqueId
    }

    companion object {

        private val _handlersList: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return _handlersList
        }
    }
}
