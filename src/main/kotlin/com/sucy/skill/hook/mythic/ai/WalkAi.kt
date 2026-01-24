package com.sucy.skill.hook.mythic.ai

import com.sucy.skill.SkillAPI
import com.sucy.skill.hook.mythic.Summon
import com.sucy.skill.utils.referTo
import me.neon.libs.taboolib.nms.ai.SimpleAi
import me.neon.libs.taboolib.nms.ai.controllerLookAt
import me.neon.libs.taboolib.nms.ai.navigationMove
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.UUID

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mythic.ai
 *
 * @author 老廖
 * @since 2025/11/19 22:07
 */
class WalkAi(
    val owner: LivingEntity,
    val summon: Summon
): SimpleAi() {

    private val distance: Double = 10.0 * 10.0

    private val entity: LivingEntity = summon.activeMob.entity.bukkitEntity as LivingEntity

    private var lastCheckLoc: Location? = null
    private var stuckTicks = 0

    private val STUCK_TICKS = 20        // 1 秒
    private val MOVE_EPS = 0.01         // 位移阈值

    /**
     * 检查，true执行startTask
     * Activation Range 生物在物观察者，超过激活范围，AI tick会被停止。
     *
     */
    override fun shouldExecute(): Boolean {
      //  println("WalkAI shouldExecute")
        summon.disableTickTeleport = true
        if (!owner.isValid) return false
        if (owner.world.name != entity.world.name) return true
        return entity.location.distanceSquared(owner.location) > distance
    }

    /**
     * 执行任务
     */
    override fun updateTask() {
      //  println("WalkAI updateTask")
        val loc = owner.location.clone()
        summon.targetEntity = null

        // 脱离卡死
        if (isStuck112()) {
            entity.teleport(
                loc.add(0.0, 1.0, 0.0),
                PlayerTeleportEvent.TeleportCause.PLUGIN
            )
            stuckTicks = 0
            return
        }

        if (summon.isWorldChange
            || loc.world.name != entity.world.name
            || entity.location.distanceSquared(loc) > AttackAi2.ownerRange) {
            entity.teleport(loc.add(0.0, 1.0, 0.0), PlayerTeleportEvent.TeleportCause.PLUGIN)
        } else {
            entity.controllerLookAt(loc)
            entity.navigationMove(loc.referTo(loc.yaw, 90f, 2.0, 0.5), 1.3)
        }
    }

    /**
     * 是否继续执行
     * false，终止并执行resetTask
     * true执行updateTask
     */
   //override fun continueExecute(): Boolean {
      //  return false
  //  }

    private fun isStuck112(): Boolean {
        val current = entity.location

        val last = lastCheckLoc
        lastCheckLoc = current.clone()

        if (last == null) return false

        // 几乎没有移动
        if (current.distanceSquared(last) < MOVE_EPS) {
            stuckTicks++
        } else {
            stuckTicks = 0
        }

        // 是否卡在固体方块内
        val block = current.block
        val inSolid =
            block.type.isSolid &&
                    block.type != Material.AIR &&
                    block.type != Material.WATER &&
                    block.type != Material.STATIONARY_WATER &&
                    block.type != Material.LAVA &&
                    block.type != Material.STATIONARY_LAVA

        return stuckTicks >= STUCK_TICKS || inSolid
    }



}