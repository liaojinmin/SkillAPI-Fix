package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.skills.SkillContext
import com.sucy.skill.hook.mythic.MythicManager
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2025/10/23 20:12
 */
class MythicFactionMechanic: MechanicComponent() {

    override fun getKey(): String {
        return "mythic faction"
    }

    override fun execute(
        caster: LivingEntity,
        context: SkillContext,
        level: Int,
        targets: MutableList<LivingEntity>,
    ): Boolean {
        val faction = settings.getString("faction")!!
        val duration = parseValues(caster, "duration", level, 5.0) * 20
        val mobs = targets.mapNotNull {
            val ac = MythicManager.api.getActiveMob(it.uniqueId)
            if (ac.isPresent) {
                ac.get() to ac.get().faction
            } else null
        }
        if (mobs.isEmpty()) return false

        mobs.forEach {
            val ac = it.first
            val bukkit = ac.entity.bukkitEntity
            if (bukkit.isValid && !bukkit.isDead) {
                ac.entity.setMetadata("MythicFactionMechanic", true)
                ac.setFaction(faction)
                ac.resetTarget()
            }
        }
        Bukkit.getScheduler().runTaskLater(SkillAPI.singleton(), Runnable {
            mobs.forEach {
                val ac = it.first
                val bukkit = ac.entity.bukkitEntity
                if (bukkit.isValid && !bukkit.isDead) {
                    ac.entity.removeMetadata("MythicFactionMechanic")
                    ac.setFaction(it.second)
                    ac.resetTarget()
                }

            }
        }, duration.toLong())

        return true
    }

}