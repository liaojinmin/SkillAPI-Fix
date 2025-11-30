package com.sucy.skill.hook.mythic.mechanic

import com.sucy.skill.hook.mythic.MythicManager
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderDouble
import io.lumine.xikage.mythicmobs.skills.targeters.IEntitySelector

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic.summon
 *
 * @author 老廖
 * @since 2025/10/23 21:44
 */
class SummonOwnerSelector(
    val config: MythicLineConfig,
): IEntitySelector(config) {

    private val resultSet = HashSet<AbstractEntity>(1)

    private val radius: PlaceholderDouble =
        PlaceholderDouble.of(config.getString(arrayOf("radius", "r"), "0"))

    override fun getEntities(p0: SkillMetadata): HashSet<AbstractEntity> {
        resultSet.clear()
        val entity = p0.caster.entity
        val a = MythicManager.summonAscription[entity.uniqueId] ?: return resultSet
        val r = radius.get(p0)

        if (r <= 0 || a.location.distanceSquared(entity.bukkitEntity.location) <= r * r) {
            resultSet.add(BukkitEntity(a))
        }
        return resultSet
    }

}