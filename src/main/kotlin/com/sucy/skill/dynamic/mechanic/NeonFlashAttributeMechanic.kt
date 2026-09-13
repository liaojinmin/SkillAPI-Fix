package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.api.skills.SkillContext
import com.sucy.skill.dynamic.DynamicSkill
import me.geek.team.common.TeamManager
import me.neon.arena.utils.getGameTeamManager
import me.neon.flash.attribute.AttributeManager
import me.neon.flash.attribute.comp.AttributeContainer
import me.neon.flash.attribute.comp.AttributeData
import me.neon.libs.util.ResourceLocation
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import kotlin.math.max

class NeonFlashAttributeMechanic : MechanicComponent() {

    companion object {

        val GEEKTEAMPLUS = ResourceLocation.of("geekteamplus:runic")

        val NEONARENA = ResourceLocation.of("neonarena:runic")

    }

    override fun execute(caster: LivingEntity, context: SkillContext, level: Int, targets: List<LivingEntity>): Boolean {

        if (targets.isEmpty()) return false

        val attr = settings.getString("key")

        val value: Double = if (settings.getBool("useValue")) {
            DynamicSkill.getCastData(caster).getOrDefault(settings.getString("skKey", settings.getString("skValue", "EMPTY")), 0.0).toString().toDouble()
        } else max(parseValues(caster, "amount", level, 0.0), settings.getDouble("amount"))

        val container = settings.getStringList("container")
        var seconds = parseValues(caster, "seconds", level, -1.0).toLong()
        seconds = if (seconds > 0) (seconds * 1000) + System.currentTimeMillis() else -1

        if (settings.getBool("subtract", false)) {
            for (target in targets) {
                if (target is Player) {
                    AttributeManager.attributePlayer[target.uniqueId]?.let { attributesPlayer ->
                        for (it in container) {
                            val location = ResourceLocation.of(it)
                            if (location == GEEKTEAMPLUS) {
                                if (TeamManager.getTeamByPlayer(target) == null) continue
                            }
                            if (location == NEONARENA) {
                                if (target.getGameTeamManager() == null) continue
                            }
                            val map = attributesPlayer.getSubtractAttributeOrCreate(location)
                            map[attr] = ((map[attr] ?: 0.0) + value)
                        }
                        attributesPlayer.updateAttribute()
                    }
                }
            }
        } else {

            val scale = settings.getBool("scale")
            val stackable =  settings.getBool("stackable")

            for (target in targets) {
                if (target is Player) {
                    AttributeManager.attributePlayer[target.uniqueId]?.let { attributesPlayer ->
                        for (it in container){
                            val location = ResourceLocation.of(it)
                            if (location == GEEKTEAMPLUS) {
                                if (TeamManager.getTeamByPlayer(target) == null) continue
                            }
                            if (location == NEONARENA) {
                                if (target.getGameTeamManager() == null) continue
                            }

                            val attributeContainer = attributesPlayer.getNormalAttributeContainerOrCreate(location)
                            val attribute = AttributeContainer.useCacheAttribute(attr)
                            val attributeOption = AttributeContainer.useCacheScaleOption(attr)
                            attributeOption.isMultiplier = scale
                            if (stackable) {
                                attributeContainer.put(
                                    AttributeData(attribute, attributeOption, value, seconds)
                                )
                            } else {
                                if (!attributeContainer.contains(attribute, attributeOption)) {
                                    attributeContainer.put(
                                        AttributeData(attribute, attributeOption, value, seconds)
                                    )
                                } else {
                                    attributeContainer.addValue(attribute, value, attributeOption)
                                }
                            }
                        }
                        attributesPlayer.updateAttribute()
                    }
                }
            }
        }

        return true
    }

    override fun getKey(): String {
        return "neonflash attribute"
    }

}
