package com.sucy.skill.utils;

import org.bukkit.event.*;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import java.lang.reflect.Method;

/**
 * SkillAPI-Fix
 * com.sucy.skill.utils
 *
 * @author 老廖
 * @since 2025/10/28 03:16
 */
public final class EventCancelTracker {


        public static void hookEventHandlers(final Class<? extends Event> eventClass) {
            try {
                Method getHandlerList = eventClass.getMethod("getHandlerList");

                HandlerList handlerList = (HandlerList) getHandlerList.invoke(null);

                RegisteredListener[] listeners = handlerList.getRegisteredListeners();
                for (RegisteredListener listener : listeners) {

                    final Plugin plugin = listener.getPlugin();
                    final Listener originListener = listener.getListener();

                    // 构造一个代理监听器
                    RegisteredListener proxy = new RegisteredListener(
                            originListener,
                            new EventExecutor() {
                                @Override
                                public void execute(Listener ignored, Event event) throws EventException {
                                    boolean before = (event instanceof Cancellable) && ((Cancellable) event).isCancelled();

                                    // 调用原始监听器逻辑
                                    listener.callEvent(event);

                                    if (event instanceof Cancellable) {
                                        boolean after = ((Cancellable) event).isCancelled();
                                        if (!before && after) {
                                            System.out.println("[EventCancelTracer] " + event.getEventName() +
                                                    " 被取消 | 取消者: " + originListener.getClass().getName() +
                                                    " | 插件: " + plugin.getName() +
                                                    " | 优先级: " + listener.getPriority());
                                        }
                                    }
                                }
                            },
                            listener.getPriority(),
                            plugin,
                            listener.isIgnoringCancelled()
                    );

                    // 替换监听器
                    handlerList.unregister(listener);
                    handlerList.register(proxy);
                }

                System.out.println("[EventCancelTracer] 已 Hook: " + eventClass.getSimpleName());

            } catch (Exception e) {
                System.out.println("[EventCancelTracer] Hook 失败: " + eventClass.getSimpleName());
                e.printStackTrace();
            }
        }


}

