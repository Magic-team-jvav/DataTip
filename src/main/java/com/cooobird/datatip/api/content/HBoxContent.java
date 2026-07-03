package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.event.TipRenderEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 水平布局容器。
 * 子元素从左到右水平排列。
 *
 * @author cooobird
 * @since 1.2.0
 */
public record HBoxContent(List<TipContent> children, int gap, int padding,
                          VerticalAlign verticalAlign) implements ContainerContent {

    // 创建水平布局容器
    public HBoxContent(List<TipContent> children, int gap, int padding, VerticalAlign verticalAlign) {
        this.children = new ArrayList<>(children);
        this.gap = gap;
        this.padding = padding;
        this.verticalAlign = verticalAlign;
    }

    // 创建默认水平布局
    public static HBoxContent create() {
        return new HBoxContent(List.of(), 0, 0, VerticalAlign.TOP);
    }

    // 创建带间距的水平布局
    public static HBoxContent withGap(int gap) {
        return new HBoxContent(List.of(), gap, 0, VerticalAlign.TOP);
    }

    // 创建带内边距的水平布局
    public static HBoxContent withPadding(int padding) {
        return new HBoxContent(List.of(), 0, padding, VerticalAlign.TOP);
    }

    // 创建垂直居中对齐的水平布局
    public static HBoxContent centered() {
        return new HBoxContent(List.of(), 0, 0, VerticalAlign.CENTER);
    }

    @Override
    public List<TipContent> children() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void addChild(TipContent child) {
        children.add(child);
    }

    @Override
    public int getHeight(int maxWidth) {
        int aw = maxWidth - padding * 2;
        int[] maxH = {0};
        var iter = forEachVisibleContent((child, needsGap) -> {
            int ch = child.getHeight(aw);
            if (ch > maxH[0]) maxH[0] = ch;
        });
        if (iter.hasCollapsed() && maxH[0] == 0) {
            return padding * 2 + HINT_LINE_HEIGHT;
        }
        if (iter.hasCollapsed() && HINT_LINE_HEIGHT > maxH[0]) {
            maxH[0] = HINT_LINE_HEIGHT;
        }
        return maxH[0] + padding * 2;
    }

    @Override
    public int getWidth(int maxWidth) {
        int aw = maxWidth - padding * 2;
        int[] totalW = {padding * 2};
        var iter = forEachVisibleContent((child, needsGap) -> {
            if (needsGap) totalW[0] += gap;
            totalW[0] += child.getWidth(aw);
        });
        if (iter.hasCollapsed()) {
            Font font = Minecraft.getInstance().font;
            Component hint = Component.translatable("tooltip.datatip.hold_shift",
                TipRenderEventHandler.SHOW_TIP.getTranslatedKeyMessage());
            if (iter.hasNonCollapsed()) totalW[0] += gap;
            totalW[0] += font.width(hint);
        }
        return Math.min(totalW[0], maxWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || children.isEmpty()) return;

        int[] cx = {x + padding};
        int aw = maxWidth - padding * 2;
        int ch = getHeight(maxWidth) - padding * 2;
        var iter = forEachVisibleContent((child, needsGap) -> {
            if (needsGap) cx[0] += gap;
            int childH = child.getHeight(aw);
            int cy = switch (verticalAlign) {
                case TOP -> y + padding;
                case CENTER -> y + padding + (ch - childH) / 2;
                case BOTTOM -> y + padding + ch - childH;
            };
            child.render(context, cx[0], cy, aw, alpha);
            cx[0] += child.getWidth(aw);
        });
        if (iter.hasCollapsed()) {
            int hintY = y + padding;
            if (verticalAlign == VerticalAlign.CENTER) hintY += (ch - HINT_LINE_HEIGHT) / 2;
            else if (verticalAlign == VerticalAlign.BOTTOM) hintY += ch - HINT_LINE_HEIGHT;
            if (iter.hasNonCollapsed()) cx[0] += gap;
            BaseTextContent.renderShiftHint(context, cx[0], hintY);
        }
    }

    // 垂直对齐方式
    public enum VerticalAlign {
        TOP, CENTER, BOTTOM
    }
}
