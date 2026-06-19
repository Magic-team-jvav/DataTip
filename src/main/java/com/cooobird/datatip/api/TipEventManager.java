package com.cooobird.datatip.api;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 事件管理器。
 * <p>
 * 提供 tooltip 渲染生命周期的事件钩子，
 * 允许其他 mod 在关键节点插入自定义逻辑。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 注册渲染前事件
 * TipEventManager.onPreRender(event -> {
 *     // 修改物品栈
 *     event.setItemStack(customStack);
 * });
 *
 * // 注册渲染后事件
 * TipEventManager.onPostRender(event -> {
 *     // 添加额外信息
 *     event.addExtraLine("来自其他 mod 的信息");
 * });
 *
 * // 注册变量解析事件
 * TipEventManager.onResolveVariable(event -> {
 *     if (event.getVariableName().equals("custom_var")) {
 *         event.setValue("自定义值");
 *     }
 * });
 * }</pre>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TipEventManager {

    // 事件监听器列表
    private static final List<Consumer<PreRenderEvent>> PRE_RENDER_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<PostRenderEvent>> POST_RENDER_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<VariableResolveEvent>> VARIABLE_RESOLVE_LISTENERS = new CopyOnWriteArrayList<>();
    private static final List<Consumer<TooltipShowEvent>> TOOLTIP_SHOW_LISTENERS = new CopyOnWriteArrayList<>();

    // 事件注册方法

    /**
     * 注册渲染前事件监听器。
     * 在 tooltip 开始渲染前触发，可以修改内容或取消渲染。
     */
    public static void onPreRender(Consumer<PreRenderEvent> listener) {
        PRE_RENDER_LISTENERS.add(listener);
    }

    /**
     * 注册渲染后事件监听器。
     * 在 tooltip 渲染完成后触发，可以添加额外信息。
     */
    public static void onPostRender(Consumer<PostRenderEvent> listener) {
        POST_RENDER_LISTENERS.add(listener);
    }

    /**
     * 注册变量解析事件监听器。
     * 在变量解析时触发，可以注入自定义变量值。
     */
    public static void onResolveVariable(Consumer<VariableResolveEvent> listener) {
        VARIABLE_RESOLVE_LISTENERS.add(listener);
    }

    /**
     * 注册 tooltip 显示事件监听器。
     * 在 tooltip 显示/隐藏时触发。
     */
    public static void onTooltipShow(Consumer<TooltipShowEvent> listener) {
        TOOLTIP_SHOW_LISTENERS.add(listener);
    }

    /**
     * 触发渲染前事件。
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
     * 触发渲染后事件。
     */
    public static PostRenderEvent firePostRender(ItemStack stack) {
        PostRenderEvent event = new PostRenderEvent(stack);
        for (Consumer<PostRenderEvent> listener : POST_RENDER_LISTENERS) {
            listener.accept(event);
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
     * 触发 tooltip 显示事件。
     */
    public static TooltipShowEvent fireTooltipShow(ItemStack stack, boolean shown) {
        TooltipShowEvent event = new TooltipShowEvent(stack, shown);
        for (Consumer<TooltipShowEvent> listener : TOOLTIP_SHOW_LISTENERS) {
            listener.accept(event);
        }
        return event;
    }

    /**
     * 渲染前事件。
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
     * 渲染后事件。
     */
    public static class PostRenderEvent {
        private final ItemStack itemStack;
        private final List<String> extraLines = new java.util.ArrayList<>();

        public PostRenderEvent(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public void addExtraLine(String line) {
            extraLines.add(line);
        }

        public List<String> getExtraLines() {
            return extraLines;
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
         * 设置变量值，标记为已解析。
         */
        public void setValue(String value) {
            this.value = value;
            this.resolved = true;
        }
    }

    /**
     * Tooltip 显示事件。
     */
    public record TooltipShowEvent(ItemStack itemStack, boolean shown) {
    }
}
