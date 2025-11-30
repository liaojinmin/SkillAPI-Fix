package com.sucy.skill.dynamic.condition

import com.rit.sucy.config.parse.DataSection
import com.sucy.skill.SkillAPI
import com.sucy.skill.dynamic.DynamicSkill
import org.bukkit.entity.LivingEntity

class AreaEntityCondition : ConditionComponent() {

    // ally = 要求是盟友
    // all = 任意
    // ot = 敌对
    private var type: String = ""

    private var minAmount: Int = 0

    private var maxAmount: Int = 1

    private var range: Double = 0.0

    override fun getKey(): String {
        return "area entity"
    }

    override fun load(skill: DynamicSkill, config: DataSection) {
        super.load(skill, config)
        type = settings.getString("type", "all")
        minAmount = settings.getInt("minAmount", 0)
        maxAmount = settings.getInt("maxAmount", 9999)
        range = settings.getDouble("range", 0.0)
    }

    override fun test(caster: LivingEntity, level: Int, target: LivingEntity): Boolean {
        if (range <= 0.1) return false
        if (target.world.name.equals("world", true)
            || target.world.name.equals("spawn", true)) return false

        // 获取附近所有生物
        val nearby = target.world
            .getNearbyEntitiesByType(LivingEntity::class.java, target.location, range, range, range)
            .filter { entity -> // 排除自己 & 排除施法者（避免误计数）
                if (entity.entityId == target.entityId) return@filter false

                when (type) {
                    "all" -> true // 任意实体都算
                    "ot" -> SkillAPI.getSettings().canAttack(target, entity)  // 敌对关系
                    "ally" -> !SkillAPI.getSettings().canAttack(target, entity) // 盟友
                    else -> true
                }
            }
        if (nearby.isEmpty()) return false

        return nearby.size in minAmount..maxAmount
    }

}
