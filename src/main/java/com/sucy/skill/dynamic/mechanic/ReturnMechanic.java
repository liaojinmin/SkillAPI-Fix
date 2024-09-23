package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.skills.SkillContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 用于中断 {@link com.sucy.skill.dynamic.mechanic.DelayMechanic} 技能
 */
public class ReturnMechanic extends MechanicComponent {

    private static final HashMap<Integer, HashSet<String>> markMap = new HashMap<>();

    public static final String MARK = "mark";

    @Nullable
    public static HashSet<String> getMarks(int entityID) {
        return markMap.get(entityID);
    }

    public static void addMark(@NotNull Entity entity, @NotNull String mark) {
        HashSet<String> list = ReturnMechanic.markMap.computeIfAbsent(entity.getEntityId(), (key) -> new HashSet<>());
        list.add(mark);
    }

    public static void delMark(@NotNull Entity entity, @NotNull String mark) {
        HashSet<String> list = ReturnMechanic.markMap.get(entity.getEntityId());
        if (list != null) {
            System.out.println("remove "+mark);
            list.remove(mark);
        }
    }

    @Override
    public String getKey() {
        return "return";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        final String[] mark = settings.getString(MARK, "").split(";");

        if (mark.length == 0) return false;
        final String m = (String) context.get("trigger_mark");
        for (String a : mark) {
            if (a.equalsIgnoreCase(m)) {
                context.remove("trigger_mark");
            }
        }
        context.remove("trigger_mark");
        for (LivingEntity target : targets) {
            HashSet<String> list = ReturnMechanic.markMap.get(target.getEntityId());
            if (list != null) {
                for (String a : mark) {
                    list.remove(a);
                }
            }
        }
        return true;
    }
}
