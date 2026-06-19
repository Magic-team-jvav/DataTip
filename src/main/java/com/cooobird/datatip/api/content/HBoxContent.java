package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;

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
public class HBoxContent implements ContainerContent {

    private final List<TipContent> children;
    private final int gap;
    private final int padding;
    private final VerticalAlign verticalAlign;

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
        int maxHeight = 0;
        int availableWidth = maxWidth - padding * 2;

        for (TipContent child : children) {
            int childHeight = child.getHeight(availableWidth);
            if (childHeight > maxHeight) {
                maxHeight = childHeight;
            }
        }

        return maxHeight + padding * 2;
    }

    @Override
    public int getWidth(int maxWidth) {
        int totalWidth = padding * 2;
        int availableWidth = maxWidth - padding * 2;

        for (int i = 0; i < children.size(); i++) {
            TipContent child = children.get(i);
            totalWidth += child.getWidth(availableWidth);
            if (i < children.size() - 1) {
                totalWidth += gap;
            }
        }

        return Math.min(totalWidth, maxWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || children.isEmpty()) return;

        int currentX = x + padding;
        int availableWidth = maxWidth - padding * 2;
        int containerHeight = getHeight(maxWidth) - padding * 2;

        for (TipContent child : children) {
            int childHeight = child.getHeight(availableWidth);
            int childY = switch (verticalAlign) {
                case TOP -> y + padding;
                case CENTER -> y + padding + (containerHeight - childHeight) / 2;
                case BOTTOM -> y + padding + containerHeight - childHeight;
            };

            child.render(context, currentX, childY, availableWidth, alpha);
            currentX += child.getWidth(availableWidth) + gap;
        }
    }

    @Override
    public boolean isAnimated() {
        return children.stream().anyMatch(TipContent::isAnimated);
    }

    @Override
    public void tick(int tickCount) {
        children.forEach(child -> child.tick(tickCount));
    }

    @Override
    public void onShow() {
        children.forEach(TipContent::onShow);
    }

    @Override
    public void onHide() {
        children.forEach(TipContent::onHide);
    }

    // 垂直对齐方式
    public enum VerticalAlign {
        TOP, CENTER, BOTTOM
    }
}
