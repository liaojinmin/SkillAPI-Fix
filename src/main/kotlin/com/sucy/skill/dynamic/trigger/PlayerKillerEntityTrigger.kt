package com.sucy.skill.dynamic.trigger

import com.sucy.skill.api.Settings
import com.sucy.skill.api.event.PlayerKillerEntityEvent
import me.neon.core.event.HealthRestoreEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class PlayerKillerEntityTrigger: Trigger<PlayerKillerEntityEvent> {

    override fun getKey(): String {
        return "PLAYER_KILLER_ENTITY"
    }

    override fun getEvent(): Class<PlayerKillerEntityEvent> {
        return PlayerKillerEntityEvent::class.java
    }

    override fun getTarget(event: PlayerKillerEntityEvent, settings: Settings): LivingEntity {
        return event.target
    }

    override fun getCaster(event: PlayerKillerEntityEvent): LivingEntity {
        return event.player
    }

    override fun setValues(event: PlayerKillerEntityEvent, data: MutableMap<String, Any>) {
        data[key + "_classification"] = event.class_
        data[key + "_skill"] = event.skill
        data[key + "_isBehind"] = event.isBehind
    }

    override fun shouldTrigger(event: PlayerKillerEntityEvent, level: Int, settings: Settings): Boolean {

        val behind = settings.getBool("behind", false)
        if (behind && !event.isBehind) return false
     //   println("PlayerKillerEntityEvent")
     //   println("    击杀技能: ${event.skill}")
     //   println("    技能类别: ${event.class_}")
        val skill = settings.getStringList("skill")
        val classification = settings.getStringList("classification")
        var accept = false
        if (skill.isNotEmpty()) {
            for (sk in skill) {
                if (sk.isEmpty()) {
                 //   println("    SK技能匹配为空 > 通过")
                    accept = true
                    continue
                }
                if (sk.startsWith("!")) {
                    if (sk.removePrefix("!") == event.skill) accept = false
                } else {
                    if (sk == event.skill) {
                      //  println("    SK技能 $sk 匹配 > 通过")
                        accept = true
                    }
                }
                if (accept) {
                    break
                }

            }
        }
        if (classification.isNotEmpty()) {
            for (cl in classification) {
                if (cl.isEmpty()) {
                 //   println("    SK类别匹配为空 > 通过")
                    accept = true
                    continue
                }

                if (cl.startsWith("!")) {
                    if (cl.removePrefix("!") != event.class_) accept = false
                } else {
                    if (cl == event.class_) {
                      //  println("    SK类别 $cl 匹配 > 通过")
                        accept = true
                    }
                }
                if (accept) {
                    break
                }

            }
        }
        return accept
    }


}