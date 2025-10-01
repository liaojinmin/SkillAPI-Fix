package com.sucy.skill.germ

import com.germ.germplugin.api.GermPacketAPI
import com.germ.germplugin.api.bean.AnimDataDTO
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * SkillAPI-Fix
 * com.sucy.skill.germ
 *
 * @author 老廖
 * @since 2025/9/19 19:40
 */
object GermPluginAPI {

    fun playAction(target: LivingEntity, action: String) {
        val viewer = target.world.getNearbyPlayers(target.location, 64.0)
        viewer.forEach {
            if (target is Player) {
                GermPacketAPI.sendBendAction(
                    it, target.getEntityId(), AnimDataDTO(action, 1.0f, false)
                )
            } else {
                GermPacketAPI.sendModelAnimation(
                    it, target.entityId, AnimDataDTO(action, 1.0f, false)
                )
            }
        }

    }
}