package com.sucy.skill.hook.mythic

import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic.summon
 *
 * @author 老廖
 * @since 2025/10/23 21:43
 */
data class SummonData(
    val owner: LivingEntity,
    internal val summon: ConcurrentHashMap<UUID, Summon> = ConcurrentHashMap(),
) {

    val addQueue: ConcurrentLinkedQueue<Summon> = ConcurrentLinkedQueue()

    val lock: AtomicBoolean = AtomicBoolean(false)

    private var amountCache: Map<String, Int> = emptyMap()

    fun getFirstTimer(): String {
        if (summon.isEmpty()) return "0"
        return max((summon.values.first().expireTimer - System.currentTimeMillis()) / 1000, 0).toString()
    }

    fun getAllSummon(func: (Summon) -> Boolean): Collection<Summon> {
        return summon.values.filter(func)
    }

    fun safeRemoveOf(type: String) {
        if (summon.isEmpty()) return
        summon.values.forEach {
            if (it.activeMob.mobType == type) {
                it.safeRemove()
            }
        }
    }

    fun getSummon(livingEntity: LivingEntity): Summon? {
        return summon[livingEntity.uniqueId]
    }

    fun isSummon(livingEntity: LivingEntity): Boolean {
        return summon[livingEntity.uniqueId] != null
    }

    fun getAmount(type: String): Int {
        return amountCache[type] ?: 0
    }

    fun updateAmountCache() {
        amountCache = summon.values.groupBy {
            it.activeMob.mobType
        }.map { it.key to it.value.size }.associate { it }
    }

    fun respawn(lco: Location, fi: (Summon) -> Boolean) {
       // summon.values.forEach { it.safeRemove() }
        summon.values.filter(fi).forEach {
            //val oldId = it.unique
            summon.remove(it.unique)
            MythicManager.summonAscription.remove(it.unique)
            it.safeRespawn(lco)
            summon[it.unique] = it

        }
    }

    fun cleanupAll() {
        addQueue.forEach { it.safeRemove() }
        addQueue.clear()
        summon.values.forEach { it.safeRemove() }
        summon.clear()
    }
}