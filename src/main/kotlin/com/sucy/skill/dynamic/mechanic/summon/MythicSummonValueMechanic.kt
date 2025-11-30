package com.sucy.skill.dynamic.mechanic.summon

import com.sucy.skill.api.event.value.ValueMechanicChangeEvent
import com.sucy.skill.api.skills.SkillContext
import com.sucy.skill.dynamic.DynamicSkill
import com.sucy.skill.dynamic.mechanic.MechanicComponent
import com.sucy.skill.hook.mythic.MythicManager
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity


/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2025/10/23 20:12
 */
class MythicSummonValueMechanic: MechanicComponent() {

    override fun getKey(): String {
        return "mythic summon value"
    }

    override fun execute(
        caster: LivingEntity,
        context: SkillContext,
        level: Int,
        targets: MutableList<LivingEntity>,
    ): Boolean {
        val type = settings.getString("mythicType")
        val key = settings.getString("key")

        targets.forEach { le ->
            val data = MythicManager.summonMap[le.uniqueId]
            if (data != null) {
                val valueData = DynamicSkill.getCastData(le)
                if (valueData != null) {
                    val value = data.getAllSummon { it.activeMob.type.entityType == type }.size
                    valueData[key] = value
                    Bukkit.getPluginManager().callEvent(
                        ValueMechanicChangeEvent(ValueMechanicChangeEvent.ValueAction.SET, caster, key, value.toDouble())
                    )
                }
            }
        }
        return true
    }

}