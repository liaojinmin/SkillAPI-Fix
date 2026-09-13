package com.sucy.skill.dynamic.target

import com.google.common.collect.ImmutableList
import com.sucy.skill.cast.IIndicator
import me.geek.team.common.TeamManager
import me.neon.arena.utils.getGameTeamManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.target
 *
 * @author 老廖
 * @since 2026/4/5 18:12
 */
class TeamPlayerTarget: TargetComponent() {

    override fun makeIndicators(list: List<IIndicator>, caster: Player, target: LivingEntity, level: Int) {
        makeCircleIndicator(list, caster, 0.5)
    }

    /** {@inheritDoc}  */
    override fun getTargets(caster: LivingEntity, level: Int, targets: List<LivingEntity>): List<LivingEntity> {
        if (caster is Player) {

            val gameTeamManager = caster.getGameTeamManager()
            if (gameTeamManager != null) {
                return gameTeamManager.getGamePlayer().map { it.getPlayer() }
            }
            val teamHandler = TeamManager.getTeamByPlayer(caster) ?: return mutableListOf()
            return teamHandler.part.getPlayers()
        }
        return ImmutableList.of(caster)
    }

    override fun getKey(): String {
        return "team player"
    }
}