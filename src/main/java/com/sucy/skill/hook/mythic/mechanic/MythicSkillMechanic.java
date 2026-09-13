package com.sucy.skill.hook.mythic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderInt;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class MythicSkillMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected PlaceholderInt level;
    protected PlaceholderString name;
    protected Boolean saveTarget;

    public MythicSkillMechanic(String line, MythicLineConfig mlc) {
        super(line, mlc);
        this.name = PlaceholderString.of(mlc.getString(new String[]{"name", "n"}, "null"));
        this.level = PlaceholderInt.of(mlc.getString(new String[]{"level", "l"}, "1"));
        this.saveTarget = mlc.getBoolean(new String[]{"saveTarget", "st"}, false);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {

        if (target.isDead()) {
            return false;
        }

        if (target.isLiving() && target.getHealth() <= 0.0d) {
            return false;
        }

        AbstractEntity cast = target;
        // 如果保留目标，则把技能施法者换成当前技能的施法者，目标继续遗传到sk
        if (saveTarget) {
            cast = data.getCaster().getEntity();
        }
       // System.out.println("saveTarget: "+saveTarget);
       // System.out.println("  cast: "+cast.getName());
       // System.out.println("  target: "+target.getName());


        Skill skill = SkillAPI.getSkill(name.get(data, cast));
        if (skill == null) {
            return false;
        }
        int level = this.level.get(data, cast);

        if (Bukkit.isPrimaryThread()) {
            if (saveTarget) {
                SkillCastAPI.cast(
                        (LivingEntity) cast.getBukkitEntity(),
                        skill, level,
                        (LivingEntity) target.getBukkitEntity()
                );
            } else {
                SkillCastAPI.cast((LivingEntity) cast.getBukkitEntity(), skill, level);
            }
        } else {
            final LivingEntity castEntity = (LivingEntity) cast.getBukkitEntity();
            Bukkit.getScheduler().runTask(SkillAPI.singleton(), () -> {
                if (saveTarget) {
                    SkillCastAPI.cast(castEntity, skill, level, (LivingEntity) target.getBukkitEntity());
                } else {
                    SkillCastAPI.cast(castEntity, skill, level);
                }
            });
        }
        return false;
    }

}
