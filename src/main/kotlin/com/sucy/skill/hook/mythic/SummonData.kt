package com.sucy.skill.hook.mythic

import org.bukkit.entity.LivingEntity
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
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

    fun getFirstTimer(): String {
        if (summon.isEmpty()) return "0"
        return max((summon.values.first().expireTimer - System.currentTimeMillis()) / 1000, 0).toString()
    }

    fun getAllSummon(func: (Summon) -> Boolean): Collection<Summon> {
        return summon.values.filter(func)
    }

    fun getSummon(livingEntity: LivingEntity): Summon? {
        return summon[livingEntity.uniqueId]
    }

    fun isSummon(livingEntity: LivingEntity): Boolean {
        return summon[livingEntity.uniqueId] != null
    }

    fun cleanupAll() {
        summon.values.forEach { it.safeRemove() }
        summon.clear()
        addQueue.forEach { it.safeRemove() }
        addQueue.clear()
    }
}