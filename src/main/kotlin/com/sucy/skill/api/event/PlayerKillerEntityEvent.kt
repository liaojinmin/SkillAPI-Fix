package com.sucy.skill.api.event

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
class PlayerKillerEntityEvent(
    who: Player?,
    val target: LivingEntity,
    val class_: String,
    val skill: String,
    val isBehind: Boolean
): PlayerEvent(who) {

    override fun getHandlers(): HandlerList {
        return handlersList
    }

    companion object {

        /**
         * @return gets the handlers for the event
         */

        private val handlersList: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlersList
        }
    }
}
