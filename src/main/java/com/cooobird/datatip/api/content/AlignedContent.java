package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;

/**
 * 对齐包装器。
 * <p>
 * 为任何 {@link TipContent} 添加对齐支持。
 * 当 JSON 中指定 "align" 属性时，解析器会自动将内容包装为此类。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 任何内容都可以指定对齐
 * {"type": "item", "item": "minecraft:diamond", "size": 32, "align": "center"}
 * {"type": "progress", "progress": 0.75, "width": 100, "align": "right"}
 * {"type": "block", "block": "minecraft:stone", "size": 48, "align": "center"}
 * }</pre>
 *
 * @author cooobird
 * @see VBoxContent.HorizontalAlign 对齐方式枚举
 * @since 1.2.0
 */
public record AlignedContent(
    TipContent inner,                    // 被包装的内容
    VBoxContent.HorizontalAlign align    // 水平对齐方式
) implements TipContent {

    @Override
    public int getHeight(int maxWidth) {
        return inner.getHeight(maxWidth);
    }

    @Override
    public int getWidth(int maxWidth) {
        return inner.getWidth(maxWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        // 获取内容实际宽度
        int contentWidth = inner.getWidth(maxWidth);

        // 根据对齐方式计算 X 坐标
        int alignedX = switch (align) {
            case LEFT -> x;
            case CENTER -> x + (maxWidth - contentWidth) / 2;
            case RIGHT -> x + maxWidth - contentWidth;
        };

        // 渲染内容
        inner.render(context, alignedX, y, maxWidth, alpha);
    }

    @Override
    public boolean isAnimated() {
        return inner.isAnimated();
    }

    @Override
    public void tick(int tickCount) {
        inner.tick(tickCount);
    }

    @Override
    public void onShow() {
        inner.onShow();
    }

    @Override
    public void onHide() {
        inner.onHide();
    }
}
