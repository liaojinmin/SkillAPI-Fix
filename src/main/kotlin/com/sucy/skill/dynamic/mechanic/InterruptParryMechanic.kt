package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.api.skills.SkillContext
import me.neon.core.NeonECore
import me.neon.core.parry.ParryManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2025/10/23 20:12
 */
class InterruptParryMechanic: MechanicComponent() {

    override fun getKey(): String {
        return "interrupt parry"
    }

    override fun execute(context: SkillContext, caster: LivingEntity, level: Int, targets: MutableList<LivingEntity>): Boolean {
        targets.forEach { target ->
            ParryManager.interrupt(target.uniqueId)
        }
        return true
    }

}