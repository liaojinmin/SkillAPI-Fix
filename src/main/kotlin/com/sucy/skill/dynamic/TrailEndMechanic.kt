package com.sucy.skill.dynamic

import com.sucy.skill.api.skills.SkillContext
import com.sucy.skill.dynamic.mechanic.MechanicComponent
import com.sucy.skill.trail.TrailEntity
import com.sucy.skill.trail.TrailManager
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2025/10/23 20:12
 */
class TrailEndMechanic: MechanicComponent() {

    override fun getKey(): String {
        return "trail end"
    }

    override fun execute(
        caster: LivingEntity,
        context: SkillContext,
        level: Int,
        targets: MutableList<LivingEntity>,
    ): Boolean {
        targets.forEach {
            TrailManager.unregister(it)
        }
        return true
    }

}