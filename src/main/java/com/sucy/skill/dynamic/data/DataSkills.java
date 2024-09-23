package com.sucy.skill.dynamic.data;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataSkills {

    private final static ConcurrentHashMap<UUID, CustomDataStack> data = new ConcurrentHashMap<>();

    public static double getValue(UUID uuid, String key) {
        return getValue(uuid, key, 0.0);
    }

    public static double getValue(UUID uuid, String key, double def) {
        if (data.isEmpty()) {
            return def;
        }
        if (!data.containsKey(uuid)) {
            return def;
        }
        CustomDataStack gets = data.get(uuid);
        if (gets == null) {
            return def;
        }
        CustomData tagData = gets.getMeta(key);
        if (tagData == null) {
            return def;
        }
        return tagData.getValue();
    }

    public static void setValue(UUID uuid, String key, Double value, Integer time) {
        CustomDataStack customDataStack = data.computeIfAbsent(uuid, (it) -> new CustomDataStack(uuid));
        customDataStack.putMeta(key, value, time);
    }

    public static void delMetaStack(UUID uuid) {
        data.remove(uuid);
    }

    @Nullable
    public static CustomDataStack getMetaStack(UUID uuid, boolean create) {
        if (!data.containsKey(uuid)) {
            if (create) {
                data.put(uuid, new CustomDataStack(uuid));
            } else {
                return null;
            }
        }
        return data.get(uuid);
    }

}
