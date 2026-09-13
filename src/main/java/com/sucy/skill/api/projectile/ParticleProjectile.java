/**
 * SkillAPI
 * com.sucy.projectile.api.skill.ParticleProjectile
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
package com.sucy.skill.api.projectile;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.armorstand.ArmorStandInstance;
import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.api.event.ParticleProjectileExpireEvent;
import com.sucy.skill.api.event.ParticleProjectileHitEvent;
import com.sucy.skill.api.event.ParticleProjectileLandEvent;
import com.sucy.skill.api.event.ParticleProjectileLaunchEvent;
import com.sucy.skill.api.util.ParticleHelper;
import com.sucy.skill.dynamic.ArmorStandCarrier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * A fake projectile that plays particles along its path
 */
public class ParticleProjectile extends CustomProjectile {

    /**
     * Settings key for the projectile speed
     */
    public static final String SPEED = "velocity";

    /**
     * Settings key for the projectile lifespan
     */
    private static final String LIFESPAN = "lifespan";

    /**
     * Settings key for the projectile's frequency of playing particles
     */
    private static final String FREQUENCY = "frequency";

    /**
     * Settings key for the projectile's effective gravity
     */
    private static final String GRAVITY = "gravity";

    private static final String PIERCE = "pierce";

    protected Location loc;

    protected final Settings settings;

    protected Double radius;

    protected Vector vel;

    protected final int steps;

    protected int count;
    protected final int freq;
    protected int life;
    protected final Vector gravity;
    protected final boolean pierce;

    public ParticleProjectile(LivingEntity shooter, int level, Location loc, Settings settings) {
        super(shooter);
        this.loc = loc;
        this.settings = settings;
        this.vel = loc.getDirection().multiply(settings.getAttr(SPEED, level, 1.0));
        this.freq = (int) (20 * settings.getDouble(FREQUENCY, 0.5));
        this.life = (int) (settings.getDouble(LIFESPAN, 2) * 20);
        this.gravity = new Vector(0, settings.getDouble(GRAVITY, 0), 0);
        this.pierce = settings.getBool(PIERCE, false);
        this.radius = settings.getDouble("radius", 1.5);
        steps = (int) Math.ceil(vel.length() * 2);
        vel.multiply(1.0 / steps);
        gravity.multiply(1.0 / steps);
        Bukkit.getPluginManager().callEvent(new ParticleProjectileLaunchEvent(this));
    }

    /**
     * Retrieves the location of the projectile
     *
     * @return location of the projectile
     */
    @Override
    public Location getLocation() {
        return loc;
    }

    /**
     * Handles 由于范围或离开加载的块而过期
     */
    @Override
    protected Event expire() {
        return new ParticleProjectileExpireEvent(this);
    }

    /**
     * Handles 命中地面
     */
    @Override
    protected Event land() {
        return new ParticleProjectileLandEvent(this);
    }

    @Override
    protected Event hit(LivingEntity entity) {
        return new ParticleProjectileHitEvent(this, entity);
    }

    @Override
    protected boolean landed() {
        return getLocation().getBlock().getType().isSolid();
    }

    @Override
    protected double getCollisionRadius() {
        return radius;
    }

    protected void applySteps() {
        // 尝试修复因为世界卸载，tick也就运行导致的问题。2026/8/15
        World world = loc.getWorld();
        if (world == null || Bukkit.getWorld(world.getUID()) == null) {
            System.out.println("世界不存在，中断，任务ID: "+getTaskId());
            cancel();
            Bukkit.getPluginManager().callEvent(expire());
            return;
        }

        // Go through multiple steps to avoid tunneling
        for (int i = 0; i < steps; i++) {
            loc.add(vel);
            //  loc.setDirection(vel);
            vel.add(gravity);

            if (!isTraveling()) {
                return;
            }
            if (!checkCollision(pierce)) break;
        }
    }

    @Override
    public Vector getVelocity() {
        return vel;
    }

    public void teleport(Location loc) {
        this.loc = loc;
    }

    @Override
    public void setVelocity(Vector vel) {
        this.vel = vel;
    }

    @Override
    public void run() {
        applySteps();

        // Particle along path
        count++;
        if (count >= freq) {
            count = 0;
            ParticleHelper.play(loc, settings, getShooter());
        }

        // Lifespan
        life--;
        if (life <= 0) {
            //System.out.println("抛射物结束...");
            cancel();
            Bukkit.getPluginManager().callEvent(expire());
        }
    }

    /**
     * Fires a spread of projectiles from the location.
     * @return list of fired projectiles
     */
    public static ArrayList<ParticleProjectile> spread(
            LivingEntity shooter,
            int level,
            Vector center,
            Location loc,
            Settings settings,
            double angle,
            int amount,
            ProjectileCallback callback
    ) {
        ArrayList<Vector> dirs = calcSpread(center, angle, amount);
        ArrayList<ParticleProjectile> list = new ArrayList<>();

        for (Vector dir : dirs) {
            Location l = loc.clone();
            l.setDirection(dir);
            ParticleProjectile p;
            if (settings.getBool(CarrierProjectile.ARMOR_STAND, false)) {
                p = new CarrierProjectile(shooter, level, l, settings);
            } else {
                p = new ParticleProjectile(shooter, level, l, settings);
            }
            p.setCallback(callback);
            list.add(p);
        }
        return list;
    }

    /**
     * Fires a spread of projectiles from the location.
     * @return list of fired projectiles
     */
    public static ArrayList<ParticleProjectile> rain(
            LivingEntity shooter,
            int level,
            Location center,
            Settings settings,
            double radius,
            double height,
            int amount,
            ProjectileCallback callback
    ) {
        Vector vel = new Vector(0, 1, 0);
        ArrayList<Location> locs = calcRain(center, radius, height, amount);
        ArrayList<ParticleProjectile> list = new ArrayList<>();
        for (Location l : locs) {
            l.setDirection(vel);

            ParticleProjectile p;
            if (settings.getBool(CarrierProjectile.ARMOR_STAND, false)) {
                p = new CarrierProjectile(shooter, level, l, settings);
            } else {
                p = new ParticleProjectile(shooter, level, l, settings);
            }

            p.setCallback(callback);
            list.add(p);
        }
        return list;
    }
}
