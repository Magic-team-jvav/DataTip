package com.cooobird.datatip.api.component;

import com.cooobird.datatip.config.DatatipConfig;
import net.minecraft.client.Minecraft;

/**
 * Tooltip 布局尺寸工具。
 */
final class TipTooltipLayout {
    private static final int VANILLA_MIN_WRAP_WIDTH = 200;

    private TipTooltipLayout() {
    }

    static int availableWidth() {
        int overrideWidth = DatatipConfig.MAX_WIDTH.get();
        if (overrideWidth > 0) {
            return overrideWidth;
        }

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        return Math.max(screenWidth / 2, VANILLA_MIN_WRAP_WIDTH);
    }
}
