package com.sucy.skill.api.skills;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.EntityCastSkillEvent;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.sucy.skill.dynamic.mechanic.WolfMechanic.LEVEL;
import static com.sucy.skill.dynamic.mechanic.WolfMechanic.SKILL_META;

public class SkillCastAPI {

    public static boolean cast(@NotNull LivingEntity caster, @NotNull Skill skill, @NotNull Integer level, @Nullable LivingEntity target) {
        return cast(new SkillContext(), caster, skill, level, target);
    }

    /**
     * 实体释放技能 [强制]
     *
     * @param caster 释放技能的实体
     * @param skill  技能
     * @param level  技能等级
     */
    public static boolean cast(@NotNull final SkillContext context, @NotNull LivingEntity caster, @NotNull Skill skill, @NotNull Integer level, @Nullable LivingEntity target) {

        if (caster instanceof Player) {

            PlayerData player = SkillAPI.getPlayerData(caster.getUniqueId());
            if (player == null) {
                return false;
            }
            return player.cast(context, skill.getName(), target);
        }

        EntityCastSkillEvent event = new EntityCastSkillEvent(caster, skill, level);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;


        if (skill instanceof SkillShot) {
            if (target != null) {
                if (target instanceof ArmorStand || !SkillAPI.getSettings().canAttack(caster, target)) {
                    return false;
                }
            }
            return ((SkillShot) skill).cast(context, caster, level, target);
        }
        if (skill instanceof PassiveSkill) {
            ((PassiveSkill) skill).initialize(context, caster, level);
            SkillAPI.setMeta(caster, SKILL_META, skill);
            SkillAPI.setMeta(caster, LEVEL, level);
            return true;
        }
        return false;
    }

}
