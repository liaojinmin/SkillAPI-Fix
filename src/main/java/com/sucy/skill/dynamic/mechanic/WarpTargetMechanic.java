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

import java.util.List;

/**
 * Strikes lightning about each target with an offset
 */
public class WarpTargetMechanic extends MechanicComponent
{
    private static final String TYPE = "type";

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
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        boolean toCaster = settings.getString(TYPE, "caster to target").toLowerCase().equals("target to caster");
        for (LivingEntity target : targets) {
            Location destination = toCaster ? caster.getLocation() : target.getLocation();

            // 尝试找到一个安全位置
            Location safeLocation = findSafeLocation(destination);
            if (safeLocation == null) safeLocation = destination;
            if (toCaster) {
                target.teleport(safeLocation);
            } else {
                caster.teleport(safeLocation);
            }
        }
        return !targets.isEmpty();
    }

    private Location findSafeLocation(Location loc) {
        World world = loc.getWorld();
        if (world == null) return null;

        int centerX = loc.getBlockX();
        int centerY = loc.getBlockY();
        int centerZ = loc.getBlockZ();

        // 如果玩家在安全位置，直接返回
        if (isSafe(loc)) return loc;

        // 尝试在 3x3 区域搜索安全位置
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int x = centerX + dx;
                int z = centerZ + dz;

                // 向上搜索 2 格以内的安全点
                for (int dy = 0; dy <= 2; dy++) {
                    Location testLoc = new Location(world, x + 0.5, centerY + dy, z + 0.5, loc.getYaw(), loc.getPitch());
                    if (isSafe(testLoc)) return testLoc;
                }
            }
        }

        // 没找到安全位置，返回 null
        return null;
    }

    /**
     * 判断位置是否安全
     */
    private boolean isSafe(Location loc) {
        World world = loc.getWorld();
        if (world == null) return false;

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        // 避免虚空
        if (y < 0) return false;

        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        Block below = world.getBlockAt(x, y - 1, z);

        // 脚和头顶必须可穿过，同时脚下必须可站立
        return feet.getType() == Material.AIR && head.getType() == Material.AIR && below.getType().isSolid();
    }
}
