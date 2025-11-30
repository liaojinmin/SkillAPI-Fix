package com.sucy.skill.hook.mythic.condition

import com.sucy.skill.hook.mythic.MythicManager
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.skills.SkillCondition
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString
import io.lumine.xikage.mythicmobs.util.annotations.MythicCondition
import io.lumine.xikage.mythicmobs.util.annotations.MythicField

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mythic.condition
 *
 * @author 老廖
 * @since 2025/11/27 19:24
 */
@MythicCondition(
    author = "廖爷爷",
    name = "excludeFaction",
    aliases = ["ef"],
    version = "4.9",
    description = "选项召唤物可攻击的派系"
)
class SkFactionCondition(
    line: String,
    ml: MythicLineConfig,
): SkillCondition(line), IEntityCondition {

    @MythicField(
        name = "faction",
        aliases = ["f"],
        description = "尼玛"
    )
    private var f: PlaceholderString? = null

    init {
        //println("line:$line:end")
        this.f = ml.
        getPlaceholderString(
            arrayOf("faction", "f"),
            this.conditionVar,
            *arrayOfNulls<String>(0)
        )
    }

    override fun check(target: AbstractEntity): Boolean {
        val faction = f ?: return false
        val factions = faction[target].split("|")
        if (target.isPlayer) {
            val maybeFaction = getPlugin().playerManager.factionProvider.getFaction(target.asPlayer())
            return !maybeFaction.isPresent || !factions.contains(maybeFaction.get())
        } else {
            val am = MythicManager.api.getMythicMobInstance(target) ?: return true
            return !am.hasFaction() || !factions.contains(am.faction)
        }
    }





}