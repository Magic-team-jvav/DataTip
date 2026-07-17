package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.PreparedContent;
import com.cooobird.datatip.api.layout.PreparedLayout;
import com.cooobird.datatip.api.layout.TipPrepareContext;

/**
 * 对齐包装器。
 * <p>
 * 为旧的程序化 API 保留任意内容的对齐包装能力。
 * 现代 JSON 使用通用 {@code selfAlign} 修饰符。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 任何内容都可以指定对齐
 * {"type": "item", "item": "minecraft:diamond", "size": 32, "selfAlign": "center"}
 * {"type": "progress", "progress": 0.75, "width": 100, "selfAlign": "right"}
 * {"type": "block", "block": "minecraft:stone", "size": 48, "selfAlign": "center"}
 * }</pre>
 *
 * @author cooobird
 * @see VBoxContent.HorizontalAlign 对齐方式枚举
 * @since 1.2.0
 */
public record AlignedContent(
    TipContent inner,                    // 被包装的内容
    VBoxContent.HorizontalAlign align    // 水平对齐方式
) implements TipContent, PreparedContent {

    @Override
    public int getHeight(int maxWidth) {
        return inner.getHeight(maxWidth);
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        return inner.getHeight(context);
    }

    @Override
    public int getWidth(int maxWidth) {
        return inner.getWidth(maxWidth);
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        return inner.getWidth(context);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        // 获取内容实际宽度
        TipLayoutContext layout = TipLayoutContext.bounded(context.font(), context.itemStack(), Math.max(1, maxWidth));
        int contentWidth = inner.getWidth(layout);

        // 根据对齐方式计算 X 坐标
        int alignedX = switch (align) {
            case LEFT -> x;
            case CENTER -> x + (maxWidth - contentWidth) / 2;
            case RIGHT -> x + maxWidth - contentWidth;
        };

        // 渲染内容
        inner.render(context, alignedX, y, Math.max(1, contentWidth), alpha);
    }

    @Override
    public PreparedLayout prepare(TipPrepareContext context) {
        return inner.prepare(context);
    }

    @Override
    public boolean hasContent() {
        return inner.hasContent();
    }

    @Override
    public boolean isShiftCollapsed() {
        return inner.isShiftCollapsed();
    }

    @Override
    public long offsetZ() {
        return inner.offsetZ();
    }

    @Override
    public boolean isAnimated() {
        return inner.isAnimated();
    }

    @Override
    public void tick(int tickCount) {
        inner.tick(tickCount);
    }

}
