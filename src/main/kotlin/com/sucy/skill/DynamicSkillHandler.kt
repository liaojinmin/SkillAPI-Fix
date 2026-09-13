package com.sucy.skill

import com.sucy.skill.api.event.SkillDamageEvent
import com.sucy.skill.api.event.SkillLinkEvent
import com.sucy.skill.api.skills.Skill
import com.sucy.skill.utils.ExpiringMap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.TimeUnit

/**
 * SkillAPI-Fix
 * com.sucy.skill
 *
 * @author 老廖
 * @since 2025/12/1 19:09
 */
object DynamicSkillHandler: Listener {

    data class Link(
        val skill: Skill,
        var clazz: String,
        var amount: Int,
        var lastTriggerTime: Long = System.currentTimeMillis()  // 新增
    )

    private val callMap: MutableMap<Int, ExpiringMap<String, Link>> = mutableMapOf()

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun ca(event: SkillDamageEvent) {
        val player = event.damager
        if (player !is Player) return

        val map = callMap.computeIfAbsent(player.entityId) { ExpiringMap() }
        // 关键改动：组合技能名与伤害分类作为 key
        val key = "${event.skill.name}#${event.classification}"
        val now = System.currentTimeMillis()

        val old = map[key]
        val interval = if (old != null) now - old.lastTriggerTime else -1L  // 首次为 -1

        val link = if (old != null) {
            old.amount++
            old.clazz = event.classification
            old.lastTriggerTime = now
            old
        } else {
            Link(event.skill, event.classification, 1, now)
        }

        // 每次触发都刷新过期时间（1秒内连续触发则保持活跃）
        map.put(key, link, 1, TimeUnit.SECONDS)

        val skillLinkEvent = SkillLinkEvent(player, event.target, link, interval)
        skillLinkEvent.callEvent()

        if (skillLinkEvent.isTrigger) {
            map.remove(key)  // 监听器决定重置连击
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun quit(event: PlayerQuitEvent) {
        callMap.remove(event.player.entityId)?.clear()
    }



}