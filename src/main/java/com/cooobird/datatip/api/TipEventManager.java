package com.cooobird.datatip.api;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Tooltip 扩展事件管理器。
 * <p>
 * 只保留当前渲染链路中有明确消费点的扩展事件。
 * </p>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TipEventManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Consumer<PreRenderEvent>> PRE_RENDER_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<VariableResolveEvent>> VARIABLE_RESOLVE_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<AppendLinesEvent>> APPEND_LINES_LISTENERS = new CopyOnWriteArrayList<>();
    private static final Set<String> REPORTED_LISTENER_FAILURES = ConcurrentHashMap.newKeySet();

    /**
     * 注册自定义内容渲染前事件。
     */
    public static void onPreRender(Consumer<PreRenderEvent> listener) {
        PRE_RENDER_LISTENERS.add(java.util.Objects.requireNonNull(listener, "listener"));
    }

    /**
     * 注册变量解析事件。
     */
    public static void onResolveVariable(Consumer<VariableResolveEvent> listener) {
        VARIABLE_RESOLVE_LISTENERS.add(java.util.Objects.requireNonNull(listener, "listener"));
    }

    /**
     * 注册 tooltip 额外文本追加事件。
     */
    public static void onAppendLines(Consumer<AppendLinesEvent> listener) {
        APPEND_LINES_LISTENERS.add(java.util.Objects.requireNonNull(listener, "listener"));
    }

    public static boolean removePreRenderListener(Consumer<PreRenderEvent> listener) {
        return PRE_RENDER_LISTENERS.remove(listener);
    }

    public static boolean removeVariableResolveListener(Consumer<VariableResolveEvent> listener) {
        return VARIABLE_RESOLVE_LISTENERS.remove(listener);
    }

    public static boolean removeAppendLinesListener(Consumer<AppendLinesEvent> listener) {
        return APPEND_LINES_LISTENERS.remove(listener);
    }

    /**
     * 触发自定义内容渲染前事件。
     */
    public static PreRenderEvent firePreRender(ItemStack stack) {
        PreRenderEvent event = new PreRenderEvent(stack);
        for (Consumer<PreRenderEvent> listener : PRE_RENDER_LISTENERS) {
            invoke(listener, event, "pre-render");
            if (event.isCanceled()) break;
        }
        return event;
    }

    /**
     * 触发变量解析事件。
     */
    public static VariableResolveEvent fireVariableResolve(String variableName, ItemStack stack) {
        VariableResolveEvent event = new VariableResolveEvent(variableName, stack);
        for (Consumer<VariableResolveEvent> listener : VARIABLE_RESOLVE_LISTENERS) {
            invoke(listener, event, "variable-resolve");
            if (event.isResolved()) break;
        }
        return event;
    }

    /**
     * 收集额外 tooltip 文本行。
     */
    public static AppendLinesEvent fireAppendLines(ItemStack stack) {
        AppendLinesEvent event = new AppendLinesEvent(stack);
        for (Consumer<AppendLinesEvent> listener : APPEND_LINES_LISTENERS) {
            invoke(listener, event, "append-lines");
        }
        return event;
    }

    private static <T> void invoke(Consumer<T> listener, T event, String eventName) {
        try {
            listener.accept(event);
        } catch (RuntimeException e) {
            String failureKey = eventName + ':' + listener.getClass().getName();
            if (REPORTED_LISTENER_FAILURES.add(failureKey)) {
                LOGGER.warn("DataTip {} listener failed; repeated failures from this listener will be suppressed", eventName, e);
            }
        }
    }

    /**
     * 自定义内容渲染前事件。
     */
    public static class PreRenderEvent {
        private ItemStack itemStack;
        private boolean canceled;

        public PreRenderEvent(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public void setItemStack(ItemStack stack) {
            this.itemStack = stack;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public void cancel() {
            this.canceled = true;
        }
    }

    /**
     * 变量解析事件。
     */
    public static class VariableResolveEvent {
        private final String variableName;
        private final ItemStack itemStack;
        private String value;
        private boolean resolved;

        public VariableResolveEvent(String variableName, ItemStack itemStack) {
            this.variableName = variableName;
            this.itemStack = itemStack;
        }

        public String getVariableName() {
            return variableName;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public boolean isResolved() {
            return resolved;
        }

        public String getValue() {
            return value;
        }

        /**
         * 设置变量值，并标记为已解析。
         */
        public void setValue(String value) {
            this.value = value;
            this.resolved = true;
        }
    }

    /**
     * Tooltip 额外文本追加事件。
     */
    public static class AppendLinesEvent {
        private final ItemStack itemStack;
        private final List<String> lines = new java.util.ArrayList<>();

        public AppendLinesEvent(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public void addLine(String line) {
            lines.add(java.util.Objects.requireNonNull(line, "line"));
        }

        public List<String> getLines() {
            return lines;
        }
    }
}
