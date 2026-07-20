package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.internal.layout.LabeledVisualBounds;
import com.cooobird.datatip.internal.layout.PreparedLabeledVisualLayout;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 物品图标内容。
 * 渲染物品图标，可选显示数量、耐久条、标签文字。
 *
 * @author cooobird
 * @since 1.2.0
 */
public record ItemContent(
    ItemStack stack,
    int size,                    // 渲染尺寸
    boolean showCount,           // 显示数量
    boolean showDurability,      // 显示耐久条
    boolean showLabel,           // 显示物品名称
    @Nullable Component label,   // 自定义标签
    @Nullable Integer labelColor, // 标签颜色
    int offsetX,                 // X 轴偏移量
    int offsetY                  // Y 轴偏移量
) implements com.cooobird.datatip.api.layout.PreparedContent {

    public ItemContent(
        ItemStack stack,
        int size,
        boolean showCount,
        boolean showDurability,
        boolean showLabel,
        @Nullable Component label,
        @Nullable Integer labelColor,
        int offsetX,
        int offsetY
    ) {
        this.stack = stack != null ? stack : ItemStack.EMPTY;
        this.size = ContentBounds.dimension(size);
        this.showCount = showCount;
        this.showDurability = showDurability;
        this.showLabel = showLabel;
        this.label = label;
        this.labelColor = labelColor;
        this.offsetX = ContentBounds.offset(offsetX);
        this.offsetY = ContentBounds.offset(offsetY);
    }

    // 创建物品内容
    public static ItemContent of(ItemStack stack) {
        return new ItemContent(stack, 16, true, true, false, null, null, 0, 0);
    }

    // 创建物品内容
    public static ItemContent of(ItemStack stack, int size) {
        return new ItemContent(stack, size, true, true, false, null, null, 0, 0);
    }

    // 创建带标签的物品内容
    public static ItemContent withLabel(ItemStack stack, Component label) {
        return new ItemContent(stack, 16, true, true, true, label, 0xFFFFFF, 0, 0);
    }

    // 创建带标签的物品内容
    public static ItemContent withLabel(ItemStack stack, Component label, int labelColor) {
        return new ItemContent(stack, 16, true, true, true, label, labelColor, 0, 0);
    }

    // 创建大尺寸物品图标
    public static ItemContent large(ItemStack stack) {
        return new ItemContent(stack, 32, true, true, false, null, null, 0, 0);
    }

    // 创建只显示图标的物品内容
    public static ItemContent iconOnly(ItemStack stack) {
        return new ItemContent(stack, 16, false, false, false, null, null, 0, 0);
    }

    // 创建只显示图标的物品内容
    public static ItemContent iconOnly(ItemStack stack, int size) {
        return new ItemContent(stack, size, false, false, false, null, null, 0, 0);
    }

    // 创建带偏移的物品内容
    public static ItemContent withOffset(ItemStack stack, int size, int offsetX, int offsetY) {
        return new ItemContent(stack, size, true, true, false, null, null, offsetX, offsetY);
    }

    @Override
    public int getHeight(int maxWidth) {
        return getHeight(new TipLayoutContext(
            Minecraft.getInstance().font,
            stack,
            Math.max(0, maxWidth)
        ));
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        return LabeledVisualBounds.height(
            visualHeight(),
            showLabel ? labelText() : null,
            context.font()
        );
    }

    @Override
    public int getWidth(int maxWidth) {
        return getWidth(new TipLayoutContext(
            Minecraft.getInstance().font,
            stack,
            Math.max(0, maxWidth)
        ));
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        return LabeledVisualBounds.width(
            visualWidth(),
            showLabel ? labelText() : null,
            context.font()
        );
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || stack.isEmpty()) return;

        int renderBaseX = x + ContentBounds.negativeInset(offsetX);
        int renderBaseY = y + ContentBounds.negativeInset(offsetY);
        int renderX = renderBaseX + offsetX;
        int renderY = renderBaseY + offsetY;

        boolean clipped = false;
        try {
            if (size == 16) context.renderItem(stack, renderX, renderY);
            else context.renderItemScaled(stack, renderX, renderY, size);

            renderIndependentDecorations(context, renderX, renderY);
            if (showLabel) {
                Component labelText = labelText();
                int color = labelColor != null ? labelColor : 0xFFFFFF;
                int labelX = x + visualWidth() + 4;
                int rowHeight = LabeledVisualBounds.height(
                    visualHeight(),
                    labelText,
                    context.font()
                );
                int labelY = LabeledVisualBounds.labelY(y, rowHeight, context.font());
                context.drawString(labelText, labelX, labelY, color);
            }
        } finally {
            ContentBounds.endHorizontalClip(context, clipped);
        }
    }

    private int visualWidth() {
        return ContentBounds.extent(size, offsetX);
    }

    private int visualHeight() {
        return ContentBounds.extent(size, offsetY);
    }

    private Component labelText() {
        return label != null ? label : stack.getHoverName();
    }

    private void renderIndependentDecorations(
        TipRenderContext context,
        int x,
        int y
    ) {
        if ((!showCount && !showDurability) || stack.isEmpty()) return;
        float scale = size / 16.0f;
        context.pose().pushPose();
        try {
            context.pose().translate(x, y, 200);
            context.pose().scale(scale, scale, 1.0f);
            if (showDurability && stack.isBarVisible()) {
                int width = stack.getBarWidth();
                int color = stack.getBarColor();
                context.fill(2, 13, 15, 15, 0xFF000000);
                context.fill(2, 13, 2 + width, 14, 0xFF000000 | color);
            }
            if (showCount && stack.getCount() != 1) {
                String count = String.valueOf(stack.getCount());
                int countX = 17 - context.font().width(count);
                context.graphics().drawString(
                    context.font(),
                    count,
                    countX,
                    9,
                    0xFFFFFFFF,
                    true
                );
            }
        } finally {
            context.pose().popPose();
        }
    }

    /**
     * 获取物品栈。
     */
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public com.cooobird.datatip.api.layout.PreparedLayout prepare(
        com.cooobird.datatip.api.layout.TipPrepareContext context
    ) {
        ItemStack frozenStack = stack.copy();
        Component frozenLabel = showLabel ? labelText().copy() : null;
        String frozenCount = showCount && frozenStack.getCount() != 1
            ? String.valueOf(frozenStack.getCount())
            : null;
        int frozenCountX = frozenCount != null
            ? 17 - context.requireLayoutContext().font().width(frozenCount)
            : 0;
        boolean frozenBarVisible = showDurability
            && frozenStack.isBarVisible();
        int frozenBarWidth = frozenBarVisible
            ? frozenStack.getBarWidth()
            : 0;
        int frozenBarColor = frozenBarVisible
            ? frozenStack.getBarColor()
            : 0;
        long rawX = ContentBounds.negativeInsetLong(offsetX) + offsetX;
        long rawY = ContentBounds.negativeInsetLong(offsetY) + offsetY;
        int color = labelColor != null ? labelColor : 0xFFFFFFFF;
        return PreparedLabeledVisualLayout.prepare(
            context,
            visualWidth(),
            visualHeight(),
            frozenLabel,
            color,
            com.cooobird.datatip.api.render.RenderPhase.ISOLATED_MODEL,
            "item",
            (renderContext, x, y, scale, alpha) -> {
                if (frozenStack.isEmpty()) return;
                int drawX = ContentBounds.coordinate(
                    x,
                    scaled(rawX, scale)
                );
                int drawY = ContentBounds.coordinate(
                    y,
                    scaled(rawY, scale)
                );
                int drawSize = Math.max(1, scaled(size, scale));
                float[] shaderColor = RenderSystem.getShaderColor().clone();
                RenderSystem.setShaderColor(
                    shaderColor[0],
                    shaderColor[1],
                    shaderColor[2],
                    shaderColor[3] * alpha
                );
                try {
                    if (drawSize == 16) {
                        renderContext.renderItem(frozenStack, drawX, drawY);
                    } else {
                        renderContext.renderItemScaled(
                            frozenStack,
                            drawX,
                            drawY,
                            drawSize
                        );
                    }
                    renderPreparedDecorations(
                        renderContext,
                        drawX,
                        drawY,
                        drawSize,
                        frozenCount,
                        frozenCountX,
                        frozenBarVisible,
                        frozenBarWidth,
                        frozenBarColor
                    );
                } finally {
                    RenderSystem.setShaderColor(
                        shaderColor[0],
                        shaderColor[1],
                        shaderColor[2],
                        shaderColor[3]
                    );
                }
            }
        );
    }

    private static void renderPreparedDecorations(
        TipRenderContext context,
        int x,
        int y,
        int drawSize,
        @Nullable String count,
        int countX,
        boolean barVisible,
        int barWidth,
        int barColor
    ) {
        if (count == null && !barVisible) return;
        float scale = drawSize / 16.0f;
        context.pose().pushPose();
        try {
            context.pose().translate(x, y, 200);
            context.pose().scale(scale, scale, 1.0f);
            if (barVisible) {
                context.fill(2, 13, 15, 15, 0xFF000000);
                context.fill(
                    2,
                    13,
                    2 + barWidth,
                    14,
                    0xFF000000 | barColor
                );
            }
            if (count != null) {
                context.graphics().drawString(
                    context.font(),
                    count,
                    countX,
                    9,
                    0xFFFFFFFF,
                    true
                );
            }
        } finally {
            context.pose().popPose();
        }
    }

    private static int scaled(long value, double scale) {
        if (value <= 0) return 0;
        return (int) Math.min(
            Integer.MAX_VALUE,
            Math.round(value * scale)
        );
    }
}
