package com.cooobird.datatip.event;

import com.cooobird.datatip.api.TipContentEntry;
import com.cooobird.datatip.api.TipContentSource;
import com.cooobird.datatip.config.DatatipConfig;
import com.cooobird.datatip.internal.input.ClientKeyState;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tooltip 渲染事件处理器。
 *
 * @author cooobird
 * @since 1.2.0
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class TipRenderEventHandler {

    /**
     * Shift 按键映射，用于显示或隐藏 shift 内容。
     */
    public static final KeyMapping SHOW_TIP = new KeyMapping(
        "key.datatip.show_tip",
        InputConstants.KEY_LSHIFT,
        "key.categories.datatip"
    );

    private static TipContentSource contentSource;

    /**
     * 设置内容加载器。
     *
     * @param source 内容来源实例
     */
    public static void setContentSource(TipContentSource source) {
        contentSource = source;
    }

    /**
     * 获取内容加载器。
     *
     * @return 内容加载器实例，未初始化时返回 null
     */
    @Nullable
    public static TipContentSource getContentSource() {
        return contentSource;
    }

    /**
     * 注册按键绑定。
     *
     * @param event 按键映射注册事件
     */
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(SHOW_TIP);
    }

    /**
     * 收集 Tooltip 组件事件处理。
     *
     * @param event 收集组件事件
     */
    @SubscribeEvent
    public static void onGatherComponents(RenderTooltipEvent.GatherComponents event) {
        if (!DatatipConfig.ENABLED.get()) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<TipContentEntry> entries = TipRenderContentLookup.find(contentSource, stack);
        if (!entries.isEmpty()) {
            TipTooltipElements.insertContentEntries(event, entries, stack, isShowTipDown(), SHOW_TIP);
        }

        TipTooltipElements.appendExtraLines(event, stack);
    }

    public static boolean isShowTipDown() {
        return ClientKeyState.isDown(SHOW_TIP)
            || (SHOW_TIP.isDefault() && Screen.hasShiftDown());
    }
}
