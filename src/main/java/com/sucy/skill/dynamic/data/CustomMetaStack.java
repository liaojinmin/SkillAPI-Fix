package com.sucy.skill.dynamic.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomMetaStack {

    private final UUID uuid;

    private final ConcurrentHashMap<String, CustomMeta> infos = new ConcurrentHashMap<>();

    public CustomMetaStack(UUID uuid) {
        this.uuid = uuid;
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @Nullable
    public CustomMeta getMeta(String key) {
        return getMeta(key, false);
    }

    @Nullable
    public CustomMeta getMeta(String key,  boolean create) {
        if (!infos.containsKey(key)) {
            if (create) {
                infos.put(key, new CustomMeta(key, 0.0, -1));
            }
            return null;
        }
        CustomMeta meta = infos.get(key);
        if (meta.isTimerOut()) {
            infos.remove(key);
            return null;
        } else {
            return meta;
        }
    }

    public void putMeta(String key, Double vale, Integer tick) {
        putMeta(key, vale, tick, "");
    }

    public void putMeta(String key, Double value, Integer tick, String action) {
        if (!infos.containsKey(key)) {
            infos.put(key, new CustomMeta(key, value, tick));
        } else {
            CustomMeta meta = getMeta(key);
            if (meta != null) {
                meta.setValue(value, action);
            }
        }
    }
}
