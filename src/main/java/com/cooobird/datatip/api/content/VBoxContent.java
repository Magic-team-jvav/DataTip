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
 * 垂直布局容器，子元素从上到下垂直排列。
 */
public record VBoxContent(List<TipContent> children, int gap, int padding,
                          HorizontalAlign horizontalAlign) implements ContainerContent {

    public VBoxContent(List<TipContent> children, int gap, int padding, HorizontalAlign horizontalAlign) {
        this.children = new ArrayList<>(children);
        this.gap = gap;
        this.padding = padding;
        this.horizontalAlign = horizontalAlign;
    }

    // 创建默认垂直布局
    public static VBoxContent create() {
        return new VBoxContent(List.of(), 0, 0, HorizontalAlign.LEFT);
    }

    // 创建带间距的垂直布局
    public static VBoxContent withGap(int gap) {
        return new VBoxContent(List.of(), gap, 0, HorizontalAlign.LEFT);
    }

    // 创建带内边距的垂直布局
    public static VBoxContent withPadding(int padding) {
        return new VBoxContent(List.of(), 0, padding, HorizontalAlign.LEFT);
    }

    // 创建居中对齐的垂直布局
    public static VBoxContent centered() {
        return new VBoxContent(List.of(), 0, 0, HorizontalAlign.CENTER);
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
        int[] totalHeight = {padding * 2};
        int aw = maxWidth - padding * 2;
        var iter = forEachVisibleContent((child, needsGap) -> {
            if (needsGap) totalHeight[0] += gap;
            totalHeight[0] += child.getHeight(aw);
        });
        if (iter.hasCollapsed()) {
            if (iter.hasNonCollapsed()) totalHeight[0] += gap;
            totalHeight[0] += HINT_LINE_HEIGHT;
        }
        return totalHeight[0];
    }

    @Override
    public int getWidth(int maxWidth) {
        int aw = maxWidth - padding * 2;
        int[] maxW = {0};
        var iter = forEachVisibleContent((child, needsGap) -> {
            int cw = child.getWidth(aw);
            if (cw > maxW[0]) maxW[0] = cw;
        });
        if (iter.hasCollapsed()) {
            Font font = Minecraft.getInstance().font;
            Component hint = Component.translatable("tooltip.datatip.hold_shift",
                TipRenderEventHandler.SHOW_TIP.getTranslatedKeyMessage());
            int hintWidth = font.width(hint);
            if (hintWidth > maxW[0]) maxW[0] = hintWidth;
        }
        return maxW[0] + padding * 2;
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || children.isEmpty()) return;

        int[] cy = {y + padding};
        int aw = maxWidth - padding * 2;
        var iter = forEachVisibleContent((child, needsGap) -> {
            if (needsGap) cy[0] += gap;
            int cw = child.getWidth(aw);
            int cx = switch (horizontalAlign) {
                case LEFT -> x + padding;
                case CENTER -> x + padding + (aw - cw) / 2;
                case RIGHT -> x + padding + aw - cw;
            };
            child.render(context, cx, cy[0], aw, alpha);
            cy[0] += child.getHeight(aw);
        });
        if (iter.hasCollapsed()) {
            if (iter.hasNonCollapsed()) cy[0] += gap;
            BaseTextContent.renderShiftHint(context, x + padding, cy[0]);
        }
    }

    // 水平对齐方式
    public enum HorizontalAlign {
        LEFT, CENTER, RIGHT
    }
}
