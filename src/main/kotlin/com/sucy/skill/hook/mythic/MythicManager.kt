package com.sucy.skill.hook.mythic

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.event.MythicMobsKillerEvent
import com.sucy.skill.api.event.SkillDamageAgentEvent
import com.sucy.skill.api.event.SkillDamageEvent
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
import me.geek.team.api.chemdah.AgentMythicMobDeathEvent
import me.geek.team.api.event.PlayerStartGameEvent
import me.neon.libs.util.getMeta
import me.neon.libs.util.getMetaFirst
import me.neon.libs.util.getMetaFirstOrNull
import me.neon.libs.util.setMeta
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
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
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap


/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic.summon
 *
 * @author 老廖
 * @since 2025/10/27 02:20
 */
object MythicManager: Listener {

    private var bukkitTask: BukkitTask? = null

    private var startClear: Boolean = false

    val UP: Vector = Vector(0, 1, 0)

    val api by lazy {
        MythicMobs.inst().mobManager
    }

    val summonMap: ConcurrentHashMap<UUID, SummonData> = ConcurrentHashMap()

    /**
     * 用于外部索引召唤物的主人
     * keu = entity
     * value = ~player
     */
    val summonAscription: MutableMap<UUID, LivingEntity> = mutableMapOf()

    private val selfKillerMeta: MutableMap<UUID, EntityDamageByEntityEvent> = mutableMapOf()

    fun isSummon(uuid: UUID): Boolean {
        return summonAscription.containsKey(uuid)
    }

    fun isOwner(owner: UUID, target: LivingEntity): Boolean {
        val summon = summonMap[owner] ?: return false
        return summon.isSummon(target)
    }

    fun trigger(event: SkillDamageEvent) {
        if (isSummon(event.damager.uniqueId)) {
            val owner = summonAscription[event.damager.uniqueId] ?: return
            val newEvent = SkillDamageAgentEvent(event.skill, owner, event.target, event.damage, event.classification, event.isRange)
            Bukkit.getPluginManager().callEvent(newEvent);
        }
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

        if (!startClear) {
            startClear = true
            Bukkit.getWorlds().forEach {
                it.livingEntities.toList().forEach { entity ->
                    // 防止以为未清理的实体
                    if (entity.type == EntityType.HUSK || entity.type == EntityType.IRON_GOLEM) {
                        entity.remove()
                    }
                }
            }
        }
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
        data.lock.set(true)
        Bukkit.getScheduler().runTaskLater(SkillAPI.singleton(), Runnable {
            data.cleanupAll()
        }, 20)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onDa2(event: EntityDamageByEntityEvent) {
        //println("EntityDamageByEntityEvent ${event.entity.name} da: ${event.damager.name}")
        if (event.damager.uniqueId != event.entity.uniqueId) {
            selfKillerMeta[event.entity.uniqueId] = event
        }
    }

    @EventHandler
    fun onDeath(event: MythicMobDeathEvent) {
        if (event is AgentMythicMobDeathEvent) return
          //println("MythicMobDeathEvent ${event.entity.name}")
        summonAscription.remove(event.entity.uniqueId)
        val meta = selfKillerMeta.remove(event.entity.uniqueId)

        var killer = event.killer
        if (killer == null) {
            val e = event.entity.lastDamageCause as? EntityDamageByEntityEvent
            if (e != null) {
                killer = e.damager as LivingEntity
            }
        }
        if (killer is Player) {
            MythicMobsKillerEvent(killer, killer, event.mob, event).callEvent()
        } else {

            var owner = killer?.getMetaFirstOrNull("SUMMON_OWNER")?.value()
            if (owner is Player) {
                MythicMobsKillerEvent(owner, killer, event.mob, event).callEvent()
            } else {
                // 降级通过攻击源记录，尝试判定是否是有源式的自残攻击
                meta ?: return
                if (meta.damager is Player) {
                    killer = meta.damager as Player
                    MythicMobsKillerEvent(killer, killer, event.mob, event).callEvent()
                } else {
                    // 索引所有者
                    owner = meta.damager.getMetaFirstOrNull("SUMMON_OWNER")?.value()
                    if (owner is Player) {
                        MythicMobsKillerEvent(owner, killer, event.mob, event).callEvent()
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun teleport(event: PlayerTeleportEvent) {
        if (event.from.world.name != event.to.world.name) return
        if (event.cause == PlayerTeleportEvent.TeleportCause.COMMAND || event.cause == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            val data = summonMap[event.player.uniqueId] ?: return
            data.lock.set(true)
            Bukkit.getScheduler().runTaskLater(SkillAPI.singleton(), Runnable {
                data.respawn(event.to) {
                    if (it.bukkitEntity.world.name == event.to.world.name) {
                        it.bukkitEntity.location.distanceSquared(event.to) >= 32.0 * 32.0
                    } else false
                }
                data.lock.set(false)
            }, 20)
        }
    }

    @EventHandler
    fun onDeath(event: EntityDeathEvent) {
        summonAscription.remove(event.entity.uniqueId)
        val data = summonMap.remove(event.entity.uniqueId) ?: return
        data.lock.set(true)
        data.cleanupAll()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onDa(event: EntityDamageByEntityEvent) {
        if (event.damager.hasMetadata("AttackAi2") && event.damage <= 1.0) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val data = summonMap.remove(event.player.uniqueId) ?: return
        data.lock.set(true)
        data.cleanupAll()
    }

    /**
     * 此方法会被异步调用
     */
    private fun tick() {
        summonMap.forEach { (key, value) ->
            try {
                if (!value.lock.get()) {
                    // 先删除过期
                    value.summon.values.removeIf {
                        it.tick()
                        it.checkTimerOut()
                    }

                    var v = value.addQueue.poll()
                    while (v != null) {
                        if (!v.checkTimerOut()) {
                            //  println("add")
                            value.summon[v.unique] = v
                        } else {
                            // println("添加失败")
                        }
                        v = value.addQueue.poll()
                    }
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