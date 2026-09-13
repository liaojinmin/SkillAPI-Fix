package com.sucy.skill.dynamic.target

import com.sucy.skill.cast.IIndicator
import com.sucy.skill.dynamic.TempEntity
import me.neon.libs.util.item.isAir
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.target
 *
 * @author 老廖
 * @since 2026/4/5 18:12
 */
class RandomLocationTarget: TargetComponent() {

    private data class LocationData(
        // 范围相关
        val minRange: Double,
        val maxRange: Double,
        // 选点数量
        val minAmount: Int,
        val maxAmount: Int,
        // 点之间距离
        val minDistance: Double,
        val maxDistance: Double,
        val ground: Boolean,
    )

    override fun makeIndicators(list: List<IIndicator>, caster: Player, target: LivingEntity, level: Int) {
        makeCircleIndicator(list, caster, 0.5)
    }

    /** {@inheritDoc}  */
    override fun getTargets(caster: LivingEntity, level: Int, targets: List<LivingEntity>): List<LivingEntity> {
        val minRange = parseValues(caster, "minRange", level, 1.0)
        val maxRange = parseValues(caster, "maxRange", level, 5.0)
        val minAmount = settings.getInt("minAmount", 1)
        val maxAmount = settings.getInt("maxAmount", 1)
        val minDistance = parseValues(caster, "minDistance", level, 0.0)
        val maxDistance = parseValues(caster, "maxDistance", level, 5.0)
        val data = LocationData(minRange, maxRange, minAmount, maxAmount, minDistance, maxDistance, settings.getBool("ground", true))
        val list = mutableListOf<LivingEntity>()
        targets.forEach { list.addAll(parseTarget(caster, it, data)) }
        return list
    }

    private fun parseTarget(caster: LivingEntity, target: LivingEntity, locationData: LocationData): List<LivingEntity> {
        val world = target.world ?: return emptyList()
        val targetLoc = target.location
        //println("中心点: x:${targetLoc.blockX} y: ${targetLoc.blockY} z:${targetLoc.blockZ}")
        val random = ThreadLocalRandom.current()

        // 随机生成点数量
        val amount =
            if (locationData.minAmount >= locationData.maxAmount)
                locationData.minAmount
            else
                random.nextInt(
                    locationData.minAmount,
                    locationData.maxAmount + 1
                )



        val generatedLocations = mutableListOf<Location>()
        val result = mutableListOf<LivingEntity>()


        for (i in 0 until amount) {
            var validLocation: Location? = null

            for (attempt in 0 until 30) {

                var distance =
                    locationData.minRange +
                            random.nextDouble() *
                            (locationData.maxRange - locationData.minRange)
                distance /= 2

                // 随机角度
                val angle = random.nextDouble() * 2 * Math.PI

                val x = targetLoc.x + cos(angle) * distance
                val z = targetLoc.z + sin(angle) * distance
                val y = if (locationData.ground) {
                    getSafeGroundYFast(world, x.toInt(), z.toInt())
                } else {
                    targetLoc.y   // 不需要贴地时保留原来逻辑
                }

                val candidate = Location(world, x, y, z)

                // 地面处理
                if (locationData.ground && !isSafe(candidate)) {
                    continue
                }

                // 检查与已生成点的距离是否满足 minDistance（避免重叠）
                val tooClose = generatedLocations.any { it.distanceSquared(candidate) < (locationData.minDistance * locationData.minDistance) }
                if (!tooClose) {
                    validLocation = candidate
                    break
                }
            }

            if (validLocation != null) {
                generatedLocations.add(validLocation)
              //  println("out: x:${validLocation.blockX} y:${validLocation.blockY} z:${validLocation.blockZ}")
                result.add(TempEntity(validLocation))
            }
        }

        return result
    }

    override fun getKey(): String {
        return "random location"
    }

    private fun getSafeGroundYFast(
        world: World,
        x: Int,
        z: Int,
        startY: Int = 20,
        step: Int = 2
    ): Double {
        val maxY = world.maxHeight - 2  // 留出头部空间

        // 1. 向上跳格扫描
        var y = startY
        while (y <= maxY) {
            if (isSafeSpot(world, x, y, z)) {
                return y.toDouble()
            }
            y += step
        }

        // 2. 向下跳格扫描（从 startY-1 开始）
        y = startY - 1
        while (y >= 1) {
            if (isSafeSpot(world, x, y, z)) {
                return y.toDouble()
            }
            y -= step
        }

        // 3. 都没找到，降级为精细扫描（兜底）
        return world.getHighestBlockYAt(x, z).toDouble()  // 使用之前的逐格扫描方法
    }

    private fun isSafeSpot(world: World, x: Int, y: Int, z: Int): Boolean {
        val head = world.getBlockAt(x, y + 1, z)
        val ground = world.getBlockAt(x, y - 1, z)

        // 脚部、头部必须为空气（或透明非固体）
        if (head.type.isSolid) return false
        // 脚下必须是固体且非危险
        if (!ground.type.isSolid || ground.type in dangerousBlocks) return false
        return true
    }

    private fun isSafe(location: Location): Boolean {
        val world = location.world ?: return false
        val ground = location.clone().subtract(0.0, 1.0, 0.0)
        val head = location.clone().add(0.0, 1.0, 0.0)

        // 地面方块检查
        val groundBlock = ground.block
        if (!groundBlock.type.isBlock || groundBlock.type.isTransparent) return false
        if (groundBlock.type in setOf(
                Material.LAVA, Material.STATIONARY_LAVA,
                Material.FIRE, Material.CACTUS
            )) return false

        // 脚部和头部空间必须是可穿越的（不会窒息）
        val feetBlock = location.block
        val headBlock = head.block
        return feetBlock.type.isTransparent && headBlock.type.isTransparent
    }

    private val dangerousBlocks = setOf(
        Material.LAVA, Material.STATIONARY_LAVA,
        Material.FIRE, Material.CACTUS,
        Material.MAGMA // 岩浆块也会造成伤害，视你的版本而定可保留
    )

}