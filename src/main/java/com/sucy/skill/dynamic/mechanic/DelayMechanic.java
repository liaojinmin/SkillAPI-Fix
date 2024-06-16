/**
 * SkillAPI
 * com.sucy.mechanic.dynamic.skill.DelayMechanic
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

import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;
import java.util.List;

/**
 * Executes child components after a delay
 * 可以使用 {@link com.sucy.skill.dynamic.mechanic.ReturnMechanic} 设置中断
 */
public class DelayMechanic extends MechanicComponent {
    private static final String SECONDS = "delay";

    @Override
    public String getKey() {
        return "delay";
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
    public boolean execute(final LivingEntity caster, SkillContext context, final int level, final List<LivingEntity> targets) {
        if (targets.size() == 0) {
            return false;
        }
        final double seconds = parseValues(caster, SECONDS, level, 2.0);
        final String mark = settings.getString(ReturnMechanic.MARK, "");
        if (!mark.isEmpty()) {
            // 添加标记
            ReturnMechanic.addMark(caster, mark);
        }
        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("SkillAPI"),
                () -> {
                    HashSet<String> list2 = ReturnMechanic.markMap.get(caster.getEntityId());
                    if (mark.isEmpty()) {
                        if (list2 != null && list2.remove("盾牌打断")) {
                          //  System.out.println("已被盾牌阻断 delay 执行 1");
                            return;
                        }
                     //   System.out.println("触发 delay 0");
                        executeChildren(caster, context, level, targets);
                    } else {
                        if (list2 == null) {
                         //   System.out.println("触发 delay 1");
                            executeChildren(caster, context, level, targets);
                        } else {
                            if (list2.remove("盾牌打断")) {
                             //   System.out.println("已被盾牌阻断 delay 执行");
                                return;
                            }
                            if (list2.remove(mark)) {
                           //     System.out.println("触发 delay 2");
                                executeChildren(caster, context, level, targets);
                            }
                        }
                    }
                },
                (long) (seconds * 20)
        );

        return true;
    }
}
