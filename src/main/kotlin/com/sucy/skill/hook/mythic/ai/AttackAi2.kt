package com.sucy.skill.hook.mythic.ai

import com.sucy.skill.SkillAPI
import com.sucy.skill.data.Settings
import com.sucy.skill.hook.mythic.MythicManager
import com.sucy.skill.hook.mythic.Summon
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitEntity
import io.lumine.xikage.mythicmobs.skills.SkillTrigger
import io.lumine.xikage.mythicmobs.skills.TriggeredSkill
import me.neon.libs.taboolib.nms.ai.SimpleAi
import me.neon.libs.taboolib.nms.ai.controllerLookAt
import me.neon.libs.taboolib.nms.ai.navigationMove
import me.neon.libs.util.BoundingBox
import me.neon.libs.util.setMeta
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import kotlin.collections.ArrayList

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mythic.ai
 *
 * @author 老廖
 * @since 2025/11/19 23:01
 */
class AttackAi2(
    val owner: LivingEntity,
    val summon: Summon,
) : SimpleAi() {

    companion object {

        /** 配置常量 */
        const val followRange = 20.0 * 20.0

        const val ownerRange = 26.0 * 26.0

        const val attackDistance = 2.3

        const val attackRange = (attackDistance + 0.5) * (attackDistance + 0.5)

    }

    private var cachedSummon: LivingEntity = summon.activeMob.entity.bukkitEntity as LivingEntity

    private var attackCooldown: Int = 0

    /**
     * shouldExecute:
     * - 不做任何重逻辑
     * - 只判断是否需要进入攻击行为
     */
    override fun shouldExecute(): Boolean {
        if (owner.world.name != cachedSummon.world.name) return false
        // 优先检查现有目标是否还有效
        if (summon.targetEntity == null || !isTargetValid()) {
            summon.targetEntity = findNewTarget()
        }
        return summon.targetEntity != null
    }

    /**
     * startTask:
     * - 初始化攻击冷却
     */
    override fun startTask() {
        attackCooldown = 20
    }

    /**
     * continueExecute:
     * - 不重复逻辑，只判断攻击是否应该持续
     */
    override fun continueExecute(): Boolean {
        if (owner.world.name != cachedSummon.world.name) return false
        return isTargetValid()
    }

    /**
     * updateTask:
     * - 真正的行为逻辑，每 Tick 执行一次
     */
    override fun updateTask() {
        val t = summon.targetEntity ?: return

        // 攻击逻辑
        if (cachedSummon.location.distanceSquared(t.location) <= (attackRange + 0.3)) {
            if (attackCooldown == 20) {
                cachedSummon.controllerLookAt(t)
                t.damage(1.0, cachedSummon)
            } else if (attackCooldown == 0) {
                attackCooldown = 20
                return
            }
            attackCooldown--
        } else {
            cachedSummon.controllerLookAt(t)
            // 移动
            cachedSummon.navigationMove(refApproachLocation(t), 1.2)
        }
    }

    /**
     * resetTask:
     * - 清理状态
     */
    override fun resetTask() {
        summon.targetEntity = null
    }

    private fun refApproachLocation(target: LivingEntity): Location {
        val selfLoc = cachedSummon.location
        val targetLoc = target.location

        // 自己 → 目标 的方向
        val dir = targetLoc.toVector().subtract(selfLoc.toVector()).normalize()

        // 在距离目标 attackDistance 的点停止
        // 即：目标位置 -（方向 * attackDistance）
        return targetLoc.clone().subtract(dir.multiply(attackDistance))
    }

    /**
     * 判断目标是否有效
     */
    private fun isTargetValid(): Boolean {
        val t = summon.targetEntity ?: return false
        if (!t.isValid || t.isDead) return false
        if (t.uniqueId == owner.uniqueId) return false
        if (t.uniqueId == cachedSummon.uniqueId) return false

        // 世界匹配
        if (t.world.name != cachedSummon.world.name) return false
        if (owner.world.name != cachedSummon.world.name) return false

        val loc = cachedSummon.location
        // 目标离召唤物太远
        if (loc.distanceSquared(t.location) > followRange) return false


        // 召唤物离主人太远
        if (loc.distanceSquared(owner.location) > ownerRange) return false

        return true
    }

    /**
     * 搜索附近可攻击目标 (你可以按需求扩展)
     */
    private fun findNewTarget(): LivingEntity? {
        val world = cachedSummon.world

        var livingEntity = findEntity(cachedSummon, world.getNearbyEntities(cachedSummon.location, 16.0, 16.0, 16.0))
        if (livingEntity == null) {
            val box = BoundingBox.of(cachedSummon.location, 16.0, 16.0, 16.0)
            livingEntity = findEntity(cachedSummon, world.players.filter { box.contains(it.location.toVector()) })
        }
        return livingEntity

    }

    private fun findEntity(summonEntity: LivingEntity, list: Collection<Entity>): LivingEntity? {
        return list.filterIsInstance<LivingEntity>()
            .filter { it.isValid && !it.isDead && it.uniqueId != owner.uniqueId && it.uniqueId != summonEntity.uniqueId }
            .filter {
                if (!SkillAPI.getSettings().canAttack(summonEntity, it)) {
                    return@filter false
                }
                if (it !is Player) {
                    val maybeTargetAM = MythicManager.api.getActiveMob(it.uniqueId)
                    if (maybeTargetAM.isPresent) {
                        val activeMob = maybeTargetAM.get()
                        if (activeMob.hasFaction() && activeMob.faction.equals("魔物", true)) {
                            return@filter true
                        }
                    }
                    return@filter false
                }
                return@filter true
            }
            .minByOrNull {     // 找最近的目标
                summonEntity.location.distanceSquared(it.location)
            }
    }
}
