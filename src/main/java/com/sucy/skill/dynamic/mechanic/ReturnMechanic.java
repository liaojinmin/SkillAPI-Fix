/**
 * SkillAPI
 * com.sucy.mechanic.dynamic.skill.PurgeMechanic
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
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 用于中断 {@link com.sucy.skill.dynamic.mechanic.DelayMechanic} 技能
 */
public class ReturnMechanic extends MechanicComponent {

    public static final HashMap<Integer, HashSet<String>> markMap = new HashMap<>();

    public static final String MARK = "mark";

    public static void addMark(LivingEntity entity, String mark) {
        HashSet<String> list = ReturnMechanic.markMap.computeIfAbsent(entity.getEntityId(), (key) -> new HashSet<>());
        list.add(mark);
    }

    public static void delMark(LivingEntity entity, String mark) {
        HashSet<String> list = ReturnMechanic.markMap.get(entity.getEntityId());
        if (list != null) {
            list.remove(mark);
        }
    }

    @Override
    public String getKey() {
        return "return";
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
        final String[] mark = settings.getString(MARK, "").split(";");
        if (mark.length == 0) return false;
        for (LivingEntity target : targets) {
            HashSet<String> list = ReturnMechanic.markMap.get(target.getEntityId());
            if (list != null) {
                for (String a : mark) {
                    list.remove(a);
                }
            }
        }
        return true;
    }
}
