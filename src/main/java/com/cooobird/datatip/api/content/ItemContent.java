package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
) implements TipContent {

    public ItemContent {
        stack = stack != null ? stack : ItemStack.EMPTY;
        size = ContentBounds.dimension(size);
        offsetX = ContentBounds.offset(offsetX);
        offsetY = ContentBounds.offset(offsetY);
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
        return visualHeight();
    }

    @Override
    public int getWidth(int maxWidth) {
        int width = visualWidth();
        if (showLabel) {
            Font font = Minecraft.getInstance().font;
            Component labelText = label != null ? label : stack.getHoverName();
            width += 4 + font.width(labelText);
        }
        return Math.min(width, maxWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || stack.isEmpty()) return;

        int renderBaseX = x + Math.max(0, -offsetX);
        int renderBaseY = y + Math.max(0, -offsetY);
        int renderX = renderBaseX + offsetX;
        int renderY = renderBaseY + offsetY;

        boolean clipped = ContentBounds.beginHorizontalClip(
            context, x, y, maxWidth, visualHeight(), getWidth(Integer.MAX_VALUE));
        try {
            if (size == 16) context.renderItem(stack, renderX, renderY);
            else context.renderItemScaled(stack, renderX, renderY, size);

            if ((showCount || showDurability) && size == 16) {
                context.renderItemDecorations(stack, renderX, renderY);
            }
            if (showLabel) {
                Component labelText = label != null ? label : stack.getHoverName();
                int color = labelColor != null ? labelColor : 0xFFFFFF;
                int labelX = x + visualWidth() + 4;
                int labelY = y + (visualHeight() - 8) / 2;
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

    /**
     * 获取物品栈。
     */
    public ItemStack getStack() {
        return stack;
    }
}
