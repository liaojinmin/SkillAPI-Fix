package com.sucy.skill.api.skills;

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
 * <p>关键语义：
 * <ul>
 *   <li>读操作（{@link #get}、{@link #getAs}、{@link #getInt} 等）会向上回退到父上下文</li>
 *   <li>写操作（{@link #put}、{@link #increment} 等）仅作用于本地，实现“读共享、写隔离”</li>
 *   <li>实现 {@link AutoCloseable}，配合 {@link #onClose} 可在技能结束时统一清理资源</li>
 * </ul>
 */
public class SkillContext extends HashMap<String, Object> implements AutoCloseable {

    // ==================== 内部状态 ====================

    @Nullable
    private final SkillContext parent;
    private final List<SkillContext> children = new ArrayList<>();
    private final List<Runnable> closeCallbacks = new ArrayList<>();
    private final long createdAt = System.currentTimeMillis();
    private volatile boolean closed = false;

    // ==================== 构造 ====================

    public SkillContext() {
        this(null);
    }

    public SkillContext(@Nullable SkillContext parent) {
        this.parent = parent;
    }

    // ========================================================================
    //  一、原始 API（保持向后兼容）
    // ========================================================================

    @SuppressWarnings("unchecked")
    public @NotNull List<String> getStringList(@NotNull String key) {
        return (List<String>) super.computeIfAbsent(key, k -> new ArrayList<String>());
    }

    @SuppressWarnings("unchecked")
    public @NotNull List<Object> getList(@NotNull String key) {
        return (List<Object>) super.computeIfAbsent(key, k -> new ArrayList<Object>());
    }

    @SuppressWarnings("unchecked")
    public @NotNull List<Integer> getIntegerList(@NotNull String key) {
        return (List<Integer>) super.computeIfAbsent(key, k -> new ArrayList<Integer>());
    }

    @SuppressWarnings("unchecked")
    public @NotNull List<Integer> delIntegerList(@NotNull String key) {
        if (super.containsKey(key)) {
            Object removed = super.remove(key);
            if (removed instanceof List) {
                return (List<Integer>) removed;
            }
        }
        return new ArrayList<>();
    }

    // ========================================================================
    //  二、父子作用域
    // ========================================================================

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

    // ========================================================================
    //  三、读取（本地优先，回退父级）
    // ========================================================================

    @Override
    @Nullable
    public Object get(@Nullable Object key) {
        Object v = super.get(key);
        if (v == null && parent != null && key != null) {
            return parent.get(key);
        }
        return v;
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return super.containsKey(key) || (parent != null && key != null && parent.containsKey(key));
    }

    @Override
    @Nullable
    public Object getOrDefault(@Nullable Object key, @Nullable Object defaultValue) {
        Object v = get(key);
        return v != null || containsKey(key) ? v : defaultValue;
    }

    /** 仅读取本地，不继承父级（用于排查问题或强制隔离） */
    @Nullable
    public Object getLocal(@NotNull String key) {
        return super.get(key);
    }

    /** 仅判断本地 */
    public boolean containsKeyLocal(@NotNull String key) {
        return super.containsKey(key);
    }

    /** 显式解析（继承查找）—— 与 {@link #get} 等价，用于语义化表达 */
    @Nullable
    public Object resolve(@NotNull String key) {
        return get(key);
    }

    // ========================================================================
    //  四、类型安全泛型读取
    // ========================================================================

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

    @NotNull
    public <T> T getAs(@NotNull String key, @NotNull Class<T> type, @NotNull T defaultValue) {
        T v = getAs(key, type);
        return v != null ? v : defaultValue;
    }

    @NotNull
    public <T> T requireAs(@NotNull String key, @NotNull Class<T> type) {
        T v = getAs(key, type);
        if (v == null) {
            throw new NoSuchElementException(
                    "Missing context value: " + key + " (" + type.getSimpleName() + ")");
        }
        return v;
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

    // ========================================================================
    //  六、数值原子操作（基于可见值计算，本地写入）
    // ========================================================================

    public int increment(@NotNull String key)              { return increment(key, 1); }
    public int increment(@NotNull String key, int delta) {
        int next = getInt(key, 0) + delta;
        super.put(key, next);
        return next;
    }

    public int decrement(@NotNull String key)              { return increment(key, -1); }
    public int decrement(@NotNull String key, int delta)   { return increment(key, -delta); }

    public long incrementLong(@NotNull String key, long delta) {
        long next = getLong(key, 0L) + delta;
        super.put(key, next);
        return next;
    }

    public double incrementDouble(@NotNull String key, double delta) {
        double next = getDouble(key, 0.0) + delta;
        super.put(key, next);
        return next;
    }

    // ========================================================================
    //  七、列表快捷操作
    // ========================================================================

    @SuppressWarnings("unchecked")
    @NotNull
    public <T> List<T> getList(@NotNull String key, @NotNull Class<T> elementType) {
        return (List<T>) super.computeIfAbsent(key, k -> new ArrayList<>());
    }

    public <T> void addToList(@NotNull String key, @Nullable T value) {
        getList(key).add(value);
    }

    public boolean removeFromList(@NotNull String key, @Nullable Object value) {
        Object o = super.get(key);
        return o instanceof List && ((List<?>) o).remove(value);
    }

    // ========================================================================
    //  八、快照 / 复制 / 链式
    // ========================================================================

    /** 合并父级与本地数据的独立快照（父在前、子覆盖） */
    @NotNull
    public Map<String, Object> snapshot() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (parent != null) {
            result.putAll(parent.snapshot());
        }
        result.putAll(this);
        return result;
    }

    /** 浅复制本地数据（List / Map 做浅拷贝），不携带父引用 */
    @NotNull
    public SkillContext copy() {
        SkillContext copy = new SkillContext();
        for (Map.Entry<String, Object> e : entrySet()) {
            Object v = e.getValue();
            if (v instanceof List) {
                copy.put(e.getKey(), new ArrayList<>((List<?>) v));
            } else if (v instanceof Map) {
                copy.put(e.getKey(), new HashMap<>((Map<?, ?>) v));
            } else {
                copy.put(e.getKey(), v);
            }
        }
        return copy;
    }

    /** 链式写入；value 为 null 时等价于 remove */
    @NotNull
    public SkillContext set(@NotNull String key, @Nullable Object value) {
        if (value == null) {
            remove(key);
        } else {
            put(key, value);
        }
        return this;
    }

    // ========================================================================
    //  九、生命周期
    // ========================================================================

    public boolean isClosed()           { return closed; }
    public long    getCreatedAt()       { return createdAt; }
    public long    getAgeMillis()       { return System.currentTimeMillis() - createdAt; }

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
        super.clear();
    }
}
