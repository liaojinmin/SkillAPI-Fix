/**
 * SkillAPI
 * com.sucy.mechanic.dynamic.skill.WarpTargetMechanic
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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

/**
 * Strikes lightning about each target with an offset
 */
public class WarpTargetMechanic extends MechanicComponent {

    private enum Facing {
        TARGET, // 面朝目标（默认）
        BACK,   // 面朝对方后背（与目标同向）
        LEFT,   // 面朝目标左侧
        RIGHT   // 面朝目标右侧
    }
    private static final Vector UP = new Vector(0, 1, 0);

    private static final String TYPE = "type";
    private static final String FORWARD    = "forward";
    private static final String UPWARD     = "upward";
    private static final String RIGHT      = "right";
    private static final String HORIZONTAL = "horizontal";

    @Override
    public String getKey() {
        return "warp target";
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
    public boolean execute(final SkillContext context, LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        boolean toCaster = settings.getString(TYPE, "caster to target").toLowerCase().equals("target to caster");
        Facing facing = parseFacing(settings.getString("facing", "target"));

        final boolean horizontal = settings.getBool(HORIZONTAL, false);
        final double forward = parseValues(context, caster, FORWARD, level, 0);
        final double upward = parseValues(context, caster, UPWARD, level, 0);
        final double right = parseValues(context, caster, RIGHT, level, 0);


        for (LivingEntity target : targets) {
            final Vector dir = target.getLocation().getDirection().setY(0).normalize();
            if (horizontal) {
                dir.setY(0).normalize();
            }
            final Vector nor = dir.clone().crossProduct(UP);
            dir.multiply(forward);
            dir.add(nor.multiply(right)).setY(upward);

            // 尝试找到一个安全位置
            //Location safe = findSafeLocationNear(src, src.getYaw());

            //if (safe == null) safe = src.clone();
            if (toCaster) {
                Location casterLocation = caster.getLocation().add(dir);
                Location targetLocation = target.getLocation();

                Vector lookDir = casterLocation.toVector().subtract(targetLocation.toVector());
                lookDir.setY(0);
                casterLocation.setDirection(lookDir);

                target.teleport(casterLocation);
            } else {
                Location targetLocation = target.getLocation().add(dir);

                Vector lookDir = target.getLocation().toVector().subtract(targetLocation.toVector());
                lookDir.setY(0);
                targetLocation.setDirection(lookDir);

                caster.teleport(targetLocation);
            }
        }
        return !targets.isEmpty();
    }


    private Facing parseFacing(String value) {
        if (value == null) return Facing.TARGET;
        switch (value.toLowerCase()) {
            case "back":
            case "behind":
            case "same":
                return Facing.BACK;
            case "left":
                return Facing.LEFT;
            case "right":
                return Facing.RIGHT;
            case "target":
            case "front":
            default:
                return Facing.TARGET;
        }
    }


}
