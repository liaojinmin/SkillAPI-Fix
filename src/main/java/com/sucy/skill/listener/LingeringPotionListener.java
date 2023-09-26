package com.sucy.skill.listener;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.mechanic.PotionProjectileMechanic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;

/**
 * SkillAPI © 2017
 * com.sucy.listener.skill.LingeringPotionListener
 */
public class LingeringPotionListener extends SkillAPIListener {

    @EventHandler
    public void onLingerSplash(LingeringPotionSplashEvent event) {
        PotionProjectileMechanic mechanic = (PotionProjectileMechanic) SkillAPI.getMeta(event.getEntity(), MechanicListener.POTION_PROJECTILE);
        if (mechanic != null) {
            SkillAPI.setMeta(event.getAreaEffectCloud(), MechanicListener.POTION_PROJECTILE, mechanic);
            event.getAreaEffectCloud().setMetadata(MechanicListener.SKILL_LEVEL, event.getEntity().getMetadata(MechanicListener.SKILL_LEVEL).get(0));
            event.getAreaEffectCloud().setMetadata(MechanicListener.SKILL_CASTER, event.getEntity().getMetadata(MechanicListener.SKILL_CASTER).get(0));
        }
    }

    @EventHandler
    public void onLinger(AreaEffectCloudApplyEvent event) {
        PotionProjectileMechanic mechanic = (PotionProjectileMechanic) SkillAPI.getMeta(event.getEntity(), MechanicListener.POTION_PROJECTILE);
        if (mechanic != null) {
            mechanic.callback(event.getEntity(), event.getAffectedEntities());
            event.getAffectedEntities().clear();
        }
    }
}
