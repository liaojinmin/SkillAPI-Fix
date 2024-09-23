package com.sucy.skill.api.armorstand;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.listener.MechanicListener;
import com.sucy.skill.task.ArmorStandTask;
import com.sucy.skill.thread.MainThread;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorStandManager {

    private static final ConcurrentHashMap<LivingEntity, ArmorStandData> instances = new ConcurrentHashMap<>();

    public static void init() {
        MainThread.register(new ArmorStandTask());
        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(ArmorStand.class).forEach(as -> {
            if (SkillAPI.getMeta(as, MechanicListener.ARMOR_STAND) != null) as.remove();
        }));
    }

    public static void cleanUp() {
        try {
            instances.values().forEach(ArmorStandData::remove);
            instances.clear();
        } catch (Exception ignored) {

        }
    }

    public static ArmorStandInstance getArmorStand(LivingEntity target, int key) {
        if (!instances.containsKey(target)) {
            return null;
        }
        return instances.get(target).getArmorStands(key);
    }

    @Nullable
    public static ArmorStandData getArmorStandData(LivingEntity entity) {
        return instances.get(entity);
    }

    @Nullable
    public static ArmorStandInstance unregister(LivingEntity target, int key) {
        ArmorStandData data = instances.computeIfAbsent(target, (a) -> new ArmorStandData(target) );
        return data.del(key);
    }

    public static void register(ArmorStandInstance armorStand, LivingEntity target, int key) {
        ArmorStandData data = instances.computeIfAbsent(target, (a) -> new ArmorStandData(target) );
        data.add(armorStand, key);
    }

    public static void tick() {
        Iterator<ArmorStandData> iterator = instances.values().iterator();
        while (iterator.hasNext()) {
            ArmorStandData data = iterator.next();
            if (data.isValid()) {
                data.tick();
            } else {
                data.remove();
                iterator.remove();
            }
        }
    }
}
