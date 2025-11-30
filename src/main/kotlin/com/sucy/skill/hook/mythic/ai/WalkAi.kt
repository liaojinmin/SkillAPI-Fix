package com.sucy.skill.hook.mythic.ai

import com.sucy.skill.hook.mythic.Summon
import me.neon.libs.taboolib.nms.ai.SimpleAi
import me.neon.libs.taboolib.nms.ai.controllerLookAt
import me.neon.libs.taboolib.nms.ai.navigationMove
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import java.util.UUID

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mythic.ai
 *
 * @author 老廖
 * @since 2025/11/19 22:07
 */
class WalkAi(
    val owner: UUID,
    val summon: Summon
): SimpleAi() {


    val distance: Double = 8.0 * 8.0

    private val distance2: Double = 26.0 * 26.0

    /**
     * 检查，true执行startTask
     */
    override fun shouldExecute(): Boolean {
        val owner = Bukkit.getPlayer(owner) ?: return false
        // 如果距离玩家超过 8.0 则移动实体
        return owner.isOnline
                && !owner.isDead
                && summon.activeMob.entity.bukkitEntity.location.distanceSquared(owner.location) > distance
    }

    /**
     * 执行任务
     */
    override fun startTask() {
        val owner = Bukkit.getPlayer(owner) ?: return
        val pet = summon.activeMob.entity.bukkitEntity as LivingEntity
        summon.activeMob.resetTarget()
        summon.targetEntity = null
        if (pet.location.distanceSquared(owner.location) > distance2) {
            pet.teleport(owner.location)
        } else {
            pet.controllerLookAt(owner)
            val location = owner.location
            pet.navigationMove(location.referTo(location.yaw, 90f, 2.0, 0.5), 1.3)
        }
    }

    /**
     * 是否继续执行
     * false，终止并执行resetTask
     * true执行updateTask
     */
    override fun continueExecute(): Boolean {
        return false
    }

    private fun Location.referTo(yaw: Float, offset: Float, multiply: Double, height: Double): Location {
        val referLoc: Location = this.clone()
        referLoc.yaw = yaw + offset
        val vectorAdd = referLoc.direction.normalize().multiply(multiply)
        referLoc.add(vectorAdd)
        referLoc.add(0.0, height, 0.0)
        return referLoc
    }

}