package com.sucy.skill.hook.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.mob.MobAttribute;
import com.sucy.skill.api.attribute.mob.MobAttributeData;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import com.sucy.skill.utils.AttributeParseUtils;
import com.sucy.skill.utils.Pair;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderInt;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;

public class MythicAttributeMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected PlaceholderString attribute;

    protected Integer amount;

    protected Long timer;

    public MythicAttributeMechanic(String line, MythicLineConfig mlc) {
        super(line, mlc);
        this.attribute = PlaceholderString.of(mlc.getString(new String[]{"attribute", "a"}));
        this.amount = mlc.getInteger(new String[]{"amount", "at"}, 1);
        this.timer = mlc.getLong(new String[]{"timer", "t"}, 20);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target.isDead()) {
            return false;
        }
        if (target.getHealth() <= 0.0d) {
            return false;
        }
        MobAttributeData mobAttributeData = MobAttribute.getData(target.getUniqueId(), true);
        String pair = SkillAPI.getAttributeManager().normalize(attribute.get(data));
        if (mobAttributeData != null && pair != null) {
            mobAttributeData.addAttribute(pair, amount, timer);
        }

        return false;
    }

}
