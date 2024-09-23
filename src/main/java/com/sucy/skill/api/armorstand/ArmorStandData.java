package com.sucy.skill.api.armorstand;

import com.sucy.skill.SkillAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorStandData {
    private final ConcurrentHashMap<Integer, ArmorStandInstance> armorStands = new ConcurrentHashMap<>();
    private final LivingEntity target;

    /**
     * @param target target of the armor stands
     */
    public ArmorStandData(LivingEntity target) {
        this.target = target;
    }


    public boolean isValid() {
        return armorStands.size() > 0 && target.isValid();
    }

    /**
     * Fetches an active armor stand by key
     *
     * @param key armor stand key
     *
     * @return active armor stand or null if not found
     */
    public ArmorStandInstance getArmorStands(int key) {
        return armorStands.get(key);
    }

    public Collection<ArmorStandInstance> getArmorStandInstances() {
        return armorStands.values();
    }

    @Nullable
    public ArmorStandInstance del(int key) {
        return armorStands.remove(key);
    }


    public void add(ArmorStandInstance armorStand, int key) {

        armorStands.put(key, armorStand);
    }

    /**
     * Ticks each armor stand for the target
     */
    public void tick() {
        Iterator<ArmorStandInstance> iterator = armorStands.values().iterator();
        while (iterator.hasNext()) {
            ArmorStandInstance armorStand = iterator.next();
            if (armorStand.isValid()) {
                armorStand.tick();
            } else {
                armorStand.remove();
                iterator.remove();
            }
        }
    }

    /**
     * Removes and unregisters all armor stands for this target
     */
    public void remove() {
        armorStands.values().forEach(ArmorStandInstance::remove);
        armorStands.clear();

    }
}
