package com.cooobird.datatip.api.component;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipEventManager;
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

    public TipContentTooltipComponent(TipContent content) {
        this(content, null);
    }

    @Override
    public int getHeight() {
        return content.getHeight(DatatipConfig.MAX_WIDTH.get());
    }

    @Override
    public int getWidth(Font font) {
        return content.getWidth(DatatipConfig.MAX_WIDTH.get());
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        // 获取物品栈
        ItemStack stack = itemStack != null ? itemStack : ItemStack.EMPTY;

        // 触发渲染前事件
        TipEventManager.PreRenderEvent preEvent = TipEventManager.firePreRender(stack);
        if (preEvent.isCanceled()) return;

        // 创建渲染上下文
        TipRenderContext context = new TipRenderContext(graphics, font,
            Minecraft.getInstance().gui.getGuiTicks(),
            Minecraft.getInstance().getDeltaFrameTime(),
            preEvent.getItemStack());

        // 更新动画
        if (content.isAnimated()) {
            content.tick(Minecraft.getInstance().gui.getGuiTicks());
        }

        // 渲染内容
        int maxWidth = DatatipConfig.MAX_WIDTH.get();
        content.render(context, x, y, maxWidth, 1.0f);

        // 触发渲染后事件
        TipEventManager.firePostRender(preEvent.getItemStack());
    }
}
