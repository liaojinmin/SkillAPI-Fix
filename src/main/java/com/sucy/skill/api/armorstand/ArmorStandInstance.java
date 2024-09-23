package com.sucy.skill.api.armorstand;

import com.sucy.skill.SkillAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ArmorStandInstance {

    private static int next = 0;

    private static final Vector UP = new Vector(0, 1, 0);

    private static int getNext() {
        next++;
        return next;
    }
    private final LivingEntity armorStand;
    private final LivingEntity owner;
    private final boolean follow;
    private double forward;
    private double upward;
    private double right;
    private Consumer<AtomicBoolean> runnable = null;
    private int tick = 0;
    private final AtomicBoolean tickAtomic = new AtomicBoolean(true);

    public final int indexID = getNext();


    public ArmorStandInstance(LivingEntity armorStand, LivingEntity owner) {
        this.armorStand = armorStand;
        this.owner = owner;
        this.follow = false;
    }

    public ArmorStandInstance(LivingEntity armorStand, LivingEntity owner,
                              double forward, double upward, double right
    ) {
        this.armorStand = armorStand;
        this.owner = owner;
        this.forward = forward;
        this.upward = upward;
        this.right = right;
        this.follow = true;
    }

    public void setRunnable(Consumer<AtomicBoolean> consumer) {
        this.runnable = consumer;
    }

    public void startRunnable() {
        tickAtomic.set(true);
    }

    public void stopRunnable() {
        tickAtomic.set(false);
    }


    public boolean isValid() {
        return owner.isValid() && armorStand.isValid();
    }

    public void remove() {
        tickAtomic.set(false);
        if (armorStand instanceof ArmorStand) {
            armorStand.setHealth(0);
            armorStand.remove();
        } else {
            Bukkit.getScheduler().runTask(SkillAPI.singleton(), () -> {
                armorStand.setHealth(0);
                armorStand.remove();
            });
        }

    }

    public void move(Location location) {
        if (armorStand.isValid()) {
            Location loc = location.clone();
            Vector dir = loc.getDirection().setY(0).normalize();
            Vector side = dir.clone().crossProduct(UP);
            loc.add(dir.multiply(forward)).add(0, upward, 0).add(side.multiply(right));
            armorStand.teleport(loc);
        }
    }

    public LivingEntity getArmorStand() {
        return armorStand;
    }

    public LivingEntity getOwner() {
        return owner;
    }

    /**
     * Ticks the armor stand
     */
    public void tick() {
        if (tickAtomic.get()) {
            if (follow) {
                Location loc = owner.getLocation().clone();
                Vector dir = loc.getDirection().setY(0).normalize();
                Vector side = dir.clone().crossProduct(UP);
                loc.add(dir.multiply(forward)).add(0, upward, 0).add(side.multiply(right));
                armorStand.teleport(loc);
            }
            if (runnable != null && tick >= 20) {
                tick = 0;
                runnable.accept(tickAtomic);
            } else tick++;
        }
    }
}
