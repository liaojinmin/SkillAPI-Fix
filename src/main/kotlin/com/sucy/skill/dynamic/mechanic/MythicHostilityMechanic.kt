package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.skills.SkillContext
import com.sucy.skill.hook.mythic.MythicManager
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2025/10/23 20:12
 */
class MythicHostilityMechanic: MechanicComponent() {

    data class ActiveMobRec(
        val activeMob: ActiveMob,
        val oldFaction: String
    )

    override fun getKey(): String {
        return "mythic hostility"
    }

    override fun execute(
        caster: LivingEntity,
        context: SkillContext,
        level: Int,
        targets: MutableList<LivingEntity>,
    ): Boolean {
        val faction = settings.getString("faction")!!
        val filterPlayer: Boolean = settings.getBool("excludePlayer")
        val duration = parseValues(caster, "duration", level, 5.0) * 20

        val mobs: List<ActiveMobRec> = targets.mapNotNull {
            val ac = MythicManager.api.getActiveMob(it.uniqueId)
            if (ac.isPresent) {
               ActiveMobRec(ac.get(), ac.get().faction)
            } else null
        }
        if (mobs.isEmpty()) return false

        mobs.forEach {
            val bukkit = it.activeMob.entity.bukkitEntity
            if (bukkit.isValid) {
                it.activeMob.entity.setMetadata("excludePlayer", filterPlayer)
                it.activeMob.setFaction(faction)
                it.activeMob.resetTarget()
                it.activeMob.setTarget(null)
            }
        }
        Bukkit.getScheduler().runTaskLater(SkillAPI.singleton(), Runnable {
            mobs.forEach {
                val bukkit = it.activeMob.entity.bukkitEntity
                if (!bukkit.isDead) {
                    it.activeMob.setFaction(it.oldFaction)
                    it.activeMob.resetTarget()
                    it.activeMob.setTarget(null)
                    it.activeMob.entity.removeMetadata("excludePlayer")
                }
            }
        }, duration.toLong())

        return true
    }

}