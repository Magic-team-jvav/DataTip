package com.cooobird.datatip.api.component;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.config.DatatipConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * 自定义 Tooltip 组件。
 * 将 TipContent 包装为 Minecraft 的 TooltipComponent。
 *
 * @author cooobird
 * @since 1.2.0
 */
public record TipContentTooltipComponent(TipContent content, @Nullable ItemStack itemStack)
    implements TooltipComponent, ClientTooltipComponent {

    public TipContentTooltipComponent {
        java.util.Objects.requireNonNull(content, "content");
    }

    @Override
    public int getHeight() {
        return content.getHeight(layoutContext(Minecraft.getInstance().font, normalizedStack()));
    }

    @Override
    public int getWidth(Font font) {
        return content.getWidth(layoutContext(font, normalizedStack()));
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        ItemStack renderStack = normalizedStack();

        TipRenderContext context = new TipRenderContext(graphics, font,
            Minecraft.getInstance().gui.getGuiTicks(),
            Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true),
            renderStack);

        if (content.isAnimated()) {
            content.tick(Minecraft.getInstance().gui.getGuiTicks());
        }

        TipLayoutContext layout = layoutContext(font, renderStack);
        int contentWidth = Math.max(1, content.getWidth(layout));
        content.render(context, x, y, contentWidth, 1.0f);
    }

    private ItemStack normalizedStack() {
        return itemStack != null ? itemStack : ItemStack.EMPTY;
    }

    private static TipLayoutContext layoutContext(Font font, ItemStack stack) {
        int configuredWidth = DatatipConfig.MAX_WIDTH.get();
        return configuredWidth > 0
            ? TipLayoutContext.bounded(font, stack, configuredWidth)
            : TipLayoutContext.unbounded(font, stack);
    }
}
