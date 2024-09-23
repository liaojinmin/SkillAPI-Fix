package com.sucy.skill.dynamic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ManaCost;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.player.PlayerSkill;
import com.sucy.skill.dynamic.mechanic.ReturnMechanic;
import com.sucy.skill.dynamic.trigger.Trigger;
import com.sucy.skill.dynamic.trigger.TriggerComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * SkillAPI © 2017
 * com.sucy.dynamic.skill.TriggerHandler
 */
public class TriggerHandler implements Listener {

    private final HashMap<Integer, Integer> active = new HashMap<>();

    private final HashMap<Integer, Runnable> cleanup = new HashMap<>();

    private final DynamicSkill skill;
    private final String key;
    private final Trigger<?> trigger;
    private final TriggerComponent component;


    public TriggerHandler(
            final DynamicSkill skill,
            final String key,
            final Trigger trigger,
            final TriggerComponent component) {

        Objects.requireNonNull(skill, "Must provide a skill");
        Objects.requireNonNull(key, "Must provide a key");
        Objects.requireNonNull(trigger, "Must provide a trigger");
        Objects.requireNonNull(component, "Must provide a component");

        this.skill = skill;
        this.key = key;
        this.trigger = trigger;
        this.component = component;
    }

    public String getKey() {
        return key;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public EffectComponent getComponent() {
        return component;
    }

    public void init(final LivingEntity entity, final int level, Runnable cleanup) {
        this.cleanup.put(entity.getEntityId(), cleanup);
        active.put(entity.getEntityId(), level);
    }

    public void init(final LivingEntity entity, final int level) {
        active.put(entity.getEntityId(), level);
    }

    public void cleanup(final LivingEntity entity) {
      //  if (skill.getName().equalsIgnoreCase("打刀平A1")) {
          //  System.out.println("- 移除生物ID: "+entity.getEntityId()+" 名称: "+entity.getName() + " by 打刀平A1");
       // }
        active.remove(entity.getEntityId());
        component.cleanUp(entity);
    }
    public void register() {
        Bukkit.getPluginManager().registerEvent(
                trigger.getEvent(),
                this,
                EventPriority.HIGHEST,
                ComponentRegistry.getExecutor(trigger), SkillAPI.singleton(), true);
    }

    <T extends Event> void apply(final T event, final Trigger<T> trigger) {
        final LivingEntity caster = trigger.getCaster(event);
        if (caster == null || !active.containsKey(caster.getEntityId())) {
            return;
        }
        final int level = active.get(caster.getEntityId());
        final Runnable c = cleanup.remove(caster.getEntityId());
        final String mark = component.settings.getString(ReturnMechanic.MARK, "");
        if (!mark.isEmpty()) {
           // System.out.println("apply >>> mark: "+mark + " class: "+trigger.getClass());
            final HashSet<String> marks = ReturnMechanic.getMarks(caster.getEntityId());
            if (marks != null) {
                if (marks.isEmpty() || !marks.contains(mark)) {
                   // System.out.println("不包含 mark: "+marks + " 已中断");
                    if (c != null) {
                        c.run();
                    }
                    //cleanup(caster);
                    return;
                }
            }
        }

        if (!trigger.shouldTrigger(event, level, component.settings)) {
            // 被动删除，如果有的话
            if (c != null) {
                c.run();
            }
            return;
        }

        final LivingEntity target = trigger.getTarget(event, component.settings);
        trigger.setValues(event, DynamicSkill.getCastData(caster));
        trigger(caster, target, level);

        if (event instanceof Cancellable) { skill.applyCancelled((Cancellable) event); }
        trigger.postProcess(event, skill);

        // 被动删除，如果有的话
        if (c != null) {
            c.run();
        }
    }

    void trigger(final LivingEntity user, final LivingEntity target, final int level) {
        if (user == null || target == null || component.isRunning() || !SkillAPI.getSettings().isValidTarget(target)) {
            return;
        }

        if (user instanceof Player) {
            final PlayerData data = SkillAPI.getPlayerData((Player) user);
            if (data == null) {
                return;
            }
            final PlayerSkill skill = data.getSkill(this.skill.getName());
            final boolean cd = component.getSettings().getBool("cooldown", false);
            final boolean mana = component.getSettings().getBool("mana", false);

            if ((cd || mana) && !data.check(skill, cd, mana)) { return; }

            if (component.trigger(user, target, level)) {
                if (cd) { skill.startCooldown(); }
                if (mana) { data.useMana(skill.getManaCost(), ManaCost.SKILL_CAST); }

            }
        } else {
            component.trigger(user, target, level);
        }
    }
}
