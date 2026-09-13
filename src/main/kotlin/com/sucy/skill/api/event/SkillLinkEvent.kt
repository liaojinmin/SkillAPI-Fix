package com.sucy.skill.api.event

import com.sucy.skill.DynamicSkillHandler
import com.sucy.skill.hook.mythic.Summon
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.event
 *
 * @author 老廖
 * @since 2025/11/23 03:59
 */
class SkillLinkEvent(
    player: Player,
    val target: LivingEntity,
    val link: DynamicSkillHandler.Link,
    val intervalMillis: Long  // 距上次同一 key 触发的毫秒数，首次为 -1
): PlayerEvent(player) {

    var isTrigger: Boolean = false

    override fun getHandlers(): HandlerList {
        return handlersList
    }

    companion object {

        private val handlersList: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlersList
        }

    }

}