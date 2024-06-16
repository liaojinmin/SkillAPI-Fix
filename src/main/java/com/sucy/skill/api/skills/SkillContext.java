package com.sucy.skill.api.skills;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * SkillAPI-Fix
 * com.sucy.skill.api.skills
 *
 * @author 老廖
 * @since 2024/6/12 6:02
 */
public class SkillContext extends HashMap<String, Object> {

    public SkillContext() {
    }

    public SkillContext(@Nullable Object def) {
        if (def == null) return;
        stringContent(def);
    }

    public SkillContext(@NotNull String key, @NotNull Object def) {
        put(key, def);
    }

    public List<String> getStringList(String key) {
        Object o = computeIfAbsent(key, (k) -> new ArrayList<String>());
        return (List<String>) o;
    }

    public List<Object> getList(String key) {
        Object o = computeIfAbsent(key, (k) -> new ArrayList<Object>());
        return (List<Object>) o;
    }

    public List<Integer> getIntegerList(String key) {
        Object o = computeIfAbsent(key, (k) -> new ArrayList<Integer>());
        return (List<Integer>) o;
    }

    public List<Integer> delIntegerList(String key) {
        if (containsKey(key)) {
            return (List<Integer>) remove(key);
        }
        return new ArrayList<>();
    }


    public String stringContent(@Nullable Object value) {
        if (value != null) {
            put("content", value);
        }
        return get("content").toString();
    }

}
