package com.sucy.skill.hook.chemdah


import com.sucy.skill.api.event.EntitySkillKillEvent
import com.sucy.skill.api.event.MythicMobsKillerEvent
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
object MythicMobsKillerObjective: ObjectiveCountableI<MythicMobsKillerEvent>() {

    override val event: Class<MythicMobsKillerEvent> = MythicMobsKillerEvent::class.java
    override val name: String = "mythic killer"

    init {

        handler {
            it.player
        }

        addSimpleCondition("mobType") { data, it ->
            data.toString().equals(it.target.mobType, true)
        }

        // 是否允许召唤物击杀的通过
        addSimpleCondition("allowSummon") { data, it ->
            if (data.toBoolean()) {
                true
            } else !it.isSummonKiller()
        }


    }

}