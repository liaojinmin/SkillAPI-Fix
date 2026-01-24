package com.sucy.skill.dynamic.mechanic.summon

import com.sucy.skill.hook.mythic.MythicManager
import com.sucy.skill.hook.mythic.SummonData
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Listener
import org.bukkit.util.Vector
import java.util.*


/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic.summon
 *
 * @author 老廖
 * @since 2025/10/27 02:20
 */
object MythicSummonManager: Listener {

    private var isStart: Boolean = false

    val UP: Vector = MythicManager.UP

    val api by lazy {
        MythicManager.api
    }

    val summonMap: MutableMap<UUID, SummonData>
        get() {
            return MythicManager.summonMap
        }

    /**
     * 用于外部索引召唤物的主人
     */
    val summonAscription: MutableMap<UUID, LivingEntity>
        get() {
            return MythicManager.summonAscription
        }

    fun isOwner(owner: UUID, target: LivingEntity): Boolean {
        val summon = summonMap[owner] ?: return false
        return summon.isSummon(target)
    }
}