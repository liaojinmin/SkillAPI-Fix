/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueAddMechanic
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Steven Sucy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.armorstand.ArmorStandInstance;
import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.LivingEntity;
import java.util.List;

/**
 * 结束动作
 */
public class ArmorStandRemoveMechanic extends MechanicComponent {

    private static final String TARGET = "target";

    @Override
    public String getKey() {
        return "armor stand remove";
    }

    private void remove(LivingEntity entity, List<Integer> list) {
        for (int a : list) {
            ArmorStandInstance armorStandInstance = ArmorStandManager.getArmorStand(entity, a);
            if (armorStandInstance != null) {
                armorStandInstance.remove();
            }
        }
    }

    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        boolean target = settings.getBool(TARGET, false);
        List<Integer> list = context.delIntegerList("armor stand");
        if (target) {
            for (LivingEntity entity : targets) {
                remove(entity, list);
            }
        } else {
            remove(caster, list);
        }
        executeChildren(caster, context, level, targets);
        return true;
    }
}
