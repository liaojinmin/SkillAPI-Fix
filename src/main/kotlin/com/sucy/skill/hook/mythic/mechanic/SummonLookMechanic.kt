package com.sucy.skill.hook.mythic.mechanic

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic.summon.mythic
 *
 * @author 老廖
 * @since 2025/10/31 00:01
 */

class SummonLookMechanic(
    line: String,
    mlc: MythicLineConfig
) : SkillMechanic(line, mlc), ITargetedEntitySkill, ITargetedLocationSkill {

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): Boolean {
        val entity = data.caster.entity.bukkitEntity as? LivingEntity ?: return false
        val t = target.bukkitEntity as? LivingEntity ?: return false
        lookAt(entity, t.eyeLocation)
        return true
    }

    override fun castAtLocation(data: SkillMetadata, target: AbstractLocation): Boolean {
        val entity = data.caster.entity.bukkitEntity as? LivingEntity ?: return false
        lookAt(entity, BukkitAdapter.adapt(target))
        return true
    }

    private fun lookAt(entity: LivingEntity, target: Location) {
        val loc = entity.location
        val dx = target.x - loc.x
        val dy = target.y - (loc.y + entity.eyeHeight)
        val dz = target.z - loc.z

        val distanceXZ = sqrt(dx * dx + dz * dz)
        val yaw = Math.toDegrees(atan2(-dx, dz)).toFloat()
        val pitch = Math.toDegrees(-atan2(dy, distanceXZ)).toFloat()

        loc.yaw = yaw
        loc.pitch = pitch

        entity.teleport(loc) // 立即更新朝向（原地传送）
    }
}

