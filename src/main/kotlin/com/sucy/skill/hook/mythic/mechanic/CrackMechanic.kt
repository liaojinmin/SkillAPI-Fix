package com.sucy.skill.hook.mythic.mechanic

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import me.neon.core.utils.CrackManager
import me.neon.core.utils.NeonPacketAPI
import org.bukkit.Location
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mythic.mechanic
 *
 * @author 老廖
 * @since 2025/10/31 00:01
 */
class CrackMechanic(
    line: String,
    mlc: MythicLineConfig
) : SkillMechanic(line, mlc), ITargetedEntitySkill, ITargetedLocationSkill {

    // ---------------- 特效参数 ----------------
    private val type: CrackManager.CrackMotionType = runCatching {
        CrackManager.CrackMotionType.valueOf(mlc.getString(arrayOf("type", "motion"), "EXPLODE").uppercase())
    }.getOrDefault(CrackManager.CrackMotionType.EXPLODE)

    private val radius: Float    = mlc.getFloat(arrayOf("radius", "r"), 3.0f)
    private val strength: Float  = mlc.getFloat(arrayOf("strength", "intensity", "s"), 1.0f)
    private val duration: Int    = mlc.getInteger(arrayOf("duration", "time", "d"), 40)
    private val length: Int      = mlc.getInteger(arrayOf("length", "len", "l"), 5)

    // ---------------- 工程参数 ----------------
    private val index: Int       = mlc.getInteger(arrayOf("index", "id"), 0)
    private val range: Double    = mlc.getDouble(arrayOf("range", "view", "v"), 64.0)

    override fun castAtEntity(data: SkillMetadata, target: AbstractEntity): Boolean {
        val caster = data.caster.entity.bukkitEntity as? LivingEntity ?: return false
        val t = target.bukkitEntity as? LivingEntity ?: return false
        call(caster, t.location)
        return true
    }

    override fun castAtLocation(data: SkillMetadata, target: AbstractLocation): Boolean {
        val caster = data.caster.entity.bukkitEntity as? LivingEntity ?: return false
        call(caster, BukkitAdapter.adapt(target))
        return true
    }

    private fun call(caster: LivingEntity, target: Location) {
        // 朝向：始终取施法者视线方向；坐标：由 targeter 决定
        val dir = caster.eyeLocation.direction

        // 只发给能看到该位置的玩家，避免全服广播
        val players = target.world.players.filter {
            it.location.distanceSquared(target) <= range * range
        }
        if (players.isEmpty()) return

        NeonPacketAPI.sendCreateCrack(
            players = players,
            index = index,
            pos = target,
            dir = dir,
            radius = radius,
            intensity = strength,
            length = length,
            duration = duration,
            type = type
        )
    }
}

