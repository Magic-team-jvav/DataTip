package com.cooobird.datatip.api.component;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.session.TooltipHit;
import com.cooobird.datatip.config.DatatipConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 仅在 DataTip 内容超出物理视口时出现的原版 Tooltip 提示行。
 */
public final class ScrollHintTooltipComponent
    implements TooltipComponent, ClientTooltipComponent {
    static final int HEIGHT = 10;

    private final TooltipViewportBudget viewportBudget;
    private final @Nullable Component message;
    private final @Nullable TipContentTooltipComponent customContent;

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
        ).withStyle(ChatFormatting.ITALIC);
        this.customContent = null;
    }

    public ScrollHintTooltipComponent(
        TooltipViewportBudget viewportBudget,
        TipContent content,
        TooltipHit hit
    ) {
        this.viewportBudget = Objects.requireNonNull(
            viewportBudget,
            "viewportBudget"
        );
        this.message = null;
        this.customContent = new TipContentTooltipComponent(
            Objects.requireNonNull(content, "content"),
            Objects.requireNonNull(hit, "hit").stackSnapshot(),
            hit
        );
    }

    TooltipViewportBudget viewportBudget() {
        return viewportBudget;
    }

    @Override
    public int getHeight() {
        if (!viewportBudget.scrollHintVisible()) return 0;
        return customContent != null ? customContent.getHeight() : HEIGHT;
    }

    @Override
    public int getWidth(Font font) {
        return viewportBudget.scrollHintVisible()
            ? intrinsicWidth(font)
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
        if (customContent != null) {
            customContent.renderImage(font, x, y, graphics);
        } else {
            graphics.drawString(
                font,
                message(),
                x,
                y,
                DatatipConfig.scrollHintColor(),
                false
            );
        }
    }

    int intrinsicHeight(Font font) {
        return customContent != null
            ? customContent.intrinsicHeight(font)
            : HEIGHT;
    }

    int intrinsicWidth(Font font) {
        return customContent != null
            ? customContent.intrinsicWidth(font)
            : font.width(message());
    }

    private Component message() {
        return Objects.requireNonNull(message, "message");
    }
}
