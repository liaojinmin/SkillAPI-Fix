package com.sucy.skill.dynamic.mechanic.summon

import com.sucy.skill.SkillAPI
import com.sucy.skill.hook.mythic.MythicManager
import com.sucy.skill.hook.mythic.SummonData
import com.sucy.skill.hook.mythic.mechanic.*
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
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
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