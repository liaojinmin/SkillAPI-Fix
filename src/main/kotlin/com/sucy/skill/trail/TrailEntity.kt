package com.sucy.skill.trail

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.event.TrailHurtEvent
import com.sucy.skill.germ.GermPluginAPI
import com.sucy.skill.utils.toNeonLibsVector
import me.neon.libs.util.Vector
import org.bukkit.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player


/**
 * SkillAPI-Fix
 * com.sucy.skill.trail
 *
 * @author 老廖
 * @since 2026/1/18 02:06
 */
class TrailEntity(

    /**
     * 玩家实列
     */
    val livingEntity: LivingEntity,

    /**
     * 特效名称
     */
    val effectName: String,

    /**
     * 链路伤害范围
     */
    val radius: Double,

    /**
     * [TrailEntity] 失效时长
     */
    val duration: Long,

    /**
     * [TrailSegment] 失效时长
     */
    val segmentDuration: Long,

    /**
     * 特效存活时间
     */
    val effectTiming: Long,

    /**
     * 伤害频率
     */
    val hitDuration: Int = 20
) {

    private val trailSegments: MutableList<TrailSegment> = mutableListOf()

    private val startTime: Long = System.currentTimeMillis() + duration

    private val registerWorld: String = livingEntity.world.name

    private var lastLocation: Location = livingEntity.location

    private var hitTick: Int = 0

    private var effectTick: Int = 0

    fun tick() {
        if (!validCheck()) return
        val current = System.currentTimeMillis()
        val now = livingEntity.location
        val from: Vector = lastLocation.toNeonLibsVector()
        val to: Vector = now.toNeonLibsVector()
        if (from.distanceSquared(to) > TrailManager.EPS) {
            val seg = TrailSegment(from, to, radius, current + segmentDuration)
            val last = trailSegments.lastOrNull()
            if (last == null) {
                trailSegments.add(seg)
            } else {
                // TODO 此处可以做直线段落合并，看后期需要。
                if (!last.approxEquals(seg)) {
                    trailSegments.add(seg)
                }
            }
        }

        lastLocation = now
        trailSegments.removeIf { it.isTimerOut(current) }
        if (hitTick >= hitDuration) {
            hitTick = 0
            //println("尝试唤起命中")
            if (trailSegments.isEmpty()) return

            livingEntity.world.livingEntities.forEach(::hit)
        } else {
            hitTick++
        }

        if (effectTick >= 1) {
            effectTick = 0
            spawnGermEffect()
        } else effectTick++

    }

    fun hit(entity: LivingEntity) {
      //  println("  判断: ${entity.name}")
        if (trailSegments.isEmpty()) {
          //  println("  中断 A1")
            return
        }
        if (!validCheck()) {
           // println("  中断 A2")
            return
        }

        if (entity.entityId == livingEntity.entityId || !SkillAPI.getSettings().canAttack(livingEntity, entity)) {
           // println("  中断 A3")
            return
        }
        //if (!Bukkit.isPrimaryThread()) error("此方法必须在主线程")
        val pos = entity.location.toNeonLibsVector()
        for (seg in trailSegments) {
            if (seg.hit(pos, entity.width / 2, entity.height / 2)) {
              //  println("  即将发起命中事件 owner: ${livingEntity.name} target: ${entity.name}")
                if (Bukkit.isPrimaryThread()) {
                    TrailHurtEvent(this, seg, entity).callEvent()
                    //entity.damage(1.0)
                } else Bukkit.getScheduler().runTask(SkillAPI.singleton(), Runnable {
                    TrailHurtEvent(this, seg, entity).callEvent()
                })
                // 只命中一次伤害
                break
            }
        }
    }

    fun validCheck(checkState: Boolean = true): Boolean {
        if (checkState && (livingEntity.isDead || !livingEntity.isValid)) return false

        if (livingEntity.world.name != registerWorld) return false

        if (duration <= 0) return true
        return System.currentTimeMillis() < startTime
    }

    private var lastEffectPoint: Vector? = null

    private fun spawnGermEffect() {
        if (trailSegments.isEmpty()) return

        val current = System.currentTimeMillis()

        for (seg in trailSegments) {
            if (seg.effectTiming > current) continue

            val from = seg.from
            val to = seg.to

            val dir = to.clone().subtract(from)
            val length = dir.length()
            if (length < TrailManager.EPS) continue

            dir.multiply(1.0 / length)

            var traveled = 0.0
            val step = 1.0 //

            while (traveled <= length) {
                val point = from.clone().add(dir.clone().multiply(traveled))

                val last = lastEffectPoint
                if (last == null || distanceSquaredXZ(last, point) >= 1.0) {
                    GermPluginAPI.sendSpawnEffect(
                        this, seg,
                        point.x, point.y + 0.1, point.z
                    )
                    lastEffectPoint = point.clone()
                }

                traveled += step
            }

            seg.effectTiming = current + effectTiming
        }
    }


    private fun distanceSquaredXZ(a: Vector, b: Vector): Double {
        val dx = a.x - b.x
        val dz = a.z - b.z
        return dx * dx + dz * dz
    }


    private fun spawnDemoEffect() {
        val world = livingEntity.world
        if (trailSegments.isEmpty()) return
        // 1. 控制随机抽取比例
        val minSegments = if (trailSegments.size > 3) 3 else 0          // 最少生成的段，避免断层
        val maxSegments = trailSegments.size
        val count = (minSegments..maxSegments).random() // 随机选取数量

        // 2. 随机抽取若干 trailSegments
        val selectedSegments = trailSegments.shuffled().take(count)
        for (seg in selectedSegments) {
            val from = seg.from
            val to = seg.to

            val dir = to.clone().subtract(from)
            val length = dir.length()
            if (length < TrailManager.EPS) continue
            dir.normalize()

            val left = Vector(-dir.z, 0.0, dir.x).normalize()
            val right = left.clone().multiply(-1)

            val step = 0.25
            var traveled = 0.0

            while (traveled <= length) {
                val center = from.clone().add(dir.clone().multiply(traveled))

                // 中心线（绿色）
                spawnEffect(world, center, 0.0, 1.0, 0.0)

                // 左右边界
                spawnEffect(
                    world,
                    center.clone().add(left.clone().multiply(seg.radius)),
                    0.0, 0.9, 0.1
                )
                spawnEffect(
                    world,
                    center.clone().add(right.clone().multiply(seg.radius)),
                    0.0, 0.9, 0.1
                )

                traveled += step
            }
        }
    }

    private fun spawnEffect(
        world: World,
        v: Vector,
        r: Double,
        g: Double,
        b: Double
    ) {
        world.spawnParticle(
            Particle.SPELL_MOB,
            v.x, v.y + 0.1, v.z,
            0,
            r, g, b,
            1.0
        )
    }


}