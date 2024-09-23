package com.sucy.skill.hook.mythic;

import com.sucy.skill.api.projectile.EntityProjectile2;
import com.sucy.skill.hook.mythic.drop.MythicExpDrop;
import com.sucy.skill.hook.mythic.mechanic.*;
import com.sucy.skill.listener.SkillAPIListener;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicDropLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class MythicListener extends SkillAPIListener {

    private static final HashMap<UUID, CopyOnWriteArraySet<String>> markMap = new HashMap<>();

    public static void addMark(AbstractEntity entity, String... mark) {
        CopyOnWriteArraySet<String> list = markMap.computeIfAbsent(entity.getUniqueId(), (key) -> new CopyOnWriteArraySet<>());
        Collections.addAll(list, mark);

    }

    public static boolean checkNotMark(AbstractEntity entity, String mark) {
        CopyOnWriteArraySet<String> set = markMap.get(entity.getUniqueId());
        if (set == null) {
            return true;
        }
        return !set.remove(mark);
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void call(MythicMechanicLoadEvent event) {
        if (event.getMechanicName().equalsIgnoreCase("castSkillApi")) {
            event.register(new MythicSkillMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("damageAtType")) {
            event.register(new MythicDamageMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("timerAttribute")) {
            event.register(new MythicAttributeMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("markDelay")) {
            event.register(new MythicDelayMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("markSkill")) {
            event.register(new MythicMarkSkillMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("markRandomSkill")) {
            event.register(new MythicMarkRandomSkillMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("return")) {
            event.register(new MythicReturnMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("entitySpread")) {
            event.register(new MythicEntitySpreadMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("summonAllyEntity")) {
            event.register(new MythicSummonAllyMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void call(MythicDropLoadEvent event) {
        if (event.getDropName().equalsIgnoreCase("skillApi")) {
            event.register(new MythicExpDrop(event.getContainer().getConfigLine(), event.getConfig()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(MythicReloadedEvent event) {
        AgentSkill.clear();
        markMap.clear();
        MythicMobs.inst().getSkillManager().runSecondPass();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(EntityDeathEvent event) {
        markMap.remove(event.getEntity().getUniqueId());
        EntityProjectile2.cache.remove(event.getEntity().getUniqueId());
        AgentSkill.delMark(event.getEntity().getUniqueId());
    }


}
