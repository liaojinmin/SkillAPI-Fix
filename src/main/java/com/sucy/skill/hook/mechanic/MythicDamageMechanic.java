package com.sucy.skill.hook.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.Skill;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class MythicDamageMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected Double value;
    protected PlaceholderString classifier;


    // multiplier   =   目标总生命值百分比伤害
    // missing      =   目标已损失生命值的百分比
    // left         =   目标剩余生命值的百分比
    // edc          =   根据发起者的原版攻击力属性 * 倍数
    protected PlaceholderString type;
    protected Boolean trueDamage;

    protected Skill skill = new Skill("MythicMobs_Cast_Damage", "Dynamic", Material.ICE, 1) {

    };

    public MythicDamageMechanic(String line, MythicLineConfig mlc) {
        super(line, mlc);
        this.value = mlc.getDouble(new String[]{"value", "v"}, 0.0);
        this.classifier = PlaceholderString.of(mlc.getString(new String[]{"classifier", "class", "c"}, ""));
        this.type = PlaceholderString.of(mlc.getString(new String[]{"type", "t"}, ""));
        this.trueDamage = mlc.getBoolean(new String[]{"true"}, false);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target.isDead()) {
            return false;
        }
        if (target.isLiving() && target.getHealth() <= 0.0d) {
            return false;
        }

        Entity caster = data.getCaster().getEntity().getBukkitEntity();
        if (!(caster instanceof LivingEntity)) {
            return false;
        }
        Entity targetSkill = target.getBukkitEntity();
        if (!(targetSkill instanceof LivingEntity)) {
            return false;
        }

        LivingEntity other = (LivingEntity) caster;

        if (value < 0) {
            return false;
        }
        if (target.isDead()) {
            return false;
        }

        String pString = type.get(data).toLowerCase();
        double amount;
        switch (pString) {
            case "multiplier":
                // 目标总生命值百分比伤害
                amount = value * target.getMaxHealth() / 100;
                break;
            case "missing":
                // 已损失生命值的百分比
                amount = value * (target.getMaxHealth() - target.getHealth()) / 100;
                break;
            case "left":
                // 剩余生命值的百分比
                amount = value * target.getHealth() / 100;
                break;
            case "edc":
                amount = value * other.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
                break;
            default:
                amount = value;
                break;
        }

        Bukkit.getScheduler().runTask(SkillAPI.singleton(), () -> {
            if (trueDamage) {
                skill.trueDamage((LivingEntity) targetSkill, amount, other);
            } else {
                skill.damage((LivingEntity) targetSkill, amount, other, classifier.get(data), true);
            }
        });
        return true;
    }

}
