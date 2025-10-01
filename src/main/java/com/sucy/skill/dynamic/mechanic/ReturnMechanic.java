package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.germ.GermPluginAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 用于中断技能
 * 可用位置:
 * {@link com.sucy.skill.dynamic.mechanic.DelayMechanic}
 * {@link com.sucy.skill.dynamic.TriggerHandler}
 * {@link com.sucy.skill.dynamic.mechanic.TriggerMechanic}
 */
public class ReturnMechanic extends MechanicComponent {

    private static final HashMap<Integer, EntityMark> taskMark = new HashMap<>();

    public static final String DENY_GERM_ACTION = "deny-germ-action";

    public static final String DENY_COMMANDS = "deny-commands";

    public static final String MARK = "mark";

    public static class MarkTask {

        private final Runnable runnable;

        private final long delay;

        private BukkitTask bukkitTask;

        private Supplier<Boolean> callback = null;

        public MarkTask(Runnable runnable, long delay) {
            this.runnable = runnable;
            this.delay = delay;
        }

        private void start() {
            if (bukkitTask == null) {
                bukkitTask = Bukkit.getScheduler()
                        .runTaskLater(SkillAPI.singleton(), () -> {
                            if (callback != null) {
                                if (callback.get()) {
                                    runnable.run();
                                }
                            } else {
                                runnable.run();
                            }
                        }, delay);
            }
        }

        private void close() {
            if (bukkitTask != null) {
                System.out.println("  >>> bukkitTask 中断关闭成功...");
                bukkitTask.cancel();
            }
        }
    }

    private static class Mark {

        private final Long uniqueId;

        private final @Nullable MarkTask markTask;

        private final String mark;

        private Mark(@NotNull Long uniqueId, @NotNull MarkTask markTask, @NotNull String mark) {
            this.uniqueId = uniqueId;
            this.markTask = markTask;
            this.mark = mark;
        }

        private Mark(@NotNull Long uniqueId, @NotNull String mark) {
            this.uniqueId = uniqueId;
            this.markTask = null;
            this.mark = mark;
        }

        private void close() {
            if (markTask != null) {
                System.out.println("MarkTask " + uniqueId + " 尝试中断关闭...");
                markTask.close();
            }
        }

        private void start(@Nullable Supplier<Boolean> callback) {
            if (markTask != null) {
                if (callback != null) {
                    markTask.callback = callback;
                }
                markTask.start();

            }
        }
    }

    private static class EntityMark {

        private final LivingEntity livingEntity;

        // 用 uniqueId 管理 Mark
        private final Map<Long, Mark> idMap = new HashMap<>(100);

        // 用 mark -> uniqueIds 的二级索引
        private final Map<String, Set<Long>> markIndex = new HashMap<>(100);

        private final AtomicLong counter = new AtomicLong(0);

        private EntityMark(LivingEntity livingEntity) {
            this.livingEntity = livingEntity;
        }

        private void addMark(@NotNull MarkTask markTask, @NotNull String mark) {
            add(markTask, mark);
        }

        private void addMark(@NotNull String mark) {
            add(null, mark);
        }

        private boolean delMark(@NotNull String... marks) {
            boolean back = false;
            for (String mark : marks) {
                Set<Long> ids = markIndex.remove(mark);
                if (ids != null) {
                    for (Long id : ids) {
                        Mark old = idMap.remove(id);
                        if (old != null) {
                            back = true;
                            old.close();
                        }
                    }
                }
            }
            return back;
        }

        private boolean hasMark(@NotNull String mark) {
            Set<Long> ids = markIndex.get(mark);
            return ids != null && !ids.isEmpty();
        }

        private void clear() {
            // 关闭所有任务
            for (Mark m : idMap.values()) {
                m.close();
            }
            idMap.clear();
            markIndex.clear();
        }

        private void add(@Nullable MarkTask markTask, @NotNull String mark) {
            final long id = nextId();
            Mark newMark = (markTask == null)
                    ? new Mark(id, mark)
                    : new Mark(id, markTask, mark);

            // 覆盖旧的
            Mark old = idMap.put(id, newMark);
            if (old != null) {
                old.close();
                markIndex.computeIfPresent(old.mark, (k, v) -> {
                    v.remove(old.uniqueId);
                    return v.isEmpty() ? null : v;
                });
            }

            // 建立索引
            markIndex.computeIfAbsent(mark, k -> new HashSet<>()).add(id);

            // 启动任务，回调时移除
            newMark.start(() -> {
                idMap.remove(id);
                markIndex.computeIfPresent(mark, (k, v) -> {
                    v.remove(id);
                    return v.isEmpty() ? null : v;
                });
                return true;
            });
        }

        private long nextId() {
            while (true) {
                long current = counter.get();
                long next = (current == Long.MAX_VALUE ? 0 : current + 1);
                if (counter.compareAndSet(current, next)) {
                    return next;
                }
            }
        }
    }

    public static void joinPlayer(Player player) {
        EntityMark old =  taskMark.put(player.getEntityId(), new EntityMark(player));
        if (old != null) {
            old.clear(); // 清理旧数据，关闭任务
        }
    }

    public static void quitPlayer(Player player) {
        EntityMark entityMark = taskMark.remove(player.getEntityId());
        if (entityMark != null) {
            entityMark.clear(); // 清理 mark 数据
        }
    }

    public static boolean hasMark(@NotNull LivingEntity entity, String mark) {
        EntityMark entityMark = taskMark.get(entity.getEntityId());
        return entityMark != null && entityMark.hasMark(mark);
    }

    public static void addMark(@NotNull LivingEntity entity, @Nullable MarkTask markTask, @NotNull String mark) {
        if (mark.isEmpty() || mark.equalsIgnoreCase("标记名称")) {
            if (markTask != null) {
                markTask.start();
            }
            return;
        }
        EntityMark entityMark;
        if (entity instanceof Player) {
            entityMark = taskMark.get(entity.getEntityId());
        } else {
            entityMark = taskMark.computeIfAbsent(entity.getEntityId(), (key) ->
                    new EntityMark(entity)
            );
        }
        if (markTask == null) {
            entityMark.addMark(mark);
        } else entityMark.addMark(markTask, mark);
    }

    private static boolean delMark(@NotNull Entity entity, @NotNull String... mark) {
        EntityMark entityMark = taskMark.get(entity.getEntityId());
        return entityMark != null && entityMark.delMark(mark);
    }

    public static void applyDeny(
            @NotNull LivingEntity caster,
            @NotNull LivingEntity target,
            @NotNull String germAction,
            @NotNull List<String> commands
    ) {
        if (!germAction.isEmpty()) {
            GermPluginAPI.INSTANCE.playAction(target, germAction);
        }
        if (commands.isEmpty()) return;
        try {
            for (String cmd : commands) {
                final String finalCommand = parseVars(caster, target, cmd);
                Bukkit.getScheduler().runTask(SkillAPI.singleton(), () ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getKey() {
        return "return";
    }

    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        final String[] mark = settings.getString(MARK, "").split(";");
        if (mark.length == 0) return false;
        String germAction = settings.getString(DENY_GERM_ACTION, "");
        List<String> commands = settings.getStringList(DENY_COMMANDS);
        context.remove("trigger_mark");
        for (LivingEntity target : targets) {
            if (delMark(target, mark)) {
                System.out.println("ReturnMechanic caster: " + caster.getName() + " target: "+target.getName() + " mark >> "+ Arrays.toString(mark));
                System.out.println("  执行动作: "+germAction);
                applyDeny(caster, target, germAction, commands);
            }
        }
        return true;
    }

}
