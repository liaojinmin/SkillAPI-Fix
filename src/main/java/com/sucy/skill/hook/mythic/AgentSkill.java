package com.sucy.skill.hook.mythic;

import com.sucy.skill.hook.mythic.mechanic.MythicDelayMechanic;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.io.ConfigManager;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.logging.MythicLogger;
import io.lumine.xikage.mythicmobs.skills.*;
import io.lumine.xikage.mythicmobs.skills.mechanics.CustomMechanic;
import io.lumine.xikage.mythicmobs.skills.mechanics.DelaySkill;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SkillAPI-Fix
 * com.sucy.skill.hook.mythic
 *
 * @author 老廖
 * @since 2024/7/10 21:19
 */
public class AgentSkill extends Skill {

    private final static ConcurrentHashMap<UUID, List<DelayedSkill>> markMap = new ConcurrentHashMap<>();

    protected LinkedList<SkillMechanic> denySkills = new LinkedList<>();

    public AgentSkill(Skill skill) {
        super(skill.getFile(), skill.getInternalName(), skill.getConfig());
        final List<String> nSkills = skill.getConfig().getStringList("denySkills");
        if (nSkills.isEmpty()) return;
        for (String s : nSkills) {
            s = MythicLineConfig.unparseBlock(s);
            SkillMechanic ms = null;
            try {
                ms = MythicMobs.inst().getSkillManager().getSkillMechanic(s);
            } catch (Exception | Error var14) {
                MythicLogger.errorGenericConfig("Critical Error while attempting to load mechanic line '" + s + "'");
            }

            if (ms != null) {
                this.denySkills.add(ms);
            }
        }
    }

    public void execute(SkillTrigger basetrigger, SkillCaster caster, AbstractEntity trigger, AbstractLocation origin, HashSet<AbstractEntity> eTargets, HashSet<AbstractLocation> lTargets, float power) {
        SkillMetadata data = new SkillMetadata(basetrigger, caster, trigger, origin, eTargets, lTargets, power);
        this.execute(data);
    }

    public void execute(SkillMetadata data) {
        executeAgent(data);
    }

    public void executeAgent(SkillMetadata data) {
        LinkedList<SkillMechanic> skillqueue = (LinkedList)this.skills.clone();
        try {
            if (this.isUsable(data)) {
                executeSkill(data, skillqueue);
            } else {
                if (!denySkills.isEmpty()) {
                    executeSkill(data, (LinkedList) this.denySkills.clone());
                }
            }
        } catch (Exception var4) {
            MythicLogger.error("Couldn't execute skill '" + this.internalName + "': Enable debugging for a stack trace.");
            if (ConfigManager.debugLevel > 0) {
                var4.printStackTrace();
            }
        }
        this.setCooldown(data.getCaster(), this.cooldown);
    }

    public static void addMark(AbstractEntity entity, DelayedSkill task) {
        List<DelayedSkill> tasks = markMap.computeIfAbsent(
                entity.getUniqueId(),
                key -> new CopyOnWriteArrayList<>()
        );
        tasks.add(task);
    }

    private static void removeMarkTask(UUID uuid, DelayedSkill task) {
        List<DelayedSkill> tasks = markMap.get(uuid);
        if (tasks == null) {
            return;
        }
        tasks.remove(task);
        if (tasks.isEmpty()) {
            markMap.remove(uuid, tasks);
        }
    }

    public static void delMark(UUID uuid) {
        List<DelayedSkill> tasks = markMap.remove(uuid);
        if (tasks != null) {
            tasks.forEach(DelayedSkill::cancel);
        }
    }

    public static void clear() {
        for (UUID uuid : new HashSet<>(markMap.keySet())) {
            delMark(uuid);
        }
    }

    public static void cancelMark(AbstractEntity entity, String mark) {
        UUID uuid = entity.getUniqueId();
        MythicListener.removeMark(entity, mark);

        List<DelayedSkill> tasks = markMap.get(uuid);
        if (tasks == null) {
            return;
        }
        for (DelayedSkill task : tasks) {
            if (task.getMark().equalsIgnoreCase(mark)) {
                task.cancel();
            }
        }
        if (tasks.isEmpty()) {
            markMap.remove(uuid, tasks);
        }
    }

    public static void executeSkill(SkillMetadata data, Queue<SkillMechanic> skillqueue) {
        while (!skillqueue.isEmpty()) {
            SkillMechanic sm = skillqueue.poll();
          //  System.out.println("class:"+mbs.getClass());
            if (sm instanceof DelaySkill) {
               // System.out.println("class is DelaySkill");
                AbstractSkill.DelayedSkill ds = new AbstractSkill.DelayedSkill(data, skillqueue);
                Bukkit.getScheduler().runTaskLater(MythicMobs.inst(), ds, ((DelaySkill)sm).getTicks());
                return;
            }
            if (sm instanceof CustomMechanic) {
                final CustomMechanic c = (CustomMechanic) sm;
                if (c.getMechanic().isPresent()) {
                    sm = c.getMechanic().get();
                }
            }
            if (sm instanceof MythicDelayMechanic) {
                //System.out.println("class is MythicDelayMechanic");
                final MythicDelayMechanic delayMechanic = (MythicDelayMechanic) sm;
                final AbstractEntity caster = data.getCaster().getEntity();
                final String mark = delayMechanic.getMark();
                if (MythicListener.checkNotMark(caster, mark)) {
                    DelayedSkill task = new DelayedSkill(data, skillqueue, mark, caster.getUniqueId());
                    addMark(caster, task);
                    task.runTaskLater(MythicMobs.inst(), (long) delayMechanic.getTicks());
                }
                return;
            } else if (sm.isUsableFromSkill(data)) {
                sm.execute(data.deepClone());
            }
        }
    }

    private static class DelayedSkill extends BukkitRunnable {
        private final SkillMetadata data;
        private Queue<SkillMechanic> skills;
        private final String mark;
        private final UUID ownerId;
        private boolean cancelled;

        DelayedSkill(SkillMetadata data, Queue<SkillMechanic> skills, String mark, UUID ownerId) {
            this.data = data;
            this.skills = skills;
            this.mark = mark;
            this.ownerId = ownerId;
        }

        public String getMark() {
            return mark;
        }

        @Override
        public void cancel() {
            if (cancelled) {
                return;
            }
            cancelled = true;
            try {
                super.cancel();
            } finally {
                skills = null;
                removeMarkTask(ownerId, this);
            }
        }

        @Override
        public void run() {
            if (cancelled) {
                return;
            }

            try {
                final AbstractEntity caster = data.getCaster().getEntity();
                if (caster.isDead()
                        && !data.getCause().equals(SkillTrigger.DEATH)
                        && !data.getCause().equals(SkillTrigger.DESPAWNED)
                        && !data.getCause().equals(SkillTrigger.EXPLODE)) {
                    MythicLogger.debug(MythicLogger.DebugLevel.MECHANIC,
                            "! Mob is dead, cancelling skill (cause = {0})", data.getCause());
                    cancel();
                    return;
                }

                if (!mark.isEmpty() && !MythicListener.checkNotMark(caster, mark)) {
                    cancel();
                    return;
                }
                executeSkill(data, skills);
            } catch (NullPointerException exception) {
                cancel();
                exception.printStackTrace();
            } finally {
                removeMarkTask(ownerId, this);
            }
        }
    }
}
