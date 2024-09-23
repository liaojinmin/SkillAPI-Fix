package com.sucy.skill.dynamic.mechanic;

import com.germ.germplugin.api.GermPacketAPI;
import com.germ.germplugin.api.bean.AnimDataDTO;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * 播放动作
 */
public class GermAnimationStartMechanic extends MechanicComponent {
    private static final String NAME = "name";

    private static final String TIME = "time";

    @Override
    public String getKey() {
        return "germ animation start";
    }

    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        if (targets.size() == 0 || !settings.has(NAME)) {
            return false;
        }

        final String key = settings.getString(NAME);
        final float time = (float) parseValues(caster, TIME, level, 1.0);


        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (LivingEntity target : targets) {
            for (Player player : players) {
                if (target instanceof Player) {
                    GermPacketAPI.sendBendAction(player, target.getEntityId(), new AnimDataDTO(key, time, false));
                } else {
                    GermPacketAPI.sendModelAnimation(player, target.getEntityId(), new AnimDataDTO(key, time, false));
                }
            }
        }
        executeChildren(caster, context, level, targets);
        return true;
    }
}
