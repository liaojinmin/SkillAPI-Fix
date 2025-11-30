package com.sucy.skill.hook.mythic.mechanic

import com.sucy.skill.SkillAPI
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.skills.SkillCondition
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityComparisonCondition
import io.lumine.xikage.mythicmobs.util.annotations.MythicCondition
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mythic.conditions
 *
 * @author 老廖
 * @since 2024/8/17 15:49
 */
@MythicCondition(
    author = "廖爷爷",
    name = "summonTarget",
    aliases = ["summonTarget"],
    version = "4.9",
    description = "选项召唤物可攻击的生物"
)
class SummonTargetCondition(line: String) : SkillCondition(line), IEntityComparisonCondition {

    override fun check(caster: AbstractEntity, target: AbstractEntity?): Boolean {
        if (target == null) return false

        if (caster.bukkitEntity.entityId == target.bukkitEntity.entityId) return false

        val casterBukkit = caster.bukkitEntity
        val targetBukkit = target.bukkitEntity
        if (targetBukkit !is LivingEntity || casterBukkit !is LivingEntity) return false

       // println("caster: ${caster.name}   target: ${target.name}")
        // 快速排除主城世界
        if (casterBukkit.world.name.equals("spawn", true)) return false

        return SkillAPI.getSettings().canAttack(casterBukkit, targetBukkit)
    }
}
