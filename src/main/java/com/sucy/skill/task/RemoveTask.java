
package com.sucy.skill.task;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.PassiveSkill;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.util.BuffManager;
import com.sucy.skill.api.util.FlagManager;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.mechanic.WolfMechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * A simple task for removing an entity after a duration
 */
public class RemoveTask extends BukkitRunnable {
    private final List<? extends Entity> entities;

    /**
     * Initializes a new task to remove the entity after the
     * given number of ticks.
     *
     * @param entities entities to remove
     * @param ticks    ticks to wait before removing the entity
     */
    public RemoveTask(List<? extends Entity> entities, int ticks) {
        this.entities = entities;
        SkillAPI.schedule(this, ticks);
    }

    /**
     * Removes the entity once the time is up
     */
    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        // Clear skill setup
        for (Entity entity : entities) {
            if (entity.hasMetadata(WolfMechanic.SKILL_META)) {
                final List<String> skills = (List<String>) SkillAPI.getMeta(entity, WolfMechanic.SKILL_META);
                final int level = SkillAPI.getMetaInt(entity, WolfMechanic.LEVEL);
                for (final String skillName : skills) {
                    final Skill skill = SkillAPI.getSkill(skillName);
                    if (skill instanceof PassiveSkill) {
                        ((PassiveSkill) skill).stopEffects((LivingEntity) entity, level);
                    }
                }

                DynamicSkill.clearCastData((LivingEntity) entity);
                FlagManager.removeFlags((LivingEntity) entity);
                BuffManager.clearData((LivingEntity) entity);
            }

            // Remove entity
           // if (entity.isValid()) {
                entity.remove();
           // }
        }
    }
}
