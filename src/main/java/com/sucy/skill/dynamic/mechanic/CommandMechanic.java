/**
 * SkillAPI
 * com.sucy.mechanic.dynamic.skill.CommandMechanic
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

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Executes a command for each target
 */
public class CommandMechanic extends MechanicComponent {
    private static final String COMMAND = "command";
    private static final String TYPE    = "type";

    @Override
    public String getKey() {
        return "command";
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
        if (targets.isEmpty() || !settings.has(COMMAND)) {
            return false;
        }
        Object object = settings.getObj(COMMAND, level);
        List<String> commands = new ArrayList<>();
        if (object instanceof String) {
            commands.add((String) object);
        } else if (object instanceof List<?>) {
            commands.addAll((List<String>) object);
        }
        String type = settings.getString(TYPE).toLowerCase();
        for (LivingEntity target : targets) {
            for (String cmd : commands) {
                final String finalCommand = filter(caster, target, cmd);
                if (target instanceof Player) {
                    Player p = (Player) target;
                    if (type.equals("op")) {
                        boolean op = p.isOp();
                        Bukkit.getScheduler().runTask(SkillAPI.singleton(), () -> {
                            try {
                                p.setOp(true);
                                Bukkit.dispatchCommand(p, finalCommand);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            } finally {
                                p.setOp(op);
                            }
                        });
                    } else {
                        Bukkit.getScheduler().runTask(SkillAPI.singleton(), () ->
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand)
                        );
                    }
                } else {
                    Bukkit.getScheduler().runTask(SkillAPI.singleton(), () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand)
                    );
                }
            }
        }

        return true;

    }
}
