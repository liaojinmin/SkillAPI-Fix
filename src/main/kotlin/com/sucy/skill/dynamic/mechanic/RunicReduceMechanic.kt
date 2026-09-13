package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.api.skills.SkillContext
import com.sucy.skill.api.util.FlagManager
import me.geek.team.common.TeamManager
import me.geek.team.common.germ.GermManager.getRunicHud
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2025/10/23 20:12
 */
class RunicReduceMechanic: MechanicComponent() {

    companion object {

        private const val KEY: String = "key"

        private const val SCALE: String = "scale"
    }

    override fun getKey(): String {
        return "runic reduce"
    }

    override fun execute(
        caster: LivingEntity,
        context: SkillContext,
        level: Int,
        targets: List<LivingEntity>,
    ): Boolean {
        if (targets.isEmpty() || !settings.has(KEY)) {
            return false
        }

        val keys = settings.getStringList(KEY)
        if (keys.isEmpty()) return false
        val scale = parseValues(caster, SCALE, level, 0.5)

        for (target in targets) {
            if (target is Player) {
                val hud = target.getRunicHud() ?: continue
                for (key in keys) {
                    if (key.isEmpty()) continue
                    hud.actionReduces[key] = scale
                }
            }
        }
        return targets.isNotEmpty()
    }

}