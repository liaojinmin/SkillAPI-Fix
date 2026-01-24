package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.api.skills.SkillContext
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
class TrailStartMechanic: MechanicComponent() {

    override fun getKey(): String {
        return "trail start"
    }

    override fun execute(
        caster: LivingEntity,
        context: SkillContext,
        level: Int,
        targets: MutableList<LivingEntity>,
    ): Boolean {
        val effectName: String = settings.getString("effectName")!!
        val radius = parseValues(caster, "radius", level, 2.0)
        val duration = (parseValues(caster, "duration", level, 5.0) * 1000).toLong()
        val segmentDuration = (parseValues(caster, "segmentDuration", level, 5.0) * 1000).toLong()
        val effectTiming = (parseValues(caster, "effectTiming", level, 5.0) * 1000).toLong()
        val hitDuration = (parseValues(caster, "hitDuration", level, 5.0) * 20).toInt()
        targets.forEach {
            val trailEntity = TrailEntity(it, effectName, radius, duration, segmentDuration, effectTiming, hitDuration)
            TrailManager.register(trailEntity)
        }
        return true
    }

}