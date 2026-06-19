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
    int offsetY                  // Y 轴偏移量
) implements TipContent {

    // 创建物品内容
    public static ItemContent of(ItemStack stack) {
        return new ItemContent(stack, 16, true, true, false, null, null, 0);
    }

    // 创建物品内容
    public static ItemContent of(ItemStack stack, int size) {
        return new ItemContent(stack, size, true, true, false, null, null, 0);
    }

    // 创建带标签的物品内容
    public static ItemContent withLabel(ItemStack stack, Component label) {
        return new ItemContent(stack, 16, true, true, true, label, 0xFFFFFF, 0);
    }

    // 创建带标签的物品内容
    public static ItemContent withLabel(ItemStack stack, Component label, int labelColor) {
        return new ItemContent(stack, 16, true, true, true, label, labelColor, 0);
    }

    // 创建大尺寸物品图标
    public static ItemContent large(ItemStack stack) {
        return new ItemContent(stack, 32, true, true, false, null, null, 0);
    }

    // 创建只显示图标的物品内容
    public static ItemContent iconOnly(ItemStack stack) {
        return new ItemContent(stack, 16, false, false, false, null, null, 0);
    }

    // 创建只显示图标的物品内容
    public static ItemContent iconOnly(ItemStack stack, int size) {
        return new ItemContent(stack, size, false, false, false, null, null, 0);
    }

    // 创建带偏移的物品内容
    public static ItemContent withOffset(ItemStack stack, int size, int offsetY) {
        return new ItemContent(stack, size, true, true, false, null, null, offsetY);
    }

    @Override
    public int getHeight(int maxWidth) {
        int height = size;
        if (showLabel) {
            height += 12;  // 标签高度
        }
        return height;
    }

    @Override
    public int getWidth(int maxWidth) {
        int width = size;
        if (showLabel && label != null) {
            Font font = Minecraft.getInstance().font;
            width += 4 + font.width(label.getString());
        }
        return Math.min(width, maxWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || stack.isEmpty()) return;

        int renderY = y + offsetY;

        // 渲染物品图标
        if (size == 16) {
            context.renderItem(stack, x, renderY);
        } else {
            context.renderItemScaled(stack, x, renderY, size);
        }

        // 渲染装饰
        if (showCount || showDurability) {
            if (size == 16) {
                context.renderItemDecorations(stack, x, renderY);
            }
            // 注意：非 16x16 尺寸的装饰渲染需要特殊处理
        }

        // 渲染标签
        if (showLabel) {
            Component labelText = label != null ? label : stack.getHoverName();
            int color = labelColor != null ? labelColor : 0xFFFFFF;
            int labelX = x + size + 4;
            int labelY = renderY + (size - 8) / 2;  // 垂直居中
            context.drawString(labelText, labelX, labelY, color);
        }
    }

    /**
     * 获取物品栈。
     */
    public ItemStack getStack() {
        return stack;
    }
}
