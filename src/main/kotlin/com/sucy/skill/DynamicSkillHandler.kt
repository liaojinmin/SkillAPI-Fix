package com.sucy.skill

import com.sucy.skill.api.event.SkillDamageEvent
import com.sucy.skill.api.event.SkillLinkEvent
import com.sucy.skill.api.skills.Skill
import com.sucy.skill.utils.ExpiringMap
import me.neon.libs.event.EventPriority
import me.neon.libs.event.SubscribeEvent
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.TimeUnit

/**
 * SkillAPI-Fix
 * com.sucy.skill
 *
 * @author 老廖
 * @since 2025/12/1 19:09
 */
object DynamicSkillHandler {

    data class Link(
        val skill: Skill,
        var clazz: String,
        var amount: Int
    )

    private val callMap: MutableMap<Int, ExpiringMap<String, Link>> = mutableMapOf()

    @SubscribeEvent(EventPriority.HIGHEST, ignoreCancelled = true)
    private fun ca(event: SkillDamageEvent) {
        val player = event.damager
        if (player is Player) {
            val map = callMap.computeIfAbsent(player.entityId) { ExpiringMap() }
            var old = map[event.skill.name]
            if (old != null) {
                old.amount++
                old.clazz = event.classification
            } else {
                old = Link(event.skill, event.classification, 1)
            }
            map.put(event.skill.name, old, 1, TimeUnit.SECONDS)
            val skillLinkEvent = SkillLinkEvent(player, event.target, old)
            skillLinkEvent.callEvent()
            if (skillLinkEvent.isTrigger) {
                map.remove(event.skill.name)
            }
        }
    }

    @SubscribeEvent(EventPriority.HIGHEST, ignoreCancelled = true)
    private fun quit(event: PlayerQuitEvent) {
        callMap.remove(event.player.entityId)?.clear()
    }



}