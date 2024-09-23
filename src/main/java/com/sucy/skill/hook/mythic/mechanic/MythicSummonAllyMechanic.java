package com.sucy.skill.hook.mythic.mechanic;

import com.sucy.skill.hook.MythicMobsHook;
import com.sucy.skill.utils.target.TargetHelper;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.ITargetedLocationSkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.util.annotations.MythicMechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mechanic
 *
 * @author 老廖
 * @since 2024/7/3 17:22
 */
@MythicMechanic(
        author = "老廖",
        name = "summonAllyEntity",
        aliases = {"r"},
        description = ""
)
public class MythicSummonAllyMechanic extends SkillMechanic implements ITargetedLocationSkill, ITargetedEntitySkill {

    protected String mob;

    protected double level;

    protected double seconds;

    public MythicSummonAllyMechanic(String skill, MythicLineConfig mlc) {
        super(skill, mlc);
        this.mob = mlc.getString(new String[]{"mobs", "mob", "m"});
        this.level = mlc.getDouble(new String[]{"levels", "level", "l"}, 1.0);
        this.seconds = mlc.getDouble(new String[]{"seconds", "second", "s"}, 1.0);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
       castAtLocation(data, target.getLocation());
        return false;
    }

    @Override
    public boolean castAtLocation(SkillMetadata data, AbstractLocation location) {
        Entity own = data.getCaster().getEntity().getBukkitEntity();
        if (own instanceof LivingEntity) {
            MythicMob mythicMob = MythicMobsHook.mythicMobs.getMobManager().getMythicMob(mob);
            if (mythicMob != null) {
                ActiveMob activeMob = mythicMob.spawn(location, level);
                activeMob.setOwner(own.getUniqueId());
                TargetHelper.addSummonAlly(own.getUniqueId(), activeMob.getUniqueId(), seconds);
                return true;
            }
        }
        return false;
    }
}
