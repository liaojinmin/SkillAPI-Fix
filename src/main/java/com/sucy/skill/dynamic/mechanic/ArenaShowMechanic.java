package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.List;


public class ArenaShowMechanic extends MechanicComponent {


    private static final String RANGE = "range";
    private static final String TIMER = "timer";

    @Override
    public String getKey() {
        return "arena show";
    }

    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        if (caster instanceof Player) {
            Player player = (Player) caster;
            int duration = (int) (20 * parseValues(caster, TIMER, level, 5));
            double range = parseValues(caster, RANGE, level, 10);
            targets.forEach(it -> {
                Location location = it.getLocation();
                me.neon.arena.manager.GameManager gameManager = me.neon.arena.utils.GameUtilsKt.getGameManager(player);
                if (gameManager != null) {
                    gameManager.showPlayer(location, range, duration);
                }
            });
        }
        return true;
    }
}
