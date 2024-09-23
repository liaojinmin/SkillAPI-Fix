package com.sucy.skill.hook.mythic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.api.skills.Skill;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class MythicDamageMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected double value;
    protected PlaceholderString classifier;


    // multiplier   =   目标总生命值百分比伤害
    // missing      =   目标已损失生命值的百分比
    // left         =   目标剩余生命值的百分比
    // edc          =   根据发起者的原版攻击力属性 * 倍数
    // skillLevel
    protected PlaceholderString type;
    protected Boolean trueDamage = false;

    protected Skill skill = new Skill("MythicMobs_Cast_Damage", "Dynamic", Material.ICE, 1) {

    };

    public MythicDamageMechanic(String line, MythicLineConfig mlc) {
        super(line, mlc);
        this.value = mlc.getDouble(new String[]{"value", "v"}, 0.0);
        this.classifier = PlaceholderString.of(mlc.getString(new String[]{"classifier", "class", "c"}, ""));
        this.type = PlaceholderString.of(mlc.getString(new String[]{"type", "t"}, ""));
        this.trueDamage = mlc.getBoolean(new String[]{"true"}, false);
        //System.out.println("trueDamage "+trueDamage);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (value < 0) {
            return false;
        }
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
        LivingEntity damager = (LivingEntity) caster;

        Entity targetSkill = target.getBukkitEntity();
        if (!(targetSkill instanceof LivingEntity)) {
            return false;
        }

        try {
            data.getCaster().setUsingDamageSkill(true);
            data.getCaster().getEntity().setMetadata("doing-skill-damage", true);

            LivingEntity entity = (LivingEntity) targetSkill;
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
                    try {
                        amount = value * damager.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
                    } catch (Throwable e) {
                        amount = value * ((ActiveMob) data.getCaster()).getDamage();
                    }
                    break;
                default:
                    amount = value;
                    break;
            }
            if (data.getCaster() instanceof ActiveMob) {
                ((ActiveMob)data.getCaster()).setLastDamageSkillAmount(amount);
            }
            //System.out.println("攻击伤害: " + amount + " trueDamage: " + trueDamage);
            if (trueDamage) {
                skill.trueDamage(entity, amount, damager);
            } else {
                skill.damage(entity, amount, damager, classifier.get(data), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            data.getCaster().setUsingDamageSkill(false);
            data.getCaster().getEntity().removeMetadata("doing-skill-damage");
        }
        return true;
    }

}
