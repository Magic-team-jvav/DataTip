package com.cooobird.datatip.client;

import com.cooobird.datatip.internal.input.ClientKeyState;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

/**
 * DataTip 客户端键位及其统一实时状态。
 */
public final class DatatipKeyMappings {
    /**
     * Shift 按键映射，用于显示或隐藏 shift 内容。
     */
    public static final KeyMapping SHOW_TIP = new KeyMapping(
        "key.datatip.show_tip",
        InputConstants.KEY_LSHIFT,
        "key.categories.datatip"
    );

    /**
     * Tooltip 视口滚动键位，默认使用左 Ctrl。
     */
    public static final KeyMapping SCROLL_TOOLTIP = new KeyMapping(
        "key.datatip.scroll_tooltip",
        InputConstants.KEY_LCONTROL,
        "key.categories.datatip"
    );

    private DatatipKeyMappings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(SHOW_TIP);
        event.register(SCROLL_TOOLTIP);
    }

    public static boolean isShowTipDown() {
        return ClientKeyState.isDown(SHOW_TIP)
            || (SHOW_TIP.isDefault() && Screen.hasShiftDown());
    }

    public static boolean isScrollTooltipDown() {
        return ClientKeyState.isDown(SCROLL_TOOLTIP)
            || (SCROLL_TOOLTIP.isDefault() && Screen.hasControlDown());
    }
}

