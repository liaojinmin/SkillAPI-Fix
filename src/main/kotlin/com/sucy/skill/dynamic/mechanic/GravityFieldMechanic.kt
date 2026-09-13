package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.skills.SkillContext
import com.sucy.skill.dynamic.GravityField
import me.neon.core.NeonECore
import me.neon.core.revive.ReviveManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2025/10/23 20:12
 */
class GravityFieldMechanic: MechanicComponent() {

    override fun getKey(): String {
        return "gravity field"
    }

    override fun execute(caster: LivingEntity, context: SkillContext, level: Int, targets: MutableList<LivingEntity>): Boolean {
        val radius = parseValues(caster, "radius", level, 5.0)
        val pullStrength = parseValues(caster, "pullStrength", level, 0.15)
        val durationTicks = parseValues(caster, "duration", level, 5.0) * 20
        val decayMode = GravityField.Decay
            .valueOf(settings.getString("decayMode", GravityField.Decay.CONSTANT.name).uppercase())
        val targetType = settings.getString("targetType", "all")
        targets.forEach {
            GravityField(
                it.location,
                radius, pullStrength, durationTicks.toLong(), decayMode
            ) { back ->
                executeChildren(caster, context, level, back.toMutableList())
            }.setTargetFilter { target ->
                if (target == caster) return@setTargetFilter false
                if (target is Player && caster is Player && caster.world.name == "spawn") return@setTargetFilter false
                when (targetType) {
                    "all" -> true // 任意实体都算
                    "ot" -> !SkillAPI.getSettings().canAttack(target, target)  // 敌对关系
                    "ally" -> SkillAPI.getSettings().canAttack(target, target) // 盟友
                    else -> false
                }
            }.start()
        }
        return true
    }

}