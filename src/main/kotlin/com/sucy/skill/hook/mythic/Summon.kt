package com.sucy.skill.hook.mythic

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.attribute.mob.MobAttribute
import com.sucy.skill.api.attribute.mob.MobAttributeData
import com.sucy.skill.api.event.MythicSummonDeathEvent
import com.sucy.skill.api.player.PlayerData
import com.sucy.skill.hook.mythic.MythicManager.spawn
import com.sucy.skill.hook.mythic.ai.AttackAi2
import com.sucy.skill.hook.mythic.ai.WalkAi
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitEntity
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import io.lumine.xikage.mythicmobs.mobs.MythicMob
import io.lumine.xikage.mythicmobs.mobs.entities.SpawnReason
import me.neon.libs.taboolib.nms.ai.addGoalAi
import me.neon.libs.taboolib.nms.ai.clearGoalAi
import me.neon.libs.taboolib.nms.ai.clearTargetAi
import me.neon.libs.util.setMeta
import org.bukkit.Bukkit
import org.bukkit.Location
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
    private var location: Location,
    private val mythicMob: MythicMob,
    private val useAI: Boolean,
    private val health: Double,
    private val duration: Double
) {

    val spawnTimer: Long = System.currentTimeMillis()

    val expireTimer: Long = spawnTimer + duration.toLong()

    lateinit var activeMob: ActiveMob

    val unique: UUID
        get() {
            return activeMob.uniqueId
        }

    val bukkitEntity: LivingEntity
        get() {
            return activeMob.entity.bukkitEntity as LivingEntity
        }

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

    private var isInit: Boolean = false

    @Volatile
    private var lock: Boolean = false

    init {
        spawn()
        bukkitEntity.setMeta("SUMMON_OWNER", owner)
        isInit = true
    }

    fun tick() {
        if (!isInit || lock) return
        if (!bukkitEntity.hasAI()) {
            if (!activeMob.mobType.contains("火焰喷射器")) {
                val loc = owner.location.clone()
                if (loc.world.name != bukkitEntity.world.name
                    || bukkitEntity.location.distanceSquared(loc) > 1024
                ) {
                    bukkitEntity.teleport(loc.add(0.0, 1.0, 0.0), PlayerTeleportEvent.TeleportCause.PLUGIN)
                }
            }
        }
    }

    fun checkTimerOut(): Boolean {
        if (!isInit && !lock) return true
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
        if (!isInit) return
        if (!activeMob.entity.isDead && activeMob.entity.isValid) {
            isDeath = true
            if (Bukkit.isPrimaryThread()) activeMob.entity.remove()
            else Bukkit.getScheduler().runTask(SkillAPI.singleton()) { activeMob.entity.remove() }
        }
    }

    fun safeRespawn(loc: Location) {
        if (!isInit) return
        val func = {
            if (!activeMob.entity.isDead && activeMob.entity.isValid) {
                activeMob.entity.remove()
            }
            location = loc.clone()
            spawn()
        }

        if (Bukkit.isPrimaryThread()) func.invoke()
        else Bukkit.getScheduler().runTask(SkillAPI.singleton()) { func.invoke() }

    }

    private fun spawn() {

        lock = true
        mythicMob.spawn(
            BukkitAdapter.adapt(location),
            1.0,
            SpawnReason.OTHER,
            pre = {
                if (it is LivingEntity) {
                    MythicManager.summonAscription[it.uniqueId] = owner
                }
            },
            apply = {
                if (it is LivingEntity) {
                    it.maxHealth = health
                    it.health = health
                }
            }
        )?.let {
            activeMob = it
            bukkitEntity.clearGoalAi()
            bukkitEntity.clearTargetAi()
            if (useAI) {
                bukkitEntity.addGoalAi(WalkAi(owner, this), 2)
                bukkitEntity.addGoalAi(AttackAi2(owner, this), 1)
            }
        }
        lock = false
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