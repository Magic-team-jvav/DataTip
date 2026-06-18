package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
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
    int size,                    // 渲染尺寸（16 = 原始大小）
    boolean showCount,           // 显示数量
    boolean showDurability,      // 显示耐久条
    boolean showLabel,           // 显示物品名称
    @Nullable Component label,   // 自定义标签（覆盖物品名称）
    @Nullable Integer labelColor // 标签颜色
) implements TipContent {

    /**
     * 创建物品内容（16x16，显示装饰）。
     */
    public static ItemContent of(ItemStack stack) {
        return new ItemContent(stack, 16, true, true, false, null, null);
    }

    /**
     * 创建物品内容（指定尺寸）。
     */
    public static ItemContent of(ItemStack stack, int size) {
        return new ItemContent(stack, size, true, true, false, null, null);
    }

    /**
     * 创建带标签的物品内容。
     */
    public static ItemContent withLabel(ItemStack stack, Component label) {
        return new ItemContent(stack, 16, true, true, true, label, 0xFFFFFF);
    }

    /**
     * 创建带标签的物品内容（指定颜色）。
     */
    public static ItemContent withLabel(ItemStack stack, Component label, int labelColor) {
        return new ItemContent(stack, 16, true, true, true, label, labelColor);
    }

    /**
     * 创建大尺寸物品图标。
     */
    public static ItemContent large(ItemStack stack) {
        return new ItemContent(stack, 32, true, true, false, null, null);
    }

    /**
     * 创建只显示图标的物品内容（无装饰）。
     */
    public static ItemContent iconOnly(ItemStack stack) {
        return new ItemContent(stack, 16, false, false, false, null, null);
    }

    /**
     * 创建只显示图标的物品内容（指定尺寸）。
     */
    public static ItemContent iconOnly(ItemStack stack, int size) {
        return new ItemContent(stack, size, false, false, false, null, null);
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
            // 图标 + 间距 + 标签宽度
            width += 4 + label.getString().length() * 6;
        }
        return width;
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || stack.isEmpty()) return;

        // 渲染物品图标
        if (size == 16) {
            context.renderItem(stack, x, y);
        } else {
            context.renderItemScaled(stack, x, y, size);
        }

        // 渲染装饰（数量、耐久条）
        if (showCount || showDurability) {
            if (size == 16) {
                context.renderItemDecorations(stack, x, y);
            }
            // 注意：非 16x16 尺寸的装饰渲染需要特殊处理
        }

        // 渲染标签
        if (showLabel) {
            Component labelText = label != null ? label : stack.getHoverName();
            int color = labelColor != null ? labelColor : 0xFFFFFF;
            int labelX = x + size + 4;
            int labelY = y + (size - 8) / 2;  // 垂直居中
            context.drawString(labelText, labelX, labelY, color);
        }
    }

    /**
     * 获取物品栈。
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * Builder 模式创建 ItemContent。
     */
    public static class Builder {
        private ItemStack stack = ItemStack.EMPTY;
        private int size = 16;
        private boolean showCount = true;
        private boolean showDurability = true;
        private boolean showLabel = false;
        private Component label;
        private Integer labelColor;

        public Builder stack(ItemStack stack) {
            this.stack = stack;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder showCount(boolean showCount) {
            this.showCount = showCount;
            return this;
        }

        public Builder showDurability(boolean showDurability) {
            this.showDurability = showDurability;
            return this;
        }

        public Builder showLabel(boolean showLabel) {
            this.showLabel = showLabel;
            return this;
        }

        public Builder label(Component label) {
            this.label = label;
            this.showLabel = true;
            return this;
        }

        public Builder labelColor(int labelColor) {
            this.labelColor = labelColor;
            return this;
        }

        public ItemContent build() {
            return new ItemContent(stack, size, showCount, showDurability, showLabel, label, labelColor);
        }
    }
}
