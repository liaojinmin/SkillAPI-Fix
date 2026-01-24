package com.sucy.skill.utils

import me.neon.libs.util.Vector
import org.bukkit.Location

/**
 * SkillAPI-Fix
 * com.sucy.skill.utils
 *
 * @author 老廖
 * @since 2025/12/24 08:17
 */

fun Location.toFormatString(): String {
    return "world=${world.name};x=${x};y=${y};z=${z};pitch=${pitch};yaw=${yaw}"
}

fun Location.referTo(yaw: Float, offset: Float, multiply: Double, height: Double): Location {
    val referLoc: Location = this.clone()
    referLoc.yaw = yaw + offset
    val vectorAdd = referLoc.direction.normalize().multiply(multiply)
    referLoc.add(vectorAdd)
    referLoc.add(0.0, height, 0.0)
    return referLoc
}

fun Location.toNeonLibsVector(): Vector {
    return Vector(this.x, this.y, this.z)
}