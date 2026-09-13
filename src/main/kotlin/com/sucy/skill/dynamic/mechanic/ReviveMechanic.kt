package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.api.skills.SkillContext
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
class ReviveMechanic: MechanicComponent() {

    override fun getKey(): String {
        return "revive"
    }

    override fun execute(caster: LivingEntity, context: SkillContext, level: Int, targets: MutableList<LivingEntity>): Boolean {
        targets.forEach { target ->
            if (target is Player) {
                ReviveManager.dungeonCache[target.world.name]?.let {
                    it.kneeCache[target.uniqueId]?.revive()
                }
            }
        }
        return true
    }

}