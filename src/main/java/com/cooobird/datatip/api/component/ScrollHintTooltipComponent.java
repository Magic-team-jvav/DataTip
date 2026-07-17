package com.cooobird.datatip.api.component;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.Objects;

/**
 * 仅在 DataTip 内容超出物理视口时出现的原版 Tooltip 提示行。
 */
public final class ScrollHintTooltipComponent
    implements TooltipComponent, ClientTooltipComponent {
    static final int HEIGHT = 10;

    private final TooltipViewportBudget viewportBudget;
    private final Component message;

    public ScrollHintTooltipComponent(
        TooltipViewportBudget viewportBudget,
        Component scrollKeyMessage
    ) {
        this.viewportBudget = Objects.requireNonNull(
            viewportBudget,
            "viewportBudget"
        );
        this.message = Component.translatable(
            "tooltip.datatip.scroll_hint",
            Objects.requireNonNull(scrollKeyMessage, "scrollKeyMessage")
        ).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    TooltipViewportBudget viewportBudget() {
        return viewportBudget;
    }

    @Override
    public int getHeight() {
        return viewportBudget.scrollHintVisible() ? HEIGHT : 0;
    }

    @Override
    public int getWidth(Font font) {
        return viewportBudget.scrollHintVisible()
            ? font.width(message)
            : 0;
    }

    @Override
    public void renderImage(
        Font font,
        int x,
        int y,
        GuiGraphics graphics
    ) {
        if (!viewportBudget.scrollHintVisible()) return;
        graphics.drawString(font, message, x, y, 0xFFFFFFFF, false);
    }
}
