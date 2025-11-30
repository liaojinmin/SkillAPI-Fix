package com.sucy.skill.api;

import com.rit.sucy.player.Protection;
import com.sucy.skill.hook.PluginChecker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * SkillAPI © 2018
 * com.sucy.skill.data.DefaultCombatProtection
 */
public class DefaultCombatProtection implements CombatProtection {

    @Override
    public boolean canAttack(final Player attacker, final Player defender) {
        return canAttack((LivingEntity) attacker, defender);
    }

    @Override
    public boolean canAttack(final Player attacker, final LivingEntity defender) {
        return canAttack((LivingEntity) attacker, defender);
    }

    @Override
    public boolean canAttack(final LivingEntity attacker, final LivingEntity defender) {
        boolean canAttack;
        if (PluginChecker.isNoCheatActive() && attacker instanceof Player) {
            Player player = (Player) attacker;

            canAttack = Protection.canAttack(attacker, defender);

        } else { canAttack = Protection.canAttack(attacker, defender); }

        return canAttack;
    }
}
