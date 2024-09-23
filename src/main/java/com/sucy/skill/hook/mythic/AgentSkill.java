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
        List<DelayedSkill> map = markMap.computeIfAbsent(entity.getUniqueId(), (it) -> new ArrayList<>());
        map.add(task);
    }

    public static void delMark(UUID uuid) {
        List<DelayedSkill> map = markMap.remove(uuid);
        if (map != null) {
            map.forEach(DelayedSkill::cancel);
        }
    }

    public static void clear() {
        for (Map.Entry<UUID, List<DelayedSkill>> d : markMap.entrySet()) {
            d.getValue().forEach(DelayedSkill::cancel);
        }
        markMap.clear();
    }

    public static void cancelMark(AbstractEntity entity, String mark) {
        List<DelayedSkill> map = markMap.get(entity.getUniqueId());
        if (map != null) {
            Iterator<DelayedSkill> iterator = map.listIterator();
            while (iterator.hasNext()) {
                DelayedSkill delayedSkill = iterator.next();
                if (delayedSkill.getMark().equalsIgnoreCase(mark)) {
                    delayedSkill.cancel();
                    iterator.remove();
                }
            }
        }
    }

    public static void executeSkill(SkillMetadata data, Queue<SkillMechanic> skillqueue) {
        while (skillqueue.size() > 0) {
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
                    DelayedSkill task = new DelayedSkill(data, skillqueue, mark);
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
        private boolean cancelled;

        DelayedSkill(SkillMetadata data, Queue<SkillMechanic> skills, String mark) {
            this.data = data;
            this.skills = skills;
            this.mark = mark;
            this.cancelled = false;
        }

        public String getMark() {
            return mark;
        }

        public void cancel() {
            super.cancel();
            this.skills = null;
            this.cancelled = true;
        }

        public void run() {
            if (!this.cancelled) {
                try {
                    final AbstractEntity caster = this.data.getCaster().getEntity();
                    if (caster.isDead()
                            && !this.data.getCause().equals(SkillTrigger.DEATH)
                            && !this.data.getCause().equals(SkillTrigger.DESPAWNED)
                            && !this.data.getCause().equals(SkillTrigger.EXPLODE))
                    {
                        MythicLogger.debug(MythicLogger.DebugLevel.MECHANIC, "! Mob is dead, cancelling skill (cause = {0})", new Object[]{this.data.getCause()});
                        this.cancel();
                        return;
                    }
                    if (mark.isEmpty()) {
                        executeSkill(this.data, this.skills);
                    } else {
                        if (MythicListener.checkNotMark(caster, mark)) {
                            executeSkill(this.data, this.skills);
                        } else {
                            cancel();
                        }
                    }
                } catch (NullPointerException var2) {
                    this.cancel();
                    var2.printStackTrace();
                }
            }
        }
    }
}
