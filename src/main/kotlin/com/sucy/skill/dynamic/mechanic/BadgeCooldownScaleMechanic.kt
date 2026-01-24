package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.api.skills.SkillContext
import me.neon.core.NeonECore
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2025/10/23 20:12
 */
class BadgeCooldownScaleMechanic: MechanicComponent() {

    companion object {

        const val PROPORTION: String = "proportion"

    }

    override fun getKey(): String {
        return "badge cooldown scale"
    }

    override fun execute(caster: LivingEntity, context: SkillContext, level: Int, targets: MutableList<LivingEntity>): Boolean {
        targets.forEach { target ->
            if (target is Player) {
                val data = NeonECore.getPlayerData(target.uniqueId)
                if (data != null) {
                    val scale = parseValues(target, PROPORTION, level, settings.getDouble(PROPORTION))
                    when (settings.getString("type", "E")!!) {
                        "E" -> data.badgeScreenHUD?.setCooldownReduction(1, scale)
                        "R" -> data.badgeScreenHUD?.setCooldownReduction(2, scale)
                        else -> data.badgeScreenHUD?.setCooldownReduction(3, scale)
                    }
                }
            }
        }
        return true
    }

}