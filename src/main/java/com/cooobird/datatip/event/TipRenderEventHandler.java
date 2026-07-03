package com.cooobird.datatip.event;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipEventManager;
import com.cooobird.datatip.api.component.TipContentTooltipComponent;
import com.cooobird.datatip.api.loader.TipContentLoader;
import com.cooobird.datatip.api.util.PerformanceOptimizer;
import com.cooobird.datatip.config.DatatipConfig;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Tooltip 渲染事件处理器。
 * <p>
 * 处理 {@link RenderTooltipEvent} 来渲染自定义内容（文本、物品、进度条等）。
 * 负责将 JSON 配置的内容集成到原版 tooltip 中。
 * </p>
 *
 * <h3>渲染流程</h3>
 * <ol>
 *   <li>监听 {@link RenderTooltipEvent.GatherComponents} 事件</li>
 *   <li>根据物品 ID 查找匹配的 JSON 配置</li>
 *   <li>解析 JSON 为 {@link TipContent} 列表</li>
 *   <li>将内容添加到原版 tooltip 中（支持 prepend 和普通模式）</li>
 * </ol>
 *
 * <h3>内容插入顺序</h3>
 * <pre>
 * 物品名
 * ↓
 * prepend 内容（在物品名之后，原版内容之前）
 * ↓
 * 原版 tooltip 内容
 * ↓
 * 普通内容（在原版内容之后）
 * </pre>
 *
 * <h3>Shift 显示逻辑</h3>
 * <p>
 * 如果配置了 {@code "shift": true}，则需要按住 Shift 键才显示该内容。
 * 当存在 shift 内容且未按住 Shift 时，会显示提示文本。
 * </p>
 *
 * @author cooobird
 * @see TipContentLoader 内容加载器
 * @see TipContentTooltipComponent 自定义 Tooltip 组件
 * @since 1.2.0
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class TipRenderEventHandler {

    /**
     * Shift 键映射（用于显示/隐藏 shift 内容）
     */
    public static final KeyMapping SHOW_TIP = new KeyMapping(
        "key.datatip.show_tip",
        InputConstants.KEY_LSHIFT,
        "key.categories.datatip"
    );

    /**
     * 内容加载器（在资源重载时设置）
     */
    private static TipContentLoader contentLoader;

    /**
     * 设置内容加载器。
     * 在资源重载时调用。
     *
     * @param loader 内容加载器实例
     */
    public static void setContentLoader(TipContentLoader loader) {
        contentLoader = loader;
    }

    /**
     * 获取内容加载器。
     *
     * @return 内容加载器实例，未初始化返回 null
     */
    @Nullable
    public static TipContentLoader getContentLoader() {
        return contentLoader;
    }

    /**
     * 注册按键绑定。
     *
     * @param event 按键映射注册事件
     */
    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(SHOW_TIP);
    }

    /**
     * 收集 Tooltip 组件事件处理。
     * <p>
     * 将自定义内容添加到原版 tooltip 中。
     * 支持两种插入模式：
     * <ul>
     *   <li><b>prepend</b>：插入到物品名之后，原版内容之前</li>
     *   <li><b>普通</b>：追加到原版内容之后</li>
     * </ul>
     * </p>
     *
     * @param event 收集组件事件
     */
    @SubscribeEvent
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        // 检查是否启用
        if (!DatatipConfig.ENABLED.get()) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        // 获取或解析内容
        List<TipContentLoader.ContentEntry> entries = getOrParseContents(stack);
        if (entries == null || entries.isEmpty()) return;

        // 触发 tooltip 显示事件（tooltip 将显示时调用）
        TipEventManager.fireTooltipShow(stack, true);

        // 检查是否有 shift 内容
        boolean anyShift = entries.stream().anyMatch(TipContentLoader.ContentEntry::shift);
        boolean shiftDown = isShowTipDown();

        // 如果有 shift 内容但未按住 Shift，显示提示
        if (anyShift && !shiftDown) {
            Component hint = Component.translatable("tooltip.datatip.hold_shift", SHOW_TIP.getTranslatedKeyMessage())
                .withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC);
            event.getTooltipElements().add(Either.left(hint));
        }

        // 分离 prepend 和普通内容
        List<TipContent> prependContents = new ArrayList<>();
        List<TipContent> normalContents = new ArrayList<>();

        for (TipContentLoader.ContentEntry entry : entries) {
            // 跳过需要 shift 但未按住 shift 的内容
            if (entry.shift() && !shiftDown) continue;

            if (entry.prepend()) {
                prependContents.add(entry.content());
            } else {
                normalContents.add(entry.content());
            }
        }

        // prepend 内容插入到索引 1（物品名之后，原版内容之前）
        int insertIndex = 1;
        for (TipContent content : prependContents) {
            event.getTooltipElements().add(insertIndex, Either.right(new TipContentTooltipComponent(content, stack)));
            insertIndex++;
        }

        // 普通内容追加到末尾（原版内容之后）
        for (TipContent content : normalContents) {
            event.getTooltipElements().add(Either.right(new TipContentTooltipComponent(content, stack)));
        }
    }

    /**
     * 获取或解析内容。
     * <p>
     * 根据物品 ID 查找匹配的 JSON 配置，支持：
     * <ul>
     *   <li>精确匹配（如 "minecraft:diamond"）</li>
     *   <li>标签匹配（如 "#minecraft:swords"）</li>
     * </ul>
     * </p>
     *
     * @param stack 物品栈
     * @return 内容条目列表，未找到返回 null
     */
    @Nullable
    private static List<TipContentLoader.ContentEntry> getOrParseContents(ItemStack stack) {
        if (contentLoader == null) {
            return null;
        }

        // 从加载器获取内容
        List<TipContentLoader.ContentEntry> entries = new ArrayList<>();

        // 使用 PerformanceOptimizer 获取物品 ID
        String itemIdStr = PerformanceOptimizer.getItemId(stack);
        ResourceLocation itemId = ResourceLocation.tryParse(itemIdStr);

        if (itemId != null) {
            // 精确匹配
            entries.addAll(contentLoader.getEntries(itemId.toString(), stack));

            // 标签匹配
            for (TagKey<Item> tag : stack.getTags().toList()) {
                entries.addAll(contentLoader.getEntriesByTag(tag.location().toString(), stack));
            }
        }

        return entries.isEmpty() ? null : entries;
    }

    /**
     * 检查显示提示按键是否按下。
     *
     * @return true 如果按住了 Shift 键
     */
    private static boolean isShowTipDown() {
        var window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, SHOW_TIP.getKey().getValue());
    }
}
