package com.sucy.skill.api.skills;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.skills
 *
 * @author 老廖
 * @since 2024/6/12 6:02
 *
 * <p>每次技能执行创建一个独立实例，供 skill / condition / targeter / mechanic
 * 等组件在本次执行中共享临时数据。
 *
 */
public class SkillContext implements AutoCloseable {

    private static final SkillContext GLOBAL_CONTEXT = new SkillContext();

    public static void clearAtContext(final LivingEntity entity) {
        GLOBAL_CONTEXT.del(String.valueOf(entity.getEntityId()));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static Map<String, Object> getAtContext(@NotNull final LivingEntity caster) {
        HashMap<String, Object> map = GLOBAL_CONTEXT.getAs(String.valueOf(caster.getEntityId()), HashMap.class);
        if (map == null) {
            map = new HashMap<>();
            map.put("caster", caster);
            GLOBAL_CONTEXT.set(String.valueOf(caster.getEntityId()), map);
        }
        return map;
    }

    @Nullable
    public static Object setAtGlobal(final LivingEntity caster, @NotNull String key, @Nullable Object value) {
        Map<String, Object> map = getAtContext(caster);
        if (value == null) {
            return map.remove(key);
        } else {
            return map.put(key, value);
        }
    }

    @Nullable
    public static Object getAtGlobal(final LivingEntity caster, @Nullable String key) {
        if (key == null) return null;
        Map<String, Object> map = getAtContext(caster);
        return map.get(key);
    }

    public static Object delAtGlobal(final LivingEntity caster, @Nullable String key) {
        if (key == null) return null;
        Map<String, Object> map = getAtContext(caster);
        return map.remove(key);
    }

    public static boolean containsKeyAtGlobal(final LivingEntity caster, @Nullable String key) {
        Map<String, Object> map = getAtContext(caster);
        return map.containsKey(key);
    }

    @Nullable
    private final SkillContext parent;
    private final List<SkillContext> children = new ArrayList<>();
    private final List<Runnable> closeCallbacks = new ArrayList<>();
    private final HashMap<String, Object> context = new HashMap<>();
    private final long createdAt = System.currentTimeMillis();
    private volatile boolean closed = false;

    public SkillContext() {
        this(null);
    }

    public SkillContext(@Nullable SkillContext parent) {
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    public @NotNull List<Integer> getIntegerList(@NotNull String key) {
        return (List<Integer>) context.computeIfAbsent(key, k -> new ArrayList<Integer>());
    }

    @SuppressWarnings("unchecked")
    public @NotNull List<Integer> delIntegerList(@NotNull String key) {
        if (context.containsKey(key)) {
            Object removed = context.remove(key);
            if (removed instanceof List) {
                return (List<Integer>) removed;
            }
        }
        return new ArrayList<>();
    }

    @Nullable
    public Object set(LivingEntity livingEntity, @NotNull String key, @Nullable Object value, boolean useGlobal) {
        if (useGlobal) {
            return setAtGlobal(livingEntity, key, value);
        }
        if (value == null) {
            return context.remove(key);
        } else {
            return context.put(key, value);
        }
    }

    @Nullable
    public Object get(LivingEntity livingEntity, @Nullable String key, boolean useGlobal) {
        if (key == null) return null;
        if (useGlobal) {
            return SkillContext.getAtGlobal(livingEntity, key);
        }
        Object v = context.get(key);
        if (v == null && parent != null) {
            return parent.get(key);
        }
        return v;
    }

    @Nullable
    public Object del(LivingEntity livingEntity, @Nullable String key, boolean useGlobal) {
        if (key == null) return null;
        if (useGlobal) {
            return SkillContext.delAtGlobal(livingEntity, key);
        }
        if (context.containsKey(key)) {
            return context.remove(key);
        }
        return parent != null ? parent.del(key) : null;
    }

    public boolean containsKey(LivingEntity livingEntity, @NotNull String key, boolean useGlobal) {
        if (useGlobal) return containsKeyAtGlobal(livingEntity, key);
        return context.containsKey(key) || (parent != null && parent.containsKey(key));
    }

    @Nullable
    public Object set(@NotNull String key, @Nullable Object value) {
        if (value == null) {
            return context.remove(key);
        } else {
            return context.put(key, value);
        }
    }

    @Nullable
    public Object get(@Nullable String key) {
        if (key == null) return null;
        Object v = context.get(key);
        if (v == null && parent != null) {
            return parent.get(key);
        }
        return v;
    }

    public Object del(@Nullable String key) {
        if (key == null) return null;
        if (context.containsKey(key)) {
            return context.remove(key);
        }
        return parent != null ? parent.del(key) : null;
    }

    public boolean containsKey(@Nullable String key) {
        return context.containsKey(key) || (parent != null && key != null && parent.containsKey(key));
    }

    @Nullable
    public Object getOrDefault(@Nullable String key, @Nullable Object defaultValue) {
        Object v = get(key);
        return v != null || containsKey(key) ? v : defaultValue;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getAs(@NotNull String key, @NotNull Class<T> type) {
        Object v = get(key);
        if (v == null) {
            return null;
        }
        if (type.isInstance(v)) {
            return (T) v;
        }
        return convert(v, type);
    }

    // ========================================================================
    //  五、基础类型便捷读取（含默认值重载）
    // ========================================================================

    @Nullable
    public String getString(@NotNull String key) {
        return getAs(key, String.class);
    }

    @NotNull
    public String getString(@NotNull String key, @NotNull String defaultValue) {
        String v = getString(key);
        return v != null ? v : defaultValue;
    }

    public int     getInt(@NotNull String key)                 { return getInt(key, 0); }
    public int     getInt(@NotNull String key, int def)        { Integer v = getAs(key, Integer.class); return v != null ? v : def; }

    public long    getLong(@NotNull String key)                { return getLong(key, 0L); }
    public long    getLong(@NotNull String key, long def)      { Long v = getAs(key, Long.class); return v != null ? v : def; }

    public double  getDouble(@NotNull String key)              { return getDouble(key, 0.0); }
    public double  getDouble(@NotNull String key, double def)  { Double v = getAs(key, Double.class); return v != null ? v : def; }

    public float   getFloat(@NotNull String key)               { return getFloat(key, 0f); }
    public float   getFloat(@NotNull String key, float def)    { Float v = getAs(key, Float.class); return v != null ? v : def; }

    public boolean getBoolean(@NotNull String key)               { return getBoolean(key, false); }
    public boolean getBoolean(@NotNull String key, boolean def)  { Boolean v = getAs(key, Boolean.class); return v != null ? v : def; }

    public boolean isClosed()           { return closed; }
    public long    getCreatedAt()       { return createdAt; }
    public long    getAgeMillis()       { return System.currentTimeMillis() - createdAt; }

    @Nullable
    public SkillContext getParent() {
        return parent;
    }

    @NotNull
    public SkillContext createChild() {
        if (closed) {
            throw new IllegalStateException("SkillContext is already closed");
        }
        SkillContext child = new SkillContext(this);
        children.add(child);
        return child;
    }

    @NotNull
    public List<SkillContext> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /** 合并父级与本地数据的独立快照（父在前、子覆盖） */
    @NotNull
    public Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (parent != null) {
            result.putAll(parent.snapshot());
        }
        result.putAll(context);
        return result;
    }

    /** 浅复制本地数据（List / Map 做浅拷贝），不携带父引用 */
    @NotNull
    public SkillContext copy() {
        SkillContext copy = new SkillContext();
        for (Map.Entry<String, Object> e : context.entrySet()) {
            Object v = e.getValue();
            if (v instanceof List) {
                copy.context.put(e.getKey(), new ArrayList<>((List<?>) v));
            } else if (v instanceof Map) {
                copy.context.put(e.getKey(), new HashMap<>((Map<?, ?>) v));
            } else {
                copy.context.put(e.getKey(), v);
            }
        }
        return copy;
    }

    /**
     * 注册关闭回调。
     * <p>若上下文已关闭，回调会立即执行一次。
     */
    @NotNull
    public SkillContext onClose(@NotNull Runnable callback) {
        if (closed) {
            callback.run();
        } else {
            closeCallbacks.add(callback);
        }
        return this;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;

        // 1. 关闭子上下文
        for (SkillContext child : new ArrayList<>(children)) {
            try { child.close(); } catch (Throwable t) { t.printStackTrace(); }
        }
        children.clear();

        // 2. 执行回调
        for (Runnable cb : closeCallbacks) {
            try { cb.run(); } catch (Throwable t) { t.printStackTrace(); }
        }
        closeCallbacks.clear();

        // 3. 清理本地数据
        context.clear();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> T convert(@NotNull Object v, @NotNull Class<T> type) {
        try {
            if (type == String.class) {
                return (T) String.valueOf(v);
            }
            if (v instanceof Number) {
                Number n = (Number) v;
                if (type == Integer.class) return (T) Integer.valueOf(n.intValue());
                if (type == Long.class)    return (T) Long.valueOf(n.longValue());
                if (type == Double.class)  return (T) Double.valueOf(n.doubleValue());
                if (type == Float.class)   return (T) Float.valueOf(n.floatValue());
                if (type == Short.class)   return (T) Short.valueOf(n.shortValue());
                if (type == Byte.class)    return (T) Byte.valueOf(n.byteValue());
            }
            if (v instanceof String) {
                String s = ((String) v).trim();
                if (type == Integer.class) return (T) Integer.valueOf(s);
                if (type == Long.class)    return (T) Long.valueOf(s);
                if (type == Double.class)  return (T) Double.valueOf(s);
                if (type == Float.class)   return (T) Float.valueOf(s);
                if (type == Boolean.class) return (T) Boolean.valueOf(s);
                if (type == Short.class)   return (T) Short.valueOf(s);
                if (type == Byte.class)    return (T) Byte.valueOf(s);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

}
