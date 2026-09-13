package com.sucy.skill

import me.neon.flash.api.event.AttributeChangeEvent
import me.neon.libs.event.SubscribeEvent

/**
 * SkillAPI-Fix
 * com.sucy.skill
 *
 * @author 老廖
 * @since 2026/1/30 00:30
 */
object Demo {

/*
    @SubscribeEvent(ignoreCancelled = true)
    fun onDemo(e: AttributeChangeEvent) {

        if (!e.attributePlayer.player.isOp) return
        val oldValue = e.oldData?.getSourceAttributeValue() ?: 0.0
        val newValue = e.newData?.getSourceAttributeValue() ?: 0.0

        val base = e.baseValue

        val delta = if (oldValue <= 0) {
            newValue
        } else {
            oldValue - newValue
        }

        val deltaRate = if (base != 0.0) delta / base * 100 else 0.0
        val totalRate = if (base != 0.0 && newValue > 0.0) newValue / base * 100 else 0.0

        val sign = if (delta >= 0) "+" else ""

        val msg = buildString {
            append("§7[属性变化] ")

            append("§f").append(e.attribute.attributeName).append(" ")

            append("§8(§7旧值: ")
            append(String.format("%.2f", oldValue))
            append(" §8→ §f新值: ")
            append(String.format("%.2f", newValue))
            append("§8) ")

            append("§a变化: ").append(sign)
                .append(String.format("%.2f", delta))

            append(" §7(§f本次: ")
            append(sign).append(String.format("%.1f", deltaRate))
            append("%")

            append(" §7| §f累计: ")
            append(String.format("%.1f", totalRate))
            append("%§7) ")

            append("§8来源: ").append(e.attributeContainer.resourceLocation)

            append(" ").append(" 动作: ").append(e.changeType)
        }

        e.attributePlayer.player.sendMessage(msg)
    }

 */


}