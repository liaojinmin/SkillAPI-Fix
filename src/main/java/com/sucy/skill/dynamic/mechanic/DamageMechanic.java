/**
 * SkillAPI
 * com.sucy.mechanic.dynamic.skill.DamageMechanic
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.UUID;

/**
 * Deals damage to each target
 */
public class DamageMechanic extends MechanicComponent {
    private static final String TYPE       = "type";
    private static final String DAMAGE     = "value";
    private static final String TRUE       = "true";
    private static final String CLASSIFIER = "classifier";

    private static final String KNOCKBACK = "knockback";

    @Override
    public String getKey() {
        return "damage";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        String pString = settings.getString(TYPE, "damage").toLowerCase();
        boolean percent = pString.equals("multiplier") || pString.equals("percent");
        boolean missing = pString.equals("percent missing");
        boolean left = pString.equals("percent left");
        boolean trueDmg = settings.getBool(TRUE, false);
        double damage = 0;
        LivingEntity other = caster;

        if (caster.getMetadata(AttributeAPI.FX_SKILL_API_MASTER).isEmpty()) {
            damage = parseValues(caster, DAMAGE, level, 1.0);
        } else {
            UUID masterId = UUID.fromString(caster.getMetadata(AttributeAPI.FX_SKILL_API_MASTER).get(0).asString());
            Entity master = Bukkit.getEntity(masterId);
            if (master == null || master.isEmpty() || master.isDead()) {
                damage = parseValues(caster, DAMAGE, level, 1.0);
            } else if (master instanceof LivingEntity) {
                other = (LivingEntity) master;
                damage = parseValues((LivingEntity) master, DAMAGE, level, 1.0);
            }
        }
        boolean knockback = settings.getBool(KNOCKBACK, true);
        String classification = settings.getString(CLASSIFIER, "default");
        if (damage < 0) {
            return false;
        }
        boolean range = targets.size() > 1;
        int index = 0;
        for (LivingEntity target : targets) {
            if (target.isDead()) {
                continue;
            }

            double amount = damage;
            if (percent) {
                amount = damage * target.getMaxHealth() / 100;
            } else if (missing) {
                amount = damage * (target.getMaxHealth() - target.getHealth()) / 100;
            } else if (left) {
                amount = damage * target.getHealth() / 100;
            }
            if (trueDmg) {
                skill.trueDamage(target, amount, other);
            } else {
                if (index > 0) {
                    skill.damage(target, amount, other, classification, knockback, range);
                } else {
                    skill.damage(target, amount, other, classification, knockback, false);
                }
                index++;
            }
        }
        return targets.size() > 0;
    }
}
