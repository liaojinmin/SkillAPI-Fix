package com.sucy.skill.dynamic.mechanic.summon

import com.sucy.skill.api.skills.SkillContext
import com.sucy.skill.dynamic.mechanic.MechanicComponent
import com.sucy.skill.hook.mythic.MythicManager
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
        context: SkillContext,
        caster: LivingEntity,
        level: Int,
        targets: MutableList<LivingEntity>,
    ): Boolean {
        val type = settings.getString("mythicType")!!
        targets.forEach { le ->
            MythicManager.summonMap[le.uniqueId]?.safeRemoveOf(type)
        }
        return true
    }

}