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
        context: SkillContext,
        caster: LivingEntity,
        level: Int,
        targets: MutableList<LivingEntity>,
    ): Boolean {
        val effectName: String = settings.getString("effectName")!!
        val radius = parseValues(context, caster, "radius", level, 2.0)
        val duration = (parseValues(context, caster, "duration", level, 5.0) * 1000).toLong()
        val segmentDuration = (parseValues(context, caster, "segmentDuration", level, 5.0) * 1000).toLong()
        val effectTiming = (parseValues(context, caster, "effectTiming", level, 5.0) * 1000).toLong()
        val hitDuration = (parseValues(context, caster, "hitDuration", level, 5.0) * 20).toInt()
        targets.forEach {
            val trailEntity = TrailEntity(it, effectName, radius, duration, segmentDuration, effectTiming, hitDuration)
            TrailManager.register(trailEntity)
        }
        return true
    }

}