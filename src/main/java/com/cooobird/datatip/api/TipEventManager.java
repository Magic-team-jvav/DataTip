package com.cooobird.datatip.api;

import net.minecraft.world.item.ItemStack;

import java.util.List;
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
    private static final List<Consumer<PreRenderEvent>> PRE_RENDER_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<VariableResolveEvent>> VARIABLE_RESOLVE_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<AppendLinesEvent>> APPEND_LINES_LISTENERS = new CopyOnWriteArrayList<>();

    /**
     * 注册自定义内容渲染前事件。
     */
    public static void onPreRender(Consumer<PreRenderEvent> listener) {
        PRE_RENDER_LISTENERS.add(listener);
    }

    /**
     * 注册变量解析事件。
     */
    public static void onResolveVariable(Consumer<VariableResolveEvent> listener) {
        VARIABLE_RESOLVE_LISTENERS.add(listener);
    }

    /**
     * 注册 tooltip 额外文本追加事件。
     */
    public static void onAppendLines(Consumer<AppendLinesEvent> listener) {
        APPEND_LINES_LISTENERS.add(listener);
    }

    /**
     * 触发自定义内容渲染前事件。
     */
    public static PreRenderEvent firePreRender(ItemStack stack) {
        PreRenderEvent event = new PreRenderEvent(stack);
        for (Consumer<PreRenderEvent> listener : PRE_RENDER_LISTENERS) {
            listener.accept(event);
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
            listener.accept(event);
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
            listener.accept(event);
        }
        return event;
    }

    /**
     * 自定义内容渲染前事件。
     */
    public static class PreRenderEvent {
        private ItemStack itemStack;
        private boolean canceled = false;

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
        private boolean resolved = false;

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
            lines.add(line);
        }

        public List<String> getLines() {
            return lines;
        }
    }
}
