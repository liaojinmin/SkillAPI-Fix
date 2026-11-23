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
public class GermDosMechanic extends MechanicComponent {
    private static final String DOS = "dos";


    @Override
    public String getKey() {
        return "germ dos";
    }

    @Override
    public boolean execute(final SkillContext context, LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty() || !settings.has(DOS)) {
            return false;
        }
        final String dos = settings.getString(DOS);
        for (LivingEntity target : targets) {
            if (target instanceof Player) GermPacketAPI.sendHudDos((Player) target, dos);
        }
        return true;
    }
}
