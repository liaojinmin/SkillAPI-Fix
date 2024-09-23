package com.sucy.skill.dynamic.mechanic;

import com.google.common.base.Objects;
import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.ComponentRegistry;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.TriggerHandler;
import com.sucy.skill.dynamic.trigger.Trigger;
import com.sucy.skill.dynamic.trigger.TriggerComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * SkillAPI © 2017
 * com.sucy.mechanic.dynamic.skill.TriggerMechanic
 */
public class TriggerMechanic extends MechanicComponent {

    private static final String TRIGGER = "trigger";
    private static final String DURATION = "duration";
    private static final String STACKABLE = "stackable";
    private static final String ONCE = "once";

    private final Map<Integer, List<Context>> CASTER_MAP = new HashMap<>();

    private TriggerHandler triggerHandler;
    private boolean once;
    private boolean stackable;

    @Override
    public void load(final DynamicSkill skill, final DataSection dataSection) {
        super.load(skill, dataSection);

        final String name = settings.getString(TRIGGER, "DEATH");
        final Trigger trigger = ComponentRegistry.getTrigger(name);
        if (trigger == null) {
            throw new IllegalArgumentException("Skill is using invalid trigger for mechanic: " + name);
        }
        final Receiver receiver = new Receiver();
        triggerHandler = new TriggerHandler(skill, "fake", trigger, receiver);
        triggerHandler.register();
        once = settings.getBool(ONCE, true);
        stackable = settings.getBool(STACKABLE, true);
    }

    @Override
    public String getKey() {
        return "trigger";
    }

    @Override
    public boolean execute(
            final LivingEntity caster, SkillContext context, final int level, final List<LivingEntity> targets) {

       // 将主动延时删除改为被动式，防止重复触发时刚好移除上下文导致触发器失效。
       // final int ticks = (int)(20 * parseValues(caster, DURATION, level, 5));
        boolean worked = false;
        final String mark = settings.getString(ReturnMechanic.MARK, "");

        for (final LivingEntity target : targets) {
            if (!stackable && CASTER_MAP.containsKey(target.getEntityId()))
                return false;
            if (!mark.isEmpty()) {
              //  System.out.println("name: "+skill.getName() + " trigger: " + triggerHandler.getTrigger().getKey() +"  mark: "+mark);
                ReturnMechanic.addMark(target, mark);
               // Bukkit.getScheduler().runTaskLater(SkillAPI.singleton(),
                  //      () -> ReturnMechanic.delMark(target, mark), 20);
            }
            if (!CASTER_MAP.containsKey(target.getEntityId())) {
                CASTER_MAP.put(target.getEntityId(), new ArrayList<>());
            }
            final Context context2 = new Context(caster, level);
            triggerHandler.init(target, level, new StopTask(target, context2));



            CASTER_MAP.get(target.getEntityId()).add(context2);

           // SkillAPI.schedule(new StopTask(target, context), ticks);
            worked = true;
        }
        return worked;
    }

    private void remove(final LivingEntity target, final Context context) {
        final List<Context> contexts = CASTER_MAP.get(target.getEntityId());
        if (contexts == null) return;

        contexts.remove(context);
        if (contexts.isEmpty()) {
            CASTER_MAP.remove(target.getEntityId());
            triggerHandler.cleanup(target);
        }
    }

    private class StopTask implements Runnable {

        private final LivingEntity target;
        private final Context context;

        public StopTask(final LivingEntity target, final Context context) {
            this.target = target;
            this.context = context;
        }

        @Override
        public void run() {
            remove(target, context);
        }
    }

    private class Receiver extends TriggerComponent {

        private Receiver() {
            final DataSection data = new DataSection();
            TriggerMechanic.this.settings.save(data);
            this.settings.load(data);
        }

        @Override
        public boolean execute(final LivingEntity caster, SkillContext context, final int level, final List<LivingEntity> targets) {
            if (!CASTER_MAP.containsKey(caster.getEntityId())) return false;

            final List<Context> contexts;
            if (once)
                contexts = CASTER_MAP.remove(caster.getEntityId());
            else
                contexts = CASTER_MAP.get(caster.getEntityId());

            final List<LivingEntity> targetList = new ArrayList<>();
            targetList.add(caster);

            for (final Context context2 : contexts) {
                DynamicSkill.getCastData(context2.caster).put("listen-target", targetList);
                TriggerMechanic.this.executeChildren(context2.caster, context, context2.level, targets);
            }

            return true;
        }
    }

    private static class Context {
        public final LivingEntity caster;
        public final int level;

        public Context(final LivingEntity caster, final int level) {
            this.caster = caster;
            this.level = level;
        }

        @Override
        public boolean equals(final Object other) {
            if (other == this) return true;
            if (!(other instanceof Context)) return false;
            final Context context = (Context) other;
            return context.caster == caster && context.level == level;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(caster, level);
        }
    }
}
