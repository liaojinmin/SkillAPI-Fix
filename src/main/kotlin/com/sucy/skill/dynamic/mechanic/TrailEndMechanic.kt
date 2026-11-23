package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.api.skills.SkillContext
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
        context: SkillContext,
        caster: LivingEntity,
        level: Int,
        targets: MutableList<LivingEntity>,
    ): Boolean {
        targets.forEach {
            TrailManager.unregister(it)
        }
        return true
    }

}