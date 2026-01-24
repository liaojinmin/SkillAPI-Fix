package com.sucy.skill.hook.mythic

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.event.value.ValueMechanicChangeEvent
import com.sucy.skill.dynamic.DynamicSkill
import com.sucy.skill.dynamic.mechanic.MythicHostilityMechanic.ActiveMobRec
import com.sucy.skill.dynamic.trigger.team.PlayerTeamStartTrigger
import com.sucy.skill.hook.mythic.condition.SkFactionCondition
import com.sucy.skill.hook.mythic.mechanic.*
import com.sucy.skill.utils.toFormatString
import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.api.bukkit.events.*
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import io.lumine.xikage.mythicmobs.mobs.MobManager
import io.lumine.xikage.mythicmobs.mobs.MythicMob
import io.lumine.xikage.mythicmobs.mobs.entities.SpawnReason
import io.lumine.xikage.mythicmobs.skills.SkillTrigger
import io.lumine.xikage.mythicmobs.skills.TriggeredSkill
import me.geek.team.api.event.PlayerStartGameEvent
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.*


/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic.summon
 *
 * @author 老廖
 * @since 2025/10/27 02:20
 */
object MythicManager: Listener {

    private var bukkitTask: BukkitTask? = null

    val UP: Vector = Vector(0, 1, 0)

    val api by lazy {
        MythicMobs.inst().mobManager
    }

    val summonMap: MutableMap<UUID, SummonData> = mutableMapOf()

    /**
     * 用于外部索引召唤物的主人
     */
    val summonAscription: MutableMap<UUID, LivingEntity> = mutableMapOf()

    fun isSummon(uuid: UUID): Boolean {
        return summonAscription.containsKey(uuid)
    }

    fun isOwner(owner: UUID, target: LivingEntity): Boolean {
        val summon = summonMap[owner] ?: return false
        return summon.isSummon(target)
    }

    fun onStart() {
        bukkitTask?.cancel()
        summonMap.clear()
        summonAscription.clear()
        try {
            // 替换 MythicMobs 的 EntityDamageByEntityEvent 监听器，让其可以攻击同派系生物
            val handlers = EntityDamageByEntityEvent.getHandlerList()
            for (listener in handlers.registeredListeners) {
                val clazz = listener.listener.javaClass.name
                //SkillAPI.singleton().logger.info("clazz: $clazz")
                // MythicMobs 的类名匹配
                if (clazz.startsWith("io.lumine.xikage.mythicmobs.utils.events.functional.single")) {
                    handlers.unregister(listener)
                    SkillAPI.singleton().logger.info("✅ MythicMobs 事件监听器已替换: ${CombatTriggerListener()}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bukkitTask = Bukkit.getScheduler().runTaskTimer(SkillAPI.singleton(), MythicManager::tick, 20, 20)
    }

    fun onShutdown() {
        summonMap.values.forEach { it.cleanupAll() }
        summonMap.clear()
        summonAscription.clear()
    }

    @EventHandler
    fun reload(event: MythicReloadedEvent) {
        Bukkit.getScheduler().runTaskLater(SkillAPI.singleton(), this::onShutdown, 20)
    }

    @EventHandler
    fun da(event: EntityTargetLivingEntityEvent) {
        val target = event.target as? Player ?: return
        val ac = api.getActiveMob(event.entity.uniqueId)
        if (ac.isPresent) {
            val activeMob = ac.get()
            if (activeMob.entity.getMetadata("excludePlayer") != null || activeMob.entity.getMetadata("MythicFactionMechanic") != null) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun load(event: MythicTargeterLoadEvent) {
        if (event.targeterName.equals("SkSummonOwner", true)) {
            event.register(SummonOwnerSelector(event.config))
        }
        if (event.targeterName.equals("SKSummonInRadius", true)) {
            event.register(SummonTargetSelector(event.config))

        }
    }

    @EventHandler
    fun load(event: MythicMechanicLoadEvent) {
        if (event.mechanicName.equals("SkSummonDamage", true)) {
            event.register(SummonDamageMechanic(event.container.configLine, event.config))
        }
        if (event.mechanicName.equals("SkSummonLock", true)) {
            event.register(SummonLookMechanic(event.container.configLine, event.config))
        }
    }

    @EventHandler
    fun load(event: MythicConditionLoadEvent) {
        if (event.conditionName.equals("summonTarget", true)) {
            event.register(SummonTargetCondition(event.config.line))
        }
        if (event.conditionName.equals("excludeFaction", true)) {
            event.register(SkFactionCondition(event.config.line, event.config))
        }
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
      //  println("PlayerChangedWorldEvent ${event.player.name}")
        val data = summonMap.remove(event.player.uniqueId) ?: return
        data.cleanupAll()
    }

    @EventHandler
    fun onDeath(event: MythicMobDeathEvent) {
      //  println("MythicMobDeathEvent ${event.entity.name}")
        summonAscription.remove(event.entity.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun teleport(event: EntityTeleportEvent) {
        val entity = event.entity as? LivingEntity ?: return
     //   println("EntityTeleportEvent ${entity.name} to ${event.to.toFormatString()} in ${event.isCancelled}")
    }

    @EventHandler
    fun onDeath(event: EntityDeathEvent) {
       // println("EntityDeathEvent ${event.entity.name}")
        summonAscription.remove(event.entity.uniqueId)
        val data = summonMap.remove(event.entity.uniqueId) ?: return
        data.cleanupAll()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDeath(event: EntityDamageByEntityEvent) {
        if (event.damager.hasMetadata("AttackAi2") && event.damage <= 1.0) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val data = summonMap.remove(event.player.uniqueId) ?: return
        data.cleanupAll()
    }

    /**
     * 此方法会被异步调用
     */
    private fun tick() {
        summonMap.forEach { (key, value) ->
            try {
                var update = false
                // 先删除过期
                value.summon.values.removeIf {
                    if (value.owner.world.name != it.activeMob.entity.world.name) {
                        if (!it.isWorldChange) it.isWorldChange = true
                    } else {
                        if (it.isWorldChange) it.isWorldChange = false
                    }
                    it.tick()
                    it.checkTimerOut().also {
                        if (it) {
                            update = true
                        }
                    }
                }

                var v = value.addQueue.poll()
                while (v != null) {
                    if (!v.checkTimerOut()) {
                        update = true
                        value.summon[v.unique] = v
                        //summonAscription[v.activeMob.uniqueId] = value.owner
                    }
                    v = value.addQueue.poll()
                }
                if (update) {
                    // 触发更新
                    value.updateAmountCache()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun MythicMob.spawn(location: AbstractLocation, level: Double, reason: SpawnReason, pre: (Entity) -> Unit, apply: (Entity) -> Unit): ActiveMob? {
        MobManager.spawnflag = true
        try {
            val e: Entity = BukkitAdapter.adapt(mythicEntity.spawn(location, reason))
            var am = ActiveMob(e.uniqueId, BukkitAdapter.adapt(e), this, 0.0)
            pre.invoke(e)
            val event = MythicMobSpawnEvent(am, level)
            Bukkit.getServer().pluginManager.callEvent(event)
            if (event.isCancelled) {
                e.remove()
                return null
            } else {
                am.level = event.mobLevel
                api.registerActiveMob(am)
                am = this.applyMobOptions(am, event.mobLevel)
                am = this.applyMobVolatileOptions(am)
                am = this.applySpawnModifiers(am)
                if (this.hasSkills(SkillTrigger.SPAWN)) {
                    TriggeredSkill(SkillTrigger.SPAWN, am, null)
                }
                apply.invoke(e)
                MobManager.spawnflag = false
                return am
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MobManager.spawnflag = false
            return null
        }
    }

    fun setNoClip(bukkitEntity: Entity, value: Boolean) {
        try {
            val nmsEntity: net.minecraft.server.v1_12_R1.Entity = (bukkitEntity as CraftEntity).handle
            nmsEntity.noclip = value
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}