package com.sucy.skill.listener;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.mechanic.PotionProjectileMechanic;
import com.sucy.skill.utils.Pair;
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
        final Object obj = SkillAPI.getMeta(event.getEntity(), MechanicListener.POTION_PROJECTILE);
        if (obj instanceof Pair<?, ?>) {
            Pair<SkillContext, PotionProjectileMechanic> pair = (Pair<SkillContext, PotionProjectileMechanic>) obj;

            SkillAPI.setMeta(event.getAreaEffectCloud(), MechanicListener.POTION_PROJECTILE, pair);
            event.getAreaEffectCloud().setMetadata(MechanicListener.SKILL_LEVEL, event.getEntity().getMetadata(MechanicListener.SKILL_LEVEL).get(0));
            event.getAreaEffectCloud().setMetadata(MechanicListener.SKILL_CASTER, event.getEntity().getMetadata(MechanicListener.SKILL_CASTER).get(0));

        }
        /*
        PotionProjectileMechanic mechanic = (PotionProjectileMechanic) SkillAPI.getMeta(event.getEntity(), MechanicListener.POTION_PROJECTILE);
        if (mechanic != null) {
            SkillAPI.setMeta(event.getAreaEffectCloud(), MechanicListener.POTION_PROJECTILE, mechanic);
            event.getAreaEffectCloud().setMetadata(MechanicListener.SKILL_LEVEL, event.getEntity().getMetadata(MechanicListener.SKILL_LEVEL).get(0));
            event.getAreaEffectCloud().setMetadata(MechanicListener.SKILL_CASTER, event.getEntity().getMetadata(MechanicListener.SKILL_CASTER).get(0));
        }

         */
    }

    @EventHandler
    public void onLinger(AreaEffectCloudApplyEvent event) {
        final Object obj = SkillAPI.getMeta(event.getEntity(), MechanicListener.POTION_PROJECTILE);
        if (obj instanceof Pair<?, ?>) {
            Pair<SkillContext, PotionProjectileMechanic> pair = (Pair<SkillContext, PotionProjectileMechanic>) obj;

            pair.value.callback(pair.key, event.getEntity(),event.getAffectedEntities());
            event.getAffectedEntities().clear();
        }

        /*
        PotionProjectileMechanic mechanic = (PotionProjectileMechanic) SkillAPI.getMeta(event.getEntity(), MechanicListener.POTION_PROJECTILE);
        if (mechanic != null) {
            mechanic.callback(event.getEntity(), event.getAffectedEntities());
            event.getAffectedEntities().clear();
        }

         */
    }
}
