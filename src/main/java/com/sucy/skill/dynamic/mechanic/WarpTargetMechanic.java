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
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        boolean toCaster = settings.getString(TYPE, "caster to target").toLowerCase().equals("target to caster");
        Facing facing = parseFacing(settings.getString("facing", "target"));

        final boolean horizontal = settings.getBool(HORIZONTAL, false);
        final double forward = parseValues(caster, FORWARD, level, 0);
        final double upward = parseValues(caster, UPWARD, level, 0);
        final double right = parseValues(caster, RIGHT, level, 0);


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

    private Location lookAt(Location from, LivingEntity target, Facing facing) {
        Location result = from.clone();
        Vector dir;

        switch (facing) {
            case BACK:
                // 使用目标自身的朝向（水平分量）
                dir = target.getLocation().getDirection().clone();
                dir.setY(0);
                break;
            case LEFT:
                // 目标朝向水平后，逆时针旋转90度
                dir = target.getLocation().getDirection().clone();
                dir.setY(0);
                dir = rotateY(dir, Math.PI / 2); // 顺时针90°
                break;
            case RIGHT:
                // 目标朝向水平后，顺时针旋转90度
                dir = target.getLocation().getDirection().clone();
                dir.setY(0);
                dir = rotateY(dir, -Math.PI / 2); // 顺时针90°
                break;
            case TARGET:
            default:
                dir = target.getLocation()
                        .toVector()
                        .subtract(result.toVector());
                dir.setY(0);
                break;
        }
        dir.normalize();
        result.add(dir.clone().multiply(0.01)); // 微小偏移防止重叠
        result.setDirection(dir);
        return result;
    }

    /**
     * 手动绕 Y 轴旋转向量（兼容没有 rotateAroundY 的版本）
     * @param vector 原始向量（水平面）
     * @param angle  旋转弧度，正值为逆时针（从上方看）
     * @return 旋转后的新向量
     */
    private Vector rotateY(Vector vector, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vector.getX() * cos + vector.getZ() * sin;
        double z = vector.getZ() * cos - vector.getX() * sin;
        return new Vector(x, vector.getY(), z);
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

    private Location findSafeLocationNear(Location center, float yaw) {

        World world = center.getWorld();
        if (world == null) return null;

        int baseX = center.getBlockX();
        int baseY = center.getBlockY();
        int baseZ = center.getBlockZ();

        List<int[]> dirs = getDirectionOffsets(yaw);

        // 1. 先按“朝向优先”
        for (int[] d : dirs) {

            int x = baseX + d[0];
            int z = baseZ + d[1];

            for (int dy = -1; dy <= 2; dy++) {

                Location test = new Location(
                        world,
                        x,
                        baseY + dy,
                        z
                );

                if (isSafe(test)) {
                    return test;
                }
            }
        }

        // 2. 再 fallback：3x3
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

                int x = baseX + dx;
                int z = baseZ + dz;

                for (int dy = -1; dy <= 2; dy++) {

                    Location test = new Location(
                            world,
                            x + 0.5,
                            baseY + dy,
                            z + 0.5
                    );

                    if (isSafe(test)) {
                        return test;
                    }
                }
            }
        }

        // 3. 最后才返回原点
        return center.clone();
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

    private List<int[]> getDirectionOffsets(float yaw) {

        // 将 yaw 转为 0~360
        yaw = (yaw % 360 + 360) % 360;

        // 8方向优先级（前 -> 斜前 -> 侧 -> 斜后 -> 后）
        int[][] dirs;

        if (yaw >= 315 || yaw < 45) {
            // SOUTH (+Z)
            dirs = new int[][]{
                    {0, 1}, {1, 1}, {-1, 1}, {1, 0}, {-1, 0}, {1, -1}, {-1, -1}, {0, -1}
            };
        } else if (yaw < 135) {
            // WEST (-X)
            dirs = new int[][]{
                    {-1, 0}, {-1, 1}, {-1, -1}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {1, 0}
            };
        } else if (yaw < 225) {
            // NORTH (-Z)
            dirs = new int[][]{
                    {0, -1}, {1, -1}, {-1, -1}, {1, 0}, {-1, 0}, {1, 1}, {-1, 1}, {0, 1}
            };
        } else {
            // EAST (+X)
            dirs = new int[][]{
                    {1, 0}, {1, 1}, {1, -1}, {0, 1}, {0, -1}, {-1, 1}, {-1, -1}, {-1, 0}
            };
        }

        return Arrays.asList(dirs);
    }

}
