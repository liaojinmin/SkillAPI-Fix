/**
 * SkillAPI
 * com.sucy.util.api.skill.Nearby
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
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
package com.sucy.skill.api.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Fetches nearby entities by going through possible chunks
 * instead of all entities in a world
 */
public class Nearby
{
    /**
     * Gets entities nearby a location using a given radius
     *
     * @param loc    location centered around
     * @param radius radius to get within
     *
     * @return nearby entities
     */
    public static List<Entity> getNearby(Location loc, double radius) {
        List<Entity> result = new ArrayList<>();

        int minX = (int) (loc.getX() - radius) >> 4;
        int maxX = (int) (loc.getX() + radius) >> 4;
        int minZ = (int) (loc.getZ() - radius) >> 4;
        int maxZ = (int) (loc.getZ() + radius) >> 4;

        radius *= radius;

        for (int i = minX; i <= maxX; i++)
            for (int j = minZ; j <= maxZ; j++)
                for (Entity entity : loc.getWorld().getChunkAt(i, j).getEntities())
                    if (entity.getLocation().distanceSquared(loc) < radius)
                        result.add(entity);

        return result;
    }

    /**
     * Fetches entities nearby a location using a given radius
     *
     * @param loc    location centered around
     * @param radius radius to get within
     *
     * @return nearby entities
     */
    public static List<LivingEntity> getLivingNearby(Location loc, double radius) {
        return getLivingNearby(null, loc, radius);
    }

    private static List<LivingEntity> getLivingNearby(Entity source, Location loc, double radius) {
        List<LivingEntity> result = new ArrayList<>();

        int minX = (int) (loc.getX() - radius) >> 4;
        int maxX = (int) (loc.getX() + radius) >> 4;
        int minZ = (int) (loc.getZ() - radius) >> 4;
        int maxZ = (int) (loc.getZ() + radius) >> 4;

        radius *= radius;

        for (int i = minX; i <= maxX; i++)
            for (int j = minZ; j <= maxZ; j++)
                for (Entity entity : loc.getWorld().getChunkAt(i, j).getEntities())
                    if (entity != source
                            && entity instanceof LivingEntity
                            && entity.getWorld() == loc.getWorld()
                            && entity.getLocation().distanceSquared(loc) < radius)
                        result.add((LivingEntity) entity);

        return result;
    }

    public static List<LivingEntity> getLivingNearAABBby(Location loc, double radius) {
        return getLivingNearAABBby(null, loc, radius);
    }

    private static List<LivingEntity> getLivingNearAABBby(Entity source, Location loc, double radius) {
        List<LivingEntity> result = new ArrayList<>();

        int minX = (int) (loc.getX() - radius) >> 4;
        int maxX = (int) (loc.getX() + radius) >> 4;
        int minZ = (int) (loc.getZ() - radius) >> 4;
        int maxZ = (int) (loc.getZ() + radius) >> 4;

        double radiusSq = radius * radius;  // 半径的平方，用于距离比较

        for (int i = minX; i <= maxX; i++) {
            for (int j = minZ; j <= maxZ; j++) {
                for (Entity entity : loc.getWorld().getChunkAt(i, j).getEntities()) {
                    if (entity == source || !(entity instanceof LivingEntity) || entity.getWorld() != loc.getWorld()) {
                        continue;
                    }

                    LivingEntity living = (LivingEntity) entity;
                    Location eLoc = living.getLocation();
                    double halfWidth = living.getWidth() / 2.0;
                    double height = living.getHeight();

                    // 构建实体的碰撞箱 AABB
                    double minXEntity = eLoc.getX() - halfWidth;
                    double maxXEntity = eLoc.getX() + halfWidth;
                    double minYEntity = eLoc.getY();
                    double maxYEntity = eLoc.getY() + height;
                    double minZEntity = eLoc.getZ() - halfWidth;
                    double maxZEntity = eLoc.getZ() + halfWidth;

                    // 计算球心到 AABB 的最近点
                    double closestX = clamp(loc.getX(), minXEntity, maxXEntity);
                    double closestY = clamp(loc.getY(), minYEntity, maxYEntity);
                    double closestZ = clamp(loc.getZ(), minZEntity, maxZEntity);

                    // 最近点与球心的距离平方
                    double dx = closestX - loc.getX();
                    double dy = closestY - loc.getY();
                    double dz = closestZ - loc.getZ();
                    if ((dx * dx + dy * dy + dz * dz) < radiusSq) {
                        result.add(living);
                    }
                }
            }
        }

        return result;
    }

    // 辅助方法：将 value 限制在 [min, max] 范围内
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Gets entities nearby a location using a given radius
     *
     * @param entity entity to get nearby ones for
     * @param radius radius to get within
     *
     * @return nearby entities
     */
    public static List<Entity> getNearby(Entity entity, double radius)
    {
        return getNearby(entity.getLocation(), radius);
    }

    /**
     * Fetches entities nearby a location using a given radius
     *
     * @param entity entity to get nearby ones for
     * @param radius radius to get within
     *
     * @return nearby entities
     */
    public static List<LivingEntity> getLivingNearby(Entity entity, double radius)
    {
        return getLivingNearby(entity, entity.getLocation(), radius);
    }

    public static List<Entity> getNearbyBox(Location loc, double radius)
    {
        List<Entity> result = new ArrayList<Entity>();

        int minX = (int) (loc.getX() - radius) >> 4;
        int maxX = (int) (loc.getX() + radius) >> 4;
        int minZ = (int) (loc.getZ() - radius) >> 4;
        int maxZ = (int) (loc.getZ() + radius) >> 4;

        for (int i = minX; i <= maxX; i++)
            for (int j = minZ; j <= maxZ; j++)
                for (Entity entity : loc.getWorld().getChunkAt(i, j).getEntities())
                    if (boxDistance(entity.getLocation(), loc) < radius)
                        result.add(entity);

        return result;
    }

    public static List<LivingEntity> getLivingNearbyBox(Location loc, double radius)
    {
        List<LivingEntity> result = new ArrayList<LivingEntity>();

        int minX = (int) (loc.getX() - radius) >> 4;
        int maxX = (int) (loc.getX() + radius) >> 4;
        int minZ = (int) (loc.getZ() - radius) >> 4;
        int maxZ = (int) (loc.getZ() + radius) >> 4;

        for (int i = minX; i <= maxX; i++)
            for (int j = minZ; j <= maxZ; j++)
                for (Entity entity : loc.getWorld().getChunkAt(i, j).getEntities())
                    if (entity instanceof LivingEntity && boxDistance(entity.getLocation(), loc) < radius)
                        result.add((LivingEntity) entity);

        return result;
    }

    private static double boxDistance(Location loc1, Location loc2)
    {
        return Math.max(Math.max(Math.abs(loc1.getX() - loc2.getX()), Math.abs(loc1.getY() - loc2.getY())), Math.abs(loc1.getZ() - loc2.getZ()));
    }
}
