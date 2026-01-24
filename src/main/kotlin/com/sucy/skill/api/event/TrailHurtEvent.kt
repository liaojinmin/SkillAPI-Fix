package com.sucy.skill.api.event

import com.sucy.skill.api.event.SkillLinkEvent.Companion
import com.sucy.skill.trail.TrailEntity
import com.sucy.skill.trail.TrailSegment
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.event
 *
 * @author 老廖
 * @since 2025/11/23 03:59
 */
class TrailHurtEvent(
    val trailEntity: TrailEntity,
    val trailSegment: TrailSegment,
    val target: LivingEntity,
): Event() {

    override fun getHandlers(): HandlerList {
        return handlersList
    }

    companion object {

        val handlersList: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlersList

    }

}