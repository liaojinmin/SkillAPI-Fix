/**
 * SkillAPI
 * com.sucy.mechanic.dynamic.skill.WarpLocMechanic
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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Strikes lightning about each target with an offset
 */
public class WarpLocMechanic extends MechanicComponent
{
    private static final String WORLD = "world";
    private static final String X     = "x";
    private static final String Y     = "y";
    private static final String Z     = "z";
    private static final String YAW   = "yaw";
    private static final String PITCH = "pitch";

    @Override
    public String getKey() {
        return "warp location";
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
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets)
    {
        if (targets.size() == 0)
        {
            return false;
        }

        // Get the world
        String world = settings.getString(WORLD, "current");
        if (world.equalsIgnoreCase("current"))
        {
            world = caster.getWorld().getName();
        }
        World w = Bukkit.getWorld(world);
        if (w == null)
        {
            return false;
        }

        // Get the other values
        double x = settings.getDouble(X, 0.0);
        double y = settings.getDouble(Y, 0.0);
        double z = settings.getDouble(Z, 0.0);
        float yaw = (float) settings.getDouble(YAW, 0.0);
        float pitch = (float) settings.getDouble(PITCH, 0.0);

        Location loc = new Location(w, x, y, z, yaw, pitch);

        for (LivingEntity target : targets)
        {
            target.teleport(loc);
        }
        return targets.size() > 0;
    }
}
