package com.sucy.skill.dynamic.mechanic.summon

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.attribute.mob.MobAttribute
import com.sucy.skill.api.attribute.mob.MobAttributeData
import com.sucy.skill.api.player.PlayerData
import com.sucy.skill.api.skills.SkillContext
import com.sucy.skill.dynamic.mechanic.MechanicComponent
import com.sucy.skill.hook.mythic.MythicManager
import com.sucy.skill.hook.mythic.MythicManager.spawn
import com.sucy.skill.hook.mythic.Summon
import com.sucy.skill.hook.mythic.SummonData
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.mobs.entities.SpawnReason
import me.neon.libs.taboolib.nms.ai.clearGoalAi
import me.neon.libs.taboolib.nms.ai.clearTargetAi
import org.bukkit.entity.LivingEntity


/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2025/10/23 20:12
 */
class MythicSummonMechanic: MechanicComponent() {

    override fun getKey(): String {
        return "mythic summon"
    }
    // MechanicArmorStand

    override fun execute(
        caster: LivingEntity,
        context: SkillContext,
        level: Int,
        targets: MutableList<LivingEntity>,
    ): Boolean {
        val type = settings.getString("type")!!
        val useAi = settings.getBool("useAi")
        val health = caster.maxHealth * parseValues(caster, "health", level, 0.5)
        val duration = parseValues(caster, "duration", level, 5.0) * 1000
        val forward = parseValues(caster, "forward", level, 0.0)
        val upward = parseValues(caster, "upward", level, 0.0)
        val right = parseValues(caster, "right", level, 0.0)

        ///val attr: PlayerData = SkillAPI.getPlayerData(caster.uniqueId) ?: return false

        val entity = targets.mapNotNull { target ->
            val loc = target.location
            val dir = loc.direction.setY(0).normalize()
            val side = dir.clone().crossProduct(MythicManager.UP)
            loc.add(dir.multiply(forward))
                .add(0.0, upward + 0.1, 0.0)
                .add(side.multiply(right))
            val mm = MythicManager.api.getMythicMob(type)
            if (mm != null) {
                Summon(caster, loc, mm, useAi, health, duration)
            } else {
                println("type is null by MythicSummonMechanic $type")
                null
            }
        }
        val data = MythicManager.summonMap.computeIfAbsent(caster.uniqueId) { SummonData(caster) }
        data.lock.set(true)
        data.addQueue.addAll(entity)
        data.lock.set(false)
        return true
    }

}