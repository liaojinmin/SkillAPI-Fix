package com.sucy.skill.hook.mythic

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.event.MythicSummonDeathEvent
import com.sucy.skill.hook.mythic.ai.AttackAi2
import com.sucy.skill.hook.mythic.ai.WalkAi
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitEntity
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import me.neon.libs.taboolib.nms.ai.addGoalAi
import me.neon.libs.taboolib.nms.ai.clearGoalAi
import me.neon.libs.taboolib.nms.ai.clearTargetAi
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.UUID

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic.summon
 *
 * @author 老廖
 * @since 2025/10/23 21:43
 */
data class Summon(
    val owner: LivingEntity,
    val activeMob: ActiveMob,
    val expireTimer: Long
) {

    val unique: UUID = activeMob.uniqueId

    val spawnTimer: Long = System.currentTimeMillis()

    val ownerEntity: LivingEntity? = owner

    val bukkitEntity: LivingEntity = activeMob.entity.bukkitEntity as LivingEntity

    var targetEntity: LivingEntity? = null
        set(value) {
            if (value != null) {
                activeMob.setTarget(BukkitEntity(value))
            } else {
                activeMob.resetTarget()
            }
            field = value
        }

    /**
     * 改值不允许设置 false
     */
    var isDeath: Boolean = false
        private set(value) {
           if (!value) return
            Bukkit.getScheduler().runTask(SkillAPI.singleton()) {
                Bukkit.getPluginManager().callEvent(MythicSummonDeathEvent(this))
            }
            field = true
        }

    var isWorldChange: Boolean = false

    var disableTickTeleport: Boolean = true

    private var worldChangeTick: Int = 0

    fun initAi() {
        bukkitEntity.clearGoalAi()
        bukkitEntity.clearTargetAi()
        bukkitEntity.addGoalAi(WalkAi(owner, this), 2)
        bukkitEntity.addGoalAi(AttackAi2(owner, this), 1)
    }

    fun tick() {
        if (!disableTickTeleport) {
            val loc = owner.location.clone()
            if (isWorldChange
                || loc.world.name != bukkitEntity.world.name
                || bukkitEntity.location.distanceSquared(loc) > AttackAi2.ownerRange
            ) {
                bukkitEntity.teleport(loc.add(0.0, 1.0, 0.0), PlayerTeleportEvent.TeleportCause.PLUGIN)
            }
        }
        // 世界切换延迟移除
        if (isWorldChange) {
            if (worldChangeTick >= 5) {
                safeRemove()
            } else {
                worldChangeTick++
            }
        } else worldChangeTick = 0
        disableTickTeleport = false
    }

    fun checkTimerOut(): Boolean {
        if (bukkitEntity.isDead || !bukkitEntity.isValid) {
            isDeath = true
            return true
        }
        if (System.currentTimeMillis() >= expireTimer) {
            safeRemove()
            return true
        }
        return false
    }

    fun safeRemove() {
        if (!activeMob.entity.isDead && activeMob.entity.isValid) {
            isDeath = true
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