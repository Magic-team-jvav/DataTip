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

    private static final int HINT_LINE_HEIGHT = 12;

    @Override
    public int getHeight(int maxWidth) {
        int availableWidth = maxWidth - padding * 2;
        boolean hasCollapsed = false;
        int maxHeight = 0;

        for (TipContent child : children) {
            if (child.isShiftCollapsed()) {
                hasCollapsed = true;
            } else {
                int childHeight = child.getHeight(availableWidth);
                if (childHeight > maxHeight) {
                    maxHeight = childHeight;
                }
            }
        }

        if (hasCollapsed && maxHeight == 0) {
            return padding * 2 + HINT_LINE_HEIGHT;
        }
        if (hasCollapsed && HINT_LINE_HEIGHT > maxHeight) {
            maxHeight = HINT_LINE_HEIGHT;
        }

        return maxHeight + padding * 2;
    }

    @Override
    public int getWidth(int maxWidth) {
        int availableWidth = maxWidth - padding * 2;
        boolean hasCollapsed = false;
        int totalWidth = padding * 2;
        boolean hasPrevNonCollapsed = false;

        for (int i = 0; i < children.size(); i++) {
            TipContent child = children.get(i);
            if (child.isShiftCollapsed()) {
                hasCollapsed = true;
            } else {
                if (hasPrevNonCollapsed) {
                    totalWidth += gap;
                }
                totalWidth += child.getWidth(availableWidth);
                hasPrevNonCollapsed = true;
            }
        }

        if (hasCollapsed) {
            Font font = Minecraft.getInstance().font;
            Component hint = Component.translatable("tooltip.datatip.hold_shift",
                TipRenderEventHandler.SHOW_TIP.getTranslatedKeyMessage());
            if (hasPrevNonCollapsed) {
                totalWidth += gap;
            }
            totalWidth += font.width(hint);
        }

        return Math.min(totalWidth, maxWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || children.isEmpty()) return;

        int currentX = x + padding;
        int availableWidth = maxWidth - padding * 2;
        int containerHeight = getHeight(maxWidth) - padding * 2;
        boolean hasCollapsed = false;
        boolean hasPrevNonCollapsed = false;

        for (TipContent child : children) {
            if (child.isShiftCollapsed()) {
                hasCollapsed = true;
            } else {
                if (hasPrevNonCollapsed) {
                    currentX += gap;
                }

                int childHeight = child.getHeight(availableWidth);
                int childY = switch (verticalAlign) {
                    case TOP -> y + padding;
                    case CENTER -> y + padding + (containerHeight - childHeight) / 2;
                    case BOTTOM -> y + padding + containerHeight - childHeight;
                };

                child.render(context, currentX, childY, availableWidth, alpha);
                currentX += child.getWidth(availableWidth);
                hasPrevNonCollapsed = true;
            }
        }

        if (hasCollapsed) {
            int hintY = y + padding;
            if (verticalAlign == VerticalAlign.CENTER) {
                hintY += (containerHeight - HINT_LINE_HEIGHT) / 2;
            } else if (verticalAlign == VerticalAlign.BOTTOM) {
                hintY += containerHeight - HINT_LINE_HEIGHT;
            }
            if (hasPrevNonCollapsed) {
                currentX += gap;
            }
            BaseTextContent.renderShiftHint(context, currentX, hintY);
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
