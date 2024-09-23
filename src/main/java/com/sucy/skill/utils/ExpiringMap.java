package com.sucy.skill.utils;

import javax.print.DocFlavor;
import java.util.concurrent.*;

/**
 * SkillAPI-Fix
 * com.sucy.skill.utils
 *
 * @author 老廖
 * @since 2024/8/17 15:22
 */
public class ExpiringMap<K, V> {

    private static class ValueWrapper<V> {
        V value;
        ScheduledFuture<?> future;

        ValueWrapper(V value, ScheduledFuture<?> future) {
            this.value = value;
            this.future = future;
        }
    }

    private final ConcurrentHashMap<K, ValueWrapper<V>> map = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Add or update a key-value pair with an expiration time
    public void put(K key, V value, long delay, TimeUnit timeUnit) {
        ValueWrapper<V> old = map.get(key);
        if (old != null) {
            old.future.cancel(false);
            old.value = value;
            old.future = scheduler.schedule(() -> map.remove(key), delay, timeUnit);
        } else {
            map.put(key, new ValueWrapper<>(value, scheduler.schedule(() -> map.remove(key), delay, timeUnit)));
        }
    }

    public V renew(K key, long delay, TimeUnit timeUnit) {
        ValueWrapper<V> value = map.get(key);
        if (value != null) {
            value.future.cancel(false);
            value.future = scheduler.schedule(() -> map.remove(key), delay, timeUnit);
            return value.value;
        }
        return null;
    }

    // Get the value associated with a key
    public V get(K key) {
        ValueWrapper<V> wrapper = map.get(key);
        return wrapper != null ? wrapper.value : null;
    }

    // Remove a key-value pair
    public V remove(K key) {
        ValueWrapper<V> wrapper = map.remove(key);
        if (wrapper != null) {
            wrapper.future.cancel(false);
            return wrapper.value;
        }
        return null;
    }

    // Check if the map contains a key
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    // Clear all key-value pairs
    public void clear() {
        map.values().forEach(wrapper -> wrapper.future.cancel(false));
        map.clear();
    }

    // Get the size of the map
    public int size() {
        return map.size();
    }
}