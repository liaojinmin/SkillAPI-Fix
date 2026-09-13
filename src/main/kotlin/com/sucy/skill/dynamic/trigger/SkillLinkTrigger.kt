package com.sucy.skill.dynamic.trigger

import com.sucy.skill.api.Settings
import com.sucy.skill.api.event.SkillLinkEvent
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.trigger.summon
 *
 * @author 老廖
 * @since 2025/11/23 04:02
 */
class SkillLinkTrigger: Trigger<SkillLinkEvent> {

    override fun getKey(): String {
        return "SKILL_LINK"
    }

    override fun getEvent(): Class<SkillLinkEvent> {
        return SkillLinkEvent::class.java
    }

    override fun getTarget(event: SkillLinkEvent, settings: Settings): LivingEntity {
        return event.target
    }

    override fun getCaster(event: SkillLinkEvent): LivingEntity {
        return event.player
    }

    override fun setValues(event: SkillLinkEvent, data: MutableMap<String, Any>) {
        data[key + "_amount"] = event.link.amount
        data[key + "_skill"] = event.link.skill.name
        data[key + "_classification"] = event.link.clazz
        data[key + "_interval"] = event.intervalMillis
    }

    override fun shouldTrigger(event: SkillLinkEvent, level: Int, settings: Settings): Boolean {
        try {

            //    println("SkillLinkTrigger")
            val eqAmount = settings.getInt("amount", 0)
            if (eqAmount <= 0) return false
            val eqInterval = settings.getInt("interval", 300)
            if (eqInterval < event.intervalMillis) return false

            //   println("    eqAmount: $eqAmount now: " + event.link.amount)
            if (event.link.amount == eqAmount) {
                val eqSkill = settings.getStringList("skill")
                val eqClassification = settings.getStringList("classification")

                var accept = false
                if (eqSkill.isNotEmpty()) {
                    for (sk in eqSkill) {
                        if (sk.isEmpty()) {
                            accept = true
                            continue
                        }
                        if (sk.startsWith("!")) {
                            if (sk.removePrefix("!") == event.link.skill.name) accept = false
                        } else {
                            if (sk == event.link.skill.name) accept = true
                        }
                        if (accept) {
                            break
                        }
                    }
                }
                if (eqClassification.isNotEmpty()) {
                    for (cl in eqClassification) {
                        if (cl.isEmpty()) {
                            accept = true
                            continue
                        }
                        if (cl.startsWith("!")) {
                            if (cl.removePrefix("!") == event.link.clazz) accept = false
                        } else {
                            if (cl == event.link.clazz) accept = true
                        }
                        if (accept) {
                            break
                        }

                    }
                }

                //  println("    accept: $accept")
                if (accept) {

                    event.isTrigger = true
                }
                return accept
            }
        } catch (e: Exception) {
            println("异常: ${e.message}")
            e.printStackTrace()
        }
        return false
    }


}