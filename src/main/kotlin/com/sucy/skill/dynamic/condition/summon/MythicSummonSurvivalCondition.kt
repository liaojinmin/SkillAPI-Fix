package com.sucy.skill.dynamic.condition.summon

import com.rit.sucy.config.parse.DataSection
import com.sucy.skill.dynamic.DynamicSkill
import com.sucy.skill.dynamic.condition.ConditionComponent
import com.sucy.skill.dynamic.mechanic.DamageMechanic
import com.sucy.skill.hook.mythic.MythicManager
import com.sucy.skill.hook.mythic.Summon
import me.neon.core.toughness.ToughnessData
import me.neon.libs.util.BoundingBox
import me.neon.libs.util.getMetaFirstOrNull
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import kotlin.math.min

class MythicSummonSurvivalCondition : ConditionComponent() {

    private var mythicType: String = ""

    private var minAmount: Int = 0
    private var maxAmount: Int = 1

    private var range: Double = 0.0

    override fun getKey(): String {
        return "mythic summon survival"
    }

    override fun load(skill: DynamicSkill, config: DataSection) {
        super.load(skill, config)
        mythicType = settings.getString("mythicType")
        minAmount = settings.getInt("minAmount")
        maxAmount = settings.getInt("maxAmount")
        range = settings.getDouble("range")
    }

    override fun test(caster: LivingEntity, level: Int, target: LivingEntity): Boolean {
        val data= MythicManager.summonMap[target.uniqueId] ?: return false
        val now: Collection<Summon>
        if (range > 0.1) {
            val box = BoundingBox.of(target.location, range, range, range)
            now = data.getAllSummon {
                (mythicType.isEmpty() || it.activeMob.type.entityType == mythicType)
                        && box.contains(it.activeMob.entity.bukkitEntity.location.toVector())
            }
        } else {
            now = data.getAllSummon {
                (mythicType.isEmpty() || it.activeMob.type.entityType == mythicType)
            }
        }
        return now.size in minAmount..maxAmount
    }

}
