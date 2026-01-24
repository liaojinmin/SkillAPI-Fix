package com.sucy.skill.trail

import com.sucy.skill.trail.TrailManager.approxEquals
import me.neon.libs.util.BoundingBox
import me.neon.libs.util.Vector
import kotlin.math.abs

/**
 * SkillAPI-Fix
 * com.sucy.skill.trail
 *
 * @author 老廖
 * @since 2026/1/18 00:52
 */
data class TrailSegment(

    /**
     * 原点
     */
    val from: Vector,

    /**
     * 目标点
     */
    val to: Vector,

    /**
     * 段落范围半径（线宽度）
     */
    val radius: Double,

    /**
     * 过期时间
     */
    val expireTime: Long,

    /**
     * 是否忽略Y轴，用于XZ平面判定
     */
    val ignoreY: Boolean = false
) {

    val boundingBox: BoundingBox by lazy {
        BoundingBox.of(from, to).expand(radius)
    }

    /**
     * 特效计时
     */
    var effectTiming: Long = -1

    fun isTimerOut(currentTime: Long): Boolean {
        return currentTime >= expireTime
    }

    fun hit(point: Vector, entityWidth: Double, entityHeight: Double): Boolean {
        return if (ignoreY) hitXZ(point, entityWidth, entityHeight) else hitXYZ(point, entityWidth, entityHeight)
    }

    // 默认XYZ全判
    private fun hitXYZ(point: Vector, entityWidth: Double, entityHeight: Double): Boolean {
        ///val pos = point.setY(point.y + entityHeight)
        //return boundingBox.contains(pos)
         val r = radius + entityWidth
         val rSq = r * r
         val distSq = distancePointToSegmentSquared(point)
         return distSq <= rSq + TrailManager.EPS
    }

    // 忽略Y轴的判定
    private fun hitXZ(point: Vector, entityWidth: Double, entityHeight: Double): Boolean {
        // 取XZ向量
        val pXZ = Vector(point.x, 0.0, point.z)
        val fromXZ = Vector(from.x, 0.0, from.z)
        val toXZ = Vector(to.x, 0.0, to.z)

        val ab = toXZ.clone().subtract(fromXZ)
        val ap = pXZ.clone().subtract(fromXZ)

        val abLenSq = ab.lengthSquared()
        if (abLenSq < TrailManager.EPS) {
            return pXZ.distanceSquared(fromXZ) <= (radius + entityWidth) * (radius + entityWidth) + TrailManager.EPS
        }

        var t = ap.dot(ab) / abLenSq
        t = t.coerceIn(0.0, 1.0)

        val closest = fromXZ.clone().add(ab.multiply(t))
        return closest.distanceSquared(pXZ) <= (radius + entityWidth) * (radius + entityWidth) + TrailManager.EPS
    }


    fun approxEquals(other: TrailSegment): Boolean {
        return from.approxEquals(other.from, 0.05) &&
                to.approxEquals(other.to, 0.05) &&
                abs(radius - other.radius) <= 0.01
    }

    private fun distancePointToSegmentSquared(p: Vector): Double {
        val ab = to.clone().subtract(from)
        val ap = from.clone().subtract(from)

        val abLenSq = ab.lengthSquared()
        if (abLenSq < TrailManager.EPS) {
            // 线段退化为点
            return p.distanceSquared(from)
        }

        var t = ap.dot(ab) / abLenSq
        t = t.coerceIn(0.0, 1.0)

        val closest = from.clone().add(ab.multiply(t))
        return closest.distanceSquared(p)
    }

}
