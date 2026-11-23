package com.sucy.skill.dynamic.mechanic

import com.sucy.skill.api.skills.SkillContext
import me.neon.core.utils.CrackManager
import me.neon.core.utils.NeonPacketAPI
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI 版本的破碎特效触发器
 *
 * YAML 配置示例：
 * crack:
 *   type: EXPLODE
 *   radius: 3.0
 *   strength: 1.0
 *   duration: 40
 *   length: 5
 *   index: 0
 *   range: 64
 */
class CrackMechanic : MechanicComponent() {

    companion object {
        private const val TYPE     = "type"
        private const val INDEX    = "index"
        private const val RADIUS   = "radius"
        private const val STRENGTH = "strength"
        private const val DURATION = "duration"
        private const val LENGTH   = "length"
        private const val RANGE    = "range"
    }

    override fun getKey(): String = "crack"

    override fun execute(
        context: SkillContext,
        caster: LivingEntity,
        level: Int,
        targets: List<LivingEntity>,
    ): Boolean {
        if (targets.isEmpty()) return false

        // ---- 参数解析 ----
        val type = parseType(settings.getString(TYPE, CrackManager.CrackMotionType.WAVE.name))
        val radius   = parseValues(context, caster, RADIUS, level, 3.0).toFloat()
        val strength = parseValues(context, caster, STRENGTH, level, 1.0).toFloat()
        val duration = parseValues(context, caster, DURATION, level, 40.0).toInt()
        val length   = parseValues(context, caster, LENGTH, level, 5.0).toInt()
        val indexBase = parseValues(context, caster, INDEX, level, 0.0).toInt()
        val range    = parseValues(context, caster, RANGE, level, 64.0)

        // ---- 施法者朝向（位置由 targeter 决定）----
        val dir = (caster.eyeLocation ?: caster.location).direction

        var sentCount = 0
        val rangeSq = range * range

        targets.forEachIndexed { i, target ->
            val pos = target.location
            val world = pos.world ?: return@forEachIndexed

            // 只发给能看见的玩家
            val viewers = world.players.filter {
                it.location.distanceSquared(pos) <= rangeSq
            }
            if (viewers.isEmpty()) return@forEachIndexed

            NeonPacketAPI.sendCreateCrack(
                players = viewers,
                index = indexBase + i,
                pos = pos,
                dir = dir,
                radius = radius,
                intensity = strength,
                length = length,
                duration = duration,
                type = type
            )
            sentCount++
        }

        return sentCount > 0
    }

    private fun parseType(raw: String): CrackManager.CrackMotionType =
        runCatching { CrackManager.CrackMotionType.valueOf(raw.uppercase()) }
            .getOrDefault(CrackManager.CrackMotionType.EXPLODE)
}