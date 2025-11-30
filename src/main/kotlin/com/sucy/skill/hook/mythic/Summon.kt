package com.sucy.skill.hook.mythic

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.event.MythicSummonDeathEvent
import com.sucy.skill.hook.mythic.ai.AttackAi2
import com.sucy.skill.hook.mythic.ai.WalkAi
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import me.neon.libs.taboolib.nms.ai.addGoalAi
import me.neon.libs.taboolib.nms.ai.clearGoalAi
import me.neon.libs.taboolib.nms.ai.clearTargetAi
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import java.util.UUID

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic.summon
 *
 * @author 老廖
 * @since 2025/10/23 21:43
 */
data class Summon(
    val owner: UUID,
    val activeMob: ActiveMob,
    val expireTimer: Long
) {

    val unique: UUID = activeMob.uniqueId

    val spawnTimer: Long = System.currentTimeMillis()

    val ownerEntity: LivingEntity? = Bukkit.getPlayer(owner) ?: Bukkit.getEntity(owner) as? LivingEntity

    val bukkitEntity: LivingEntity = activeMob.entity.bukkitEntity as LivingEntity

    var targetEntity: LivingEntity? = null

    /**
     * 改值不允许设置 false
     */
    var isDeath: Boolean = false
        set(value) {
           if (field || !value) return
            callDeathEvent()
            field = true
        }

    private var evnetCall: Boolean = false

    fun initAi() {
        bukkitEntity.clearGoalAi()
        bukkitEntity.clearTargetAi()
        bukkitEntity.addGoalAi(WalkAi(owner, this), 10)
        bukkitEntity.addGoalAi(AttackAi2(owner, this), 11)
    }

    private fun callDeathEvent() {
        if (!evnetCall) {
            evnetCall = true
            Bukkit.getScheduler().runTask(SkillAPI.singleton()) {
                Bukkit.getPluginManager().callEvent(MythicSummonDeathEvent(this))
            }
        }
    }

    fun checkTimerOut(): Boolean {
        if (isDeath || bukkitEntity.isDead || !bukkitEntity.isValid) {
            callDeathEvent()
            return true
        }

        if (System.currentTimeMillis() >= expireTimer) {
            callDeathEvent()
            if (Bukkit.isPrimaryThread()) {
                activeMob.entity.bukkitEntity.remove()
                activeMob.entity.remove()
            } else {
                Bukkit.getScheduler().runTask(SkillAPI.singleton()) {
                    activeMob.entity.bukkitEntity.remove()
                    activeMob.entity.remove()
                }
            }
            return true
        }
        return false
    }

    fun safeRemove() {
        if (!activeMob.entity.isDead && activeMob.entity.isValid) {
            callDeathEvent()
            if (Bukkit.isPrimaryThread()) activeMob.entity.remove()
            else Bukkit.getScheduler().runTask(SkillAPI.singleton()) { activeMob.entity.remove() }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Summon) return false

        if (activeMob.uniqueId != other.activeMob.uniqueId) return false

        return true
    }

    override fun hashCode(): Int {
        return activeMob.uniqueId.hashCode()
    }


}