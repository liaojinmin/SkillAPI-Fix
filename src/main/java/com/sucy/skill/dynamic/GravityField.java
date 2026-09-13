package com.sucy.skill.dynamic;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic
 *
 * @author 老廖
 * @since 2026/7/14 04:56
 * 引力场技能 - 在指定位置持续吸引生物，生物可自行逃离。
 * <p>
 * 使用示例：
 * <pre>
 * new GravityField(center, 5.0, 0.15, 100, GravityField.Decay.LINEAR_INVERSE, entities -> {
 *     // 技能结束，entities 为结束时仍在范围内的生物
 *     for (LivingEntity e : entities) {
 *         e.damage(10);
 *     }
 * }).start();
 * </pre>
 */
public class GravityField {

    /**
     * 拉力衰减模式
     */
    public enum Decay {
        /** 恒定拉力，不随距离变化 */
        CONSTANT,
        /** 线性反比衰减：strength * (1 - distance/radius)，边缘拉力为0，中心拉力最大 */
        LINEAR_INVERSE,
        /** 线性正比衰减：strength * distance/radius，中心拉力为0，边缘拉力最大 */
        LINEAR_PROPORTIONAL
    }

    private final Location center;
    private final double radius;
    private final double pullStrength;      // 基础拉力（每tick速度增量）
    private final long durationTicks;       // 持续时间（tick）
    private final Decay decayMode;
    private final Consumer<Collection<LivingEntity>> onFinish;
    private Predicate<LivingEntity> targetFilter = null;

    private BukkitTask task;
    private boolean active = false;

    /**
     * @param center       引力场中心坐标（会自动拷贝，不保持引用）
     * @param radius       引力半径（格）
     * @param pullStrength 基础拉力，每 tick 施加的速度增量，建议范围 0.05 ~ 0.3
     * @param durationTicks 持续时间（刻），20刻=1秒
     * @param decayMode    拉力衰减模式
     * @param onFinish     结束回调，参数为技能结束时仍在半径内的所有生物（可能为空列表）
     */
    public GravityField(Location center, double radius, double pullStrength,
                        long durationTicks, Decay decayMode,
                        Consumer<Collection<LivingEntity>> onFinish) {
        this.center = center.clone();
        this.radius = Math.max(0.1, radius);
        this.pullStrength = pullStrength;
        this.durationTicks = Math.max(1, durationTicks);
        this.decayMode = decayMode;
        this.onFinish = onFinish;
    }

    /**
     * 启动引力场效果，重复调用会先停止之前的实例。
     */
    public void start() {
        stop(); // 防止重复启动

        World world = center.getWorld();
        if (world == null) {
            throw new IllegalStateException("引力场中心世界为空");
        }
        active = true;
        task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                try {
                    if (!active || ticks >= durationTicks) {
                        finish();
                        cancel();
                        return;
                    }
                    // 应用引力
                    applyForce(world);
                    ticks++;
                } catch (Exception exception) {
                    exception.printStackTrace();
                    cancel();
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugins()[0], 0L, 1L); // 你需要传入你的插件实例
        // 注意：上面的插件获取方式仅为示例，实际应通过你的插件主类传入。
        // 建议将你的插件实例通过构造函数传入，或使用 JavaPlugin.getProvidingPlugin() 等。
    }

    /**
     * 提前停止引力场（不会触发结束回调）
     */
    public void stop() {
        active = false;
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * 设置目标筛选条件。只有满足此条件的生物才会被作用。
     * 支持链式调用，例如：<br>
     * {@code new GravityField(...).setTargetFilter(e -> e.getType() == EntityType.ZOMBIE).start();}
     *
     * @param filter 判断函数，返回 true 的生物会被影响；传入 null 则清除筛选（影响所有生物）
     * @return 当前实例，便于链式调用
     */
    public GravityField setTargetFilter(Predicate<LivingEntity> filter) {
        this.targetFilter = filter;
        return this;
    }

    /**
     * 每 tick 对范围内生物施加拉力
     */
    private void applyForce(World world) {
        // 获取半径内的所有活体生物
        Collection<LivingEntity> targets = world.getNearbyEntities(
                center, radius, radius, radius
        ).stream()
                .filter(entity ->
                        !(entity instanceof TempEntity) && entity instanceof LivingEntity && entity.isValid()
                        && (targetFilter == null || targetFilter.test((LivingEntity) entity))
                )
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());

        Vector centerVec = center.toVector();

        for (LivingEntity entity : targets) {
            Location loc = entity.getLocation();
            Vector toCenter = centerVec.clone().subtract(loc.toVector());
            double distance = toCenter.length();

            // 防止除零和中心点抖动
            if (distance < 0.01) continue;

            // 计算当前距离下的拉力大小
            double strength = getPullStrength(distance);

            // 方向向量（已归一化）
            Vector direction = toCenter.multiply(1.0 / distance);

            // 施加加速度
            entity.setVelocity(entity.getVelocity().add(direction.multiply(strength)));
        }
    }

    /**
     * 根据衰减模式计算实际拉力
     */
    private double getPullStrength(double distance) {
        double ratio = distance / radius; // 0 中心, 1 边缘
        switch (decayMode) {
            case CONSTANT:
                return pullStrength;
            case LINEAR_INVERSE:
                // 边缘力为0，中心力为 pullStrength
                return pullStrength * (1.0 - Math.min(ratio, 1.0));
            case LINEAR_PROPORTIONAL:
                // 中心力为0，边缘力为 pullStrength
                return pullStrength * Math.min(ratio, 1.0);
            default:
                return pullStrength;
        }
    }

    /**
     * 效果结束，触发回调
     */
    private void finish() {
        active = false;
        if (onFinish == null) return;

        World world = center.getWorld();
        if (world == null) return;

        // 收集结束时仍在范围内的生物
        Collection<LivingEntity> entities = world.getNearbyEntities(
                center, radius, radius, radius
        ).stream()
                .filter(entity ->
                        entity instanceof LivingEntity && entity.isValid()
                                && (targetFilter == null || targetFilter.test((LivingEntity) entity))
                )
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());
        onFinish.accept(entities);
    }

    // ---------- 可选：链式调用辅助 ----------
    public Location getCenter() { return center.clone(); }
    public double getRadius() { return radius; }
    public boolean isActive() { return active; }
}