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
class BadgeCooldownResetMechanic: MechanicComponent() {

    override fun getKey(): String {
        return "badge cooldown reset"
    }

    override fun execute(caster: LivingEntity, context: SkillContext, level: Int, targets: MutableList<LivingEntity>): Boolean {
        targets.forEach { target ->
            if (target is Player) {
                val data = NeonECore.getPlayerData(target.uniqueId)
                if (data != null) {
                    when (settings.getString("type", "E")!!) {
                        "E" -> data.badgeScreenHUD?.resetEKeySkills(false)
                        "R" -> data.badgeScreenHUD?.resetRKeySkills(false)
                        else -> data.badgeScreenHUD?.resetCooldown()
                    }
                }
            }
        }
        return true
    }

}