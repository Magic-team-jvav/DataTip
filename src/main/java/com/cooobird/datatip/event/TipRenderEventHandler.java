package com.cooobird.datatip.event;

import com.cooobird.datatip.api.TipContent;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Tooltip 渲染事件处理器。
 * 使用 RenderTooltipEvent.GatherComponents 添加自定义渲染内容。
 *
 * @author cooobird
 * @since 1.0
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TipRenderEventHandler {

    public static final KeyMapping SHOW_TIP = new KeyMapping(
        "key.datatip.show_tip",
        InputConstants.KEY_LSHIFT,
        "key.categories.datatip"
    );

    private static TipContentLoader contentLoader;

    public static void setContentLoader(TipContentLoader loader) {
        contentLoader = loader;
    }

    @Nullable
    public static TipContentLoader getContentLoader() {
        return contentLoader;
    }

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(SHOW_TIP);
    }

    /**
     * 收集 Tooltip 组件事件处理。
     * 使用 RenderTooltipEvent.GatherComponents 添加自定义渲染内容。
     */
    @SubscribeEvent
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        if (!DatatipConfig.ENABLED.get()) return;
        if (contentLoader == null) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<TipContentLoader.ContentEntry> entries = getOrParseContents(stack);
        if (entries == null || entries.isEmpty()) return;

        boolean anyShift = entries.stream().anyMatch(TipContentLoader.ContentEntry::shift);
        boolean shiftDown = isShowTipDown();

        // 如果有 shift 内容但未按住 Shift，显示提示
        if (anyShift && !shiftDown) {
            Component hint = Component.translatable("tooltip.datatip.hold_shift", SHOW_TIP.getTranslatedKeyMessage())
                .withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC);
            event.getTooltipElements().add(Either.left(hint));
            return;
        }

        // 分离 prepend 和普通内容
        List<TipContent> prependContents = new ArrayList<>();
        List<TipContent> normalContents = new ArrayList<>();

        for (TipContentLoader.ContentEntry entry : entries) {
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

    @Nullable
    private static List<TipContentLoader.ContentEntry> getOrParseContents(ItemStack stack) {
        if (contentLoader == null) {
            return null;
        }

        List<TipContentLoader.ContentEntry> entries = new ArrayList<>();

        String itemIdStr = PerformanceOptimizer.getItemId(stack);
        ResourceLocation itemId = ResourceLocation.tryParse(itemIdStr);

        if (itemId != null) {
            entries.addAll(contentLoader.getEntries(itemId.toString(), stack));

            for (TagKey<Item> tag : stack.getTags().toList()) {
                entries.addAll(contentLoader.getEntriesByTag(tag.location().toString(), stack));
            }
        }

        return entries.isEmpty() ? null : entries;
    }

    private static boolean isShowTipDown() {
        var window = Minecraft.getInstance().getWindow().getWindow();
        return InputConstants.isKeyDown(window, SHOW_TIP.getKey().getValue());
    }
}
