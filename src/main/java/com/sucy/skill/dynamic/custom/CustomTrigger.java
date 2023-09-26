package com.sucy.skill.dynamic.custom;

import com.sucy.skill.dynamic.trigger.Trigger;
import org.bukkit.event.Event;

/**
 * SkillAPI © 2018
 * com.sucy.custom.dynamic.skill.CustomTrigger
 */
public interface CustomTrigger<E extends Event> extends Trigger<E>, CustomComponent { }
