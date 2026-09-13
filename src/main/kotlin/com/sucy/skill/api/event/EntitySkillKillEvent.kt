package com.sucy.skill.api.event

import com.sucy.skill.api.skills.Skill
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.event
 *
 * @author 老廖
 * @since 2025/11/23 03:59
 */
class EntitySkillKillEvent(
    val attack: LivingEntity,
    val attackHandItem: ItemStack?,
    val target: LivingEntity,
    val skill: Skill,
    val classification: String,
    val tagTime: Long = System.currentTimeMillis()
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