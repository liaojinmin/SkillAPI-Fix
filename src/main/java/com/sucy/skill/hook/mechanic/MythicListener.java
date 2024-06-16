package com.sucy.skill.hook.mechanic;

import com.sucy.skill.listener.SkillAPIListener;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicDropLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class MythicListener extends SkillAPIListener {


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void call(MythicMechanicLoadEvent event) {
        if (event.getMechanicName().equalsIgnoreCase("castSkillApi")) {
            event.register(new MythicSkillMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("damageAtType")) {
            event.register(new MythicDamageMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
        if (event.getMechanicName().equalsIgnoreCase("timerAttribute")) {
            event.register(new MythicAttributeMechanic(event.getContainer().getConfigLine(), event.getConfig()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void call(MythicDropLoadEvent event) {
        if (event.getDropName().equalsIgnoreCase("skillApi")) {
            event.register(new MythicExpDrop(event.getContainer().getConfigLine(), event.getConfig()));
        }
    }


}
