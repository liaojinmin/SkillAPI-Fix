package com.sucy.skill.hook.chemdah


import com.sucy.skill.api.event.EntitySkillKillEvent
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI
import me.neon.core.toughness.Toughness.bukkitAPIHelper
import me.neon.flash.api.NeonFlashAPI
import me.neon.libs.taboolib.nms.getName
import me.neon.libs.util.item.isAir
import org.bukkit.entity.Player

/**
 * GeekTeamPlus
 * me.geek.team.api.chemdah
 *
 * @author 老廖
 * @since 2024/7/28 18:37
 */
object EntitySkillKillObjective: ObjectiveCountableI<EntitySkillKillEvent>() {

    override val event: Class<EntitySkillKillEvent> = EntitySkillKillEvent::class.java
    override val name: String = "skill kill"

    init {

        handler {
            it.attack as? Player
        }

        addSimpleCondition("mobType") { data, it ->
            val activeMob = bukkitAPIHelper.getMythicMobInstance(it.target)
            if (activeMob != null) {
                data.toString().equals(activeMob.mobType, true)
            }
            return@addSimpleCondition false
        }

        addSimpleCondition("skill") { data, it ->
            data.toString().equals(it.skill.name, true)
        }

        addSimpleCondition("classification") { data, it ->
            data.toString().equals(it.classification, true)
        }

        addSimpleCondition("itemLoreContain") { data, it ->
            val item = it.attackHandItem ?: return@addSimpleCondition false
            if (item.isAir()) return@addSimpleCondition false
            val meta = item.itemMeta ?: return@addSimpleCondition false
            val lore = meta.lore ?: return@addSimpleCondition false
            lore.any { line -> line.contains(data.toString()) }
        }

        addSimpleCondition("itemNameContain") { data, it ->
            val item = it.attackHandItem ?: return@addSimpleCondition false
            if (item.isAir()) return@addSimpleCondition false

            item.getName().contains(data.toString())
        }

        addSimpleCondition("itemName") { data, it ->
            val item = it.attackHandItem ?: return@addSimpleCondition false
            if (item.isAir()) return@addSimpleCondition false

            item.getName().equals(data.toString(), true)
        }

        addSimpleCondition("itemNeonflashIdContain") { data, it ->
            val item = it.attackHandItem ?: return@addSimpleCondition false
            if (item.isAir()) return@addSimpleCondition false
            val factory = NeonFlashAPI.itemHandler.readSimple(item)
                ?: return@addSimpleCondition false
            factory.getNeonFlashId().contains(data.toString())
        }



        addSimpleCondition("itemNeonflashId") { data, it ->
            val item = it.attackHandItem ?: return@addSimpleCondition false
            if (item.isAir()) return@addSimpleCondition false
            val factory = NeonFlashAPI.itemHandler.readSimple(item)
                ?: return@addSimpleCondition false
            factory.getNeonFlashId().equals(data.toString(), true)
        }

        addConditionVariable("skill") {
            it.skill.name
        }

        addConditionVariable("classification") {
            it.classification
        }

        addConditionVariable("itemName") {
            val item = it.attackHandItem ?: return@addConditionVariable "AIR"
            if (item.isAir()) return@addConditionVariable "AIR"
            item.getName()
        }

        addConditionVariable("itemNeonflashId") {
            val item = it.attackHandItem ?: return@addConditionVariable "NULL"
            if (item.isAir()) return@addConditionVariable "NULL"
            val factory = NeonFlashAPI.itemHandler.readSimple(item)
                ?: return@addConditionVariable "NULL"
            factory.getNeonFlashId()
        }

    }

}