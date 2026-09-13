package com.sucy.skill.hook.neonflash

import com.sucy.skill.SkillAPI
import me.neon.flash.attribute.Attribute
import me.neon.flash.attribute.AttributeType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.neonflash
 *
 * @author 老廖
 * @since 2026/8/28 11:45
 */
class ManaAttribute(
    override var priority: Int = 2,
    override val attributeName: String = "体力",
    override val attributeType: String = AttributeType.Other.name,
    override val placeholder: String = attributeName
): Attribute {

    override val parseHand: Boolean = true

    override fun init(livingEntity: LivingEntity, value: Double) {
        val player = livingEntity as? Player ?: return
        val playerData = SkillAPI.getPlayerData(player.uniqueId) ?: return
        playerData.maxMana = value
    }

    override fun update(livingEntity: LivingEntity, value: Double, old: Double) {
        init(livingEntity, value)
    }

    override fun delete(livingEntity: LivingEntity) {
        val player = livingEntity as? Player ?: return
        val playerData = SkillAPI.getPlayerData(player.uniqueId) ?: return
        playerData.maxMana = 0.0
    }
}