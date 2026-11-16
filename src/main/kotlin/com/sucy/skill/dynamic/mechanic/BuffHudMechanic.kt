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
class BuffHudMechanic: MechanicComponent() {

    companion object {

        private const val ACTION: String = "action"

        private const val KEY: String = "key"

        private const val TEXTURE: String = "texture"

        private const val TEXTS: String = "texts"

        private const val TIME: String = "time"
    }

    override fun getKey(): String {
        return "buff hud"
    }

    override fun execute(caster: LivingEntity, context: SkillContext, level: Int, targets: MutableList<LivingEntity>): Boolean {
        val key = settings.getString(KEY, "DEFAULT")
        val texts = settings.getStringList(TEXTS)
        val texture = settings.getString(TEXTURE, "")
        val timer = ((parseValues(caster, TIME, level, -1.0) * 20) * 50).toLong()
        if (settings.getBool("useCaster", false)) {
            compute(caster, caster, settings.getString(ACTION), key, texture, texts, timer)
        } else {
            targets.forEach {
                compute(caster, it, settings.getString(ACTION), key, texture, texts, timer)
            }
        }
        return true
    }

    private fun compute(caster: LivingEntity, livingEntity: LivingEntity, action: String, key: String, texture: String, texts: List<String>, timer: Long) {
        if (livingEntity is Player) {
            val info = texts.map { parseVars(caster, livingEntity, it, true) }
            NeonECore.getPlayerData(livingEntity.uniqueId)?.let {
                val hud = it.buffScreenHUD ?: return
                when (action.lowercase()) {
                    "insert" -> {
                        hud.insert(key, texture, info, timer)
                    }
                    "delete" -> {
                        hud.delete(key)
                    }
                    "marge" -> {
                        hud.marge(key, info, timer)
                    }
                }
            }

        }
    }

}