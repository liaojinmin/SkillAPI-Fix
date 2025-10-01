package com.sucy.skill.api.attribute.mob;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MobAttribute {

    public static ConcurrentHashMap<UUID, MobAttributeData> data = new ConcurrentHashMap<>();

    public static List<MobAttributeData> getData(String name) {
        return data.values().stream()
                .filter(it -> it.getDisplay().contains(name))
                .collect(Collectors.toList());
    }

    public static MobAttributeData getData(UUID uuid, boolean create) {
        return getData(Bukkit.getEntity(uuid), create);
    }

    public static MobAttributeData getData(Entity entity, boolean create) {
        if (entity == null || !(entity instanceof LivingEntity)) return null;
        if (create) {
            return data.computeIfAbsent(entity.getUniqueId(), key -> new MobAttributeData((LivingEntity) entity));
        }
        return data.get(entity.getUniqueId());
    }

}
