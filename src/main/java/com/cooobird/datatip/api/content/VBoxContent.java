package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 垂直布局容器，子元素从上到下垂直排列。
 */
public record VBoxContent(List<TipContent> children, int gap, int padding,
                          HorizontalAlign horizontalAlign) implements ContainerContent {

    // 创建垂直布局容器
    public VBoxContent(List<TipContent> children, int gap, int padding, HorizontalAlign horizontalAlign) {
        this.children = new ArrayList<>(children);
        this.gap = gap;
        this.padding = padding;
        this.horizontalAlign = horizontalAlign;
    }

    @Override
    public int gap() {
        return gap;
    }

    @Override
    public int padding() {
        return padding;
    }

    @Override
    public HorizontalAlign horizontalAlign() {
        return horizontalAlign;
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
        int totalHeight = padding * 2;
        int availableWidth = maxWidth - padding * 2;

        for (int i = 0; i < children.size(); i++) {
            TipContent child = children.get(i);
            totalHeight += child.getHeight(availableWidth);
            if (i < children.size() - 1) {
                totalHeight += gap;
            }
        }

        return totalHeight;
    }

    @Override
    public int getWidth(int maxWidth) {
        int maxChildWidth = 0;
        int availableWidth = maxWidth - padding * 2;

        for (TipContent child : children) {
            int childWidth = child.getWidth(availableWidth);
            if (childWidth > maxChildWidth) {
                maxChildWidth = childWidth;
            }
        }

        return maxChildWidth + padding * 2;
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || children.isEmpty()) return;

        int currentY = y + padding;
        int availableWidth = maxWidth - padding * 2;

        for (TipContent child : children) {
            int childWidth = child.getWidth(availableWidth);
            int childX = switch (horizontalAlign) {
                case LEFT -> x + padding;
                case CENTER -> x + padding + (availableWidth - childWidth) / 2;
                case RIGHT -> x + padding + availableWidth - childWidth;
            };

            child.render(context, childX, currentY, availableWidth, alpha);
            currentY += child.getHeight(availableWidth) + gap;
        }
    }

    // 渲染静态内容
    public void renderStatic(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || children.isEmpty()) return;

        int currentY = y + padding;
        int availableWidth = maxWidth - padding * 2;

        for (TipContent child : children) {
            // 跳过动画内容
            if (child.isAnimated()) continue;

            int childWidth = child.getWidth(availableWidth);
            int childX = switch (horizontalAlign) {
                case LEFT -> x + padding;
                case CENTER -> x + padding + (availableWidth - childWidth) / 2;
                case RIGHT -> x + padding + availableWidth - childWidth;
            };

            child.render(context, childX, currentY, availableWidth, alpha);
            currentY += child.getHeight(availableWidth) + gap;
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

    // 水平对齐方式
    public enum HorizontalAlign {
        LEFT, CENTER, RIGHT
    }
}
