package com.sucy.skill.api.event

import com.sucy.skill.hook.mythic.Summon
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.event
 *
 * @author 老廖
 * @since 2025/11/23 03:59
 */
class MythicSummonDeathEvent(
    val summon: Summon
): Event() {

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }

    companion object {

        val handlers: HandlerList = HandlerList()

        fun getHandlerList(): HandlerList = handlers

    }

}