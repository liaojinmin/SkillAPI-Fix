package com.sucy.skill.dynamic.mechanic.summon

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.attribute.mob.MobAttribute
import com.sucy.skill.api.attribute.mob.MobAttributeData
import com.sucy.skill.api.player.PlayerData
import com.sucy.skill.api.skills.SkillContext
import com.sucy.skill.dynamic.mechanic.MechanicComponent
import com.sucy.skill.hook.mythic.MythicManager
import com.sucy.skill.hook.mythic.MythicManager.spawn
import com.sucy.skill.hook.mythic.Summon
import com.sucy.skill.hook.mythic.SummonData
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.mobs.entities.SpawnReason
import org.bukkit.entity.LivingEntity


/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2025/10/23 20:12
 */
class MythicSummonDeleteMechanic: MechanicComponent() {

    override fun getKey(): String {
        return "mythic summon delete"
    }

    override fun execute(
        caster: LivingEntity,
        context: SkillContext,
        level: Int,
        targets: MutableList<LivingEntity>,
    ): Boolean {
        val type = settings.getString("mythicType")!!
        targets.forEach { le ->
            val data = MythicManager.summonMap[le.uniqueId]
            data?.getAllSummon { it.activeMob.type.entityType == type }?.forEach { it.isDeath = true }
        }
        return true
    }

}