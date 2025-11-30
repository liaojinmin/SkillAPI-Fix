package com.sucy.skill.hook.mythic.mechanic

import com.sucy.skill.SkillAPI
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderDouble
import io.lumine.xikage.mythicmobs.skills.targeters.IEntitySelector
import org.bukkit.entity.LivingEntity

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic.summon
 *
 * @author 老廖
 * @since 2025/10/23 21:44
 */
class SummonTargetSelector(
    val config: MythicLineConfig,
): IEntitySelector(config) {

    private val radius: PlaceholderDouble =
        PlaceholderDouble.of(config.getString(arrayOf("radius", "r"), "5"))

    override fun getEntities(p0: SkillMetadata): HashSet<AbstractEntity>{
        val entity = p0.caster.entity.bukkitEntity as? LivingEntity ?: return HashSet()
        //val el = entity.location
        val r = radius.get(p0)
        return java.util.HashSet(entity.getNearbyEntities(r, r, r)
            .filterIsInstance<LivingEntity>()
            .filter { SkillAPI.getSettings().canAttack(entity, it) }
            .map { BukkitEntity(it) })
    }

}