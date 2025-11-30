package com.sucy.skill.api.skills;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.EntityCastSkillEvent;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.utils.target.TargetHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static com.sucy.skill.dynamic.mechanic.WolfMechanic.LEVEL;
import static com.sucy.skill.dynamic.mechanic.WolfMechanic.SKILL_META;

public class SkillCastAPI {

    /**
     * 实体释放技能 [强制]
     *
     * @param caster 释放技能的实体
     * @param skill  技能
     * @param level  技能等级
     */
    public static boolean cast(LivingEntity caster, Skill skill, Integer level) {
        return cast(caster, skill, level, null);
    }

    /**
     * 实体释放技能 [强制]
     *
     * @param caster 释放技能的实体
     * @param skill  技能
     * @param level  技能等级
     */
    public static boolean cast(LivingEntity caster, Skill skill, Integer level, @Nullable LivingEntity target) {

        if (skill == null) {
            throw new IllegalArgumentException("Skill cannot be null");
        }

      //  System.out.println("caster: " + caster.getName() + " skill: "+skill.getName());

      //  if (target != null) {
           // System.out.println("  target: " + target.getName());
   //     } else  {
         //  System.out.println("  target is null");
     //   }

        if (caster instanceof Player) {

            PlayerData player = SkillAPI.getPlayerData(caster.getUniqueId());
            if (player == null) {
                return false;
            }
            return player.cast(skill.getName());
        }

        EntityCastSkillEvent event = new EntityCastSkillEvent(caster, skill, level);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            //System.out.println("EntityCastSkillEvent isCancelled");
            return false;
        }
        if (skill instanceof SkillShot) {
            if (target != null) {

                if (target instanceof ArmorStand) {
                    //System.out.println("target instanceof ArmorStand");
                    return false;
                }

                if (!SkillAPI.getSettings().canAttack(caster, target)) {
                    //System.out.println("target instanceof canAttack");
                    return false;
                }
            }
            return ((SkillShot) skill).cast(caster, level, target);
        }
        if (skill instanceof TargetSkill) {

            if (target == null) {
                target = TargetHelper.getLivingTarget(caster, skill.getRange(level));
                if (target == null) {
                    return false;
                }
            }
            boolean canAttack = !SkillAPI.getSettings().canAttack(caster, target);
            return ((TargetSkill) skill).cast(caster, target, level, canAttack);
        }
        if (skill instanceof PassiveSkill) {
            ((PassiveSkill) skill).initialize(caster, level);
            SkillAPI.setMeta(caster, SKILL_META, skill);
            SkillAPI.setMeta(caster, LEVEL, level);
            return true;
        }
        return false;
    }

}
