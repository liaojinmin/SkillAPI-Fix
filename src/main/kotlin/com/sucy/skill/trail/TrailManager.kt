package com.sucy.skill.trail

import com.sucy.skill.SkillAPI
import me.neon.libs.util.Vector
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * SkillAPI-Fix
 * com.sucy.skill.trail
 *
 * @author 老廖
 * @since 2026/1/18 02:54
 */
object TrailManager: Listener {

    private var bukkitTask: BukkitTask? = null

    private val trailEntityMap: ConcurrentHashMap<Int, TrailEntity> = ConcurrentHashMap()

    const val EPS = 1e-2 // 0.01

    var demoTrailEntity: TrailEntity? = null

    fun Vector.approxEquals(other: Vector, eps: Double): Boolean {
        return abs(x - other.x) <= eps &&
                abs(y - other.y) <= eps &&
                abs(z - other.z) <= eps
    }


    fun register(trail: TrailEntity) {

        if (trailEntityMap.containsKey(trail.livingEntity.entityId)) return
    //    println("注册 ${trail.livingEntity.name}")
        trailEntityMap[trail.livingEntity.entityId] = trail
    }

    fun unregister(entity: LivingEntity) {
        trailEntityMap.remove(entity.entityId)
    }

    fun onStart() {
        bukkitTask?.cancel()
        demoTrailEntity = null
        bukkitTask = Bukkit.getScheduler()
            .runTaskTimerAsynchronously(SkillAPI.singleton(), this::tick, 1, 1)
    }

    fun onClose() {
        bukkitTask?.cancel()
        demoTrailEntity = null
        trailEntityMap.clear()
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        unregister(event.player)
    }

    @EventHandler
    fun re(event: PlayerChangedWorldEvent) {
        unregister(event.player)
    }

    private fun tick() {
        demoTrailEntity?.tick()
        val iterator = trailEntityMap.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val trailEntity = entry.value
            trailEntity.tick()
            if (!trailEntity.validCheck(false)) {
             //   println("过期")
                iterator.remove()
            }
        }
        //trailEntity.values.forEach(TrailEntity::tick)
    }

}