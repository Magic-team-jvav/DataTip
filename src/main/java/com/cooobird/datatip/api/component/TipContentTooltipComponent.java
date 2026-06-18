package com.cooobird.datatip.api.component;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.config.DatatipConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
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

    public TipContentTooltipComponent(TipContent content) {
        this(content, null);
    }

    @Override
    public int getHeight() {
        int maxWidth = DatatipConfig.MAX_WIDTH.get();
        return content.getHeight(maxWidth);
    }

    @Override
    public int getWidth(@NotNull Font font) {
        int maxWidth = DatatipConfig.MAX_WIDTH.get();
        return content.getWidth(maxWidth);
    }

    @Override
    public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix, @NotNull MultiBufferSource.BufferSource bufferSource) {
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics graphics) {
        // 获取物品栈
        ItemStack stack = itemStack;
        if (stack == null || stack.isEmpty()) {
            var mc = Minecraft.getInstance();
            if (mc.screen instanceof AbstractContainerScreen containerScreen) {
                stack = containerScreen.getMenu().getCarried();
            }
            if (stack == null || stack.isEmpty()) {
                stack = ItemStack.EMPTY;
            }
        }

        // 创建渲染上下文
        TipRenderContext context = new TipRenderContext(graphics, font,
            Minecraft.getInstance().gui.getGuiTicks(),
            Minecraft.getInstance().getDeltaFrameTime(),
            stack);

        // 更新动画
        if (content.isAnimated()) {
            content.tick(Minecraft.getInstance().gui.getGuiTicks());
        }

        // 渲染内容
        int maxWidth = DatatipConfig.MAX_WIDTH.get();
        content.render(context, x, y, maxWidth, 1.0f);
    }
}
