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
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
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
    val owner: UUID,
    val summon: Summon,
) : SimpleAi() {

    /** 配置常量 */
    private val followRange = 26.0 * 26.0
    private val ownerRange = 30.0 * 30.0
    private val attackRange = 2.5 * 2.5

    /** 状态 */
    private var target: LivingEntity? = null
    private var attackCooldown: Int = 0

    /** 每 tick 缓存减少重复计算 */
    private var cachedOwner: Player? = null
    private var cachedSummon: LivingEntity? = null

    /**
     * shouldExecute:
     * - 不做任何重逻辑
     * - 只判断是否需要进入攻击行为
     */
    override fun shouldExecute(): Boolean {
        val ownerPlayer = Bukkit.getPlayer(owner) ?: return false
        cachedOwner = ownerPlayer

        val summonEntity = summon.activeMob.entity.bukkitEntity as? LivingEntity ?: return false
        cachedSummon = summonEntity

        // 优先检查现有目标是否还有效
        if (!isTargetValid(target, ownerPlayer, summonEntity)) {
            target = findNewTarget(ownerPlayer, summonEntity)
        }

        return target != null
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
        val t = target ?: return false
        val ownerPlayer = cachedOwner ?: return false
        val summonEntity = cachedSummon ?: return false

        return isTargetValid(t, ownerPlayer, summonEntity)
    }

    /**
     * updateTask:
     * - 真正的行为逻辑，每 Tick 执行一次
     */
    override fun updateTask() {
        val summonEntity = cachedSummon ?: return
        val t = target ?: return

        summonEntity.controllerLookAt(t)
        // 攻击逻辑
        val dist = summonEntity.location.distanceSquared(t.location)
        if (dist <= attackRange) {
            if (attackCooldown == 20) {
                //println("attackCooldown TriggeredSkill")
                summon.activeMob.setTarget(BukkitEntity(t))
                t.damage(1.0, summon.bukkitEntity)
            } else if (attackCooldown == 0) {
                attackCooldown = 20
                return
            }
            attackCooldown--
        } else {
            // 移动
            summonEntity.navigationMove(t.location, 1.3)
        }
    }

    /**
     * resetTask:
     * - 清理状态
     */
    override fun resetTask() {
        summon.activeMob.resetTarget()
        target = null
        cachedOwner = null
        cachedSummon = null
    }

    /**
     * 判断目标是否有效
     */
    private fun isTargetValid(t: LivingEntity?, owner: Player, summonEntity: LivingEntity): Boolean {
        if (t == null) return false
        if (!t.isValid || t.isDead) return false

        if (t.uniqueId == owner.uniqueId) return false
        if (t.uniqueId == summonEntity.uniqueId) return false

        // 世界匹配
        if (t.world != summonEntity.world) return false

        // 目标离召唤物太远
        if (summonEntity.location.distanceSquared(t.location) > followRange) return false

        // 召唤物离主人太远
        if (summonEntity.location.distanceSquared(owner.location) > ownerRange) return false

        return true
    }

    /**
     * 搜索附近可攻击目标 (你可以按需求扩展)
     */
    private fun findNewTarget(owner: Player, summonEntity: LivingEntity): LivingEntity? {
        val world = summonEntity.world

        var livingEntity = findEntity(owner, summonEntity, world.getNearbyEntities(summonEntity.location, 16.0, 16.0, 16.0))
        if (livingEntity == null) {
            val box = BoundingBox.of(summonEntity.location, 16.0, 16.0, 16.0)
            livingEntity = findEntity(owner, summonEntity, world.players.filter { box.contains(it.location.toVector()) })
        }
        return livingEntity

    }

    private fun findEntity(owner: Player, summonEntity: LivingEntity, list: Collection<Entity>): LivingEntity? {
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
