package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.client.DatatipKeyMappings;
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
        this.children = new ArrayList<>(List.copyOf(children != null ? children : List.of()));
        this.gap = ContentBounds.spacing(gap);
        this.padding = ContentBounds.spacing(padding);
        this.verticalAlign = verticalAlign != null ? verticalAlign : VerticalAlign.TOP;
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
        children.add(java.util.Objects.requireNonNull(child, "child"));
    }

    @Override
    public int getHeight(int maxWidth) {
        return getHeight(legacyContext(maxWidth));
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        TipLayoutContext childContext = childContext(context);
        int availableWidth = context.hasWidthLimit()
            ? Math.max(0, context.maxWidth() - padding * 2
            - reservedHintWidth(context.font(), Math.max(0, context.maxWidth() - padding * 2)))
            : Integer.MAX_VALUE;
        int[] usedWidth = {0};
        int[] maxH = {0};
        var iter = forEachVisibleContent((child, needsGap) -> {
            if (availableWidth == 0) return;
            if (needsGap) usedWidth[0] = Math.min(availableWidth, usedWidth[0] + gap);
            int remainingWidth = Math.max(1, availableWidth - usedWidth[0]);
            int childWidth = Math.max(1, Math.min(child.getWidth(childContext), remainingWidth));
            TipLayoutContext slotContext = TipLayoutContext.bounded(
                context.font(), context.itemStack(), childWidth);
            int ch = child.getHeight(slotContext);
            if (ch > maxH[0]) maxH[0] = ch;
            usedWidth[0] = Math.min(availableWidth, usedWidth[0] + childWidth);
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
        return getWidth(legacyContext(maxWidth));
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        TipLayoutContext childContext = childContext(context);
        int[] totalW = {padding * 2};
        var iter = forEachVisibleContent((child, needsGap) -> {
            if (needsGap) totalW[0] += gap;
            totalW[0] += child.getWidth(childContext);
        });
        if (iter.hasCollapsed()) {
            Font font = Minecraft.getInstance().font;
            Component hint = Component.translatable("tooltip.datatip.hold_shift",
                DatatipKeyMappings.SHOW_TIP.getTranslatedKeyMessage());
            if (iter.hasNonCollapsed()) totalW[0] += gap;
            totalW[0] += font.width(hint);
        }
        return context.constrainWidth(totalW[0]);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || children.isEmpty()) return;

        int[] cx = {x + padding};
        int aw = Math.max(1, maxWidth - padding * 2);
        int childAreaWidth = Math.max(0, aw - reservedHintWidth(context.font(), aw));
        TipLayoutContext childContext = TipLayoutContext.bounded(
            context.font(), context.itemStack(), Math.max(1, childAreaWidth));
        int ch = getHeight(TipLayoutContext.bounded(context.font(), context.itemStack(), maxWidth)) - padding * 2;
        var iter = forEachVisibleContent((child, needsGap) -> {
            if (needsGap) cx[0] += gap;
            int remainingWidth = Math.max(0, x + padding + childAreaWidth - cx[0]);
            if (remainingWidth == 0) return;
            int childWidth = Math.min(child.getWidth(childContext), remainingWidth);
            TipLayoutContext slotContext = TipLayoutContext.bounded(
                context.font(), context.itemStack(), Math.max(1, childWidth));
            int childH = child.getHeight(slotContext);
            int cy = switch (verticalAlign) {
                case TOP -> y + padding;
                case CENTER -> y + padding + (ch - childH) / 2;
                case BOTTOM -> y + padding + ch - childH;
            };
            child.render(context, cx[0], cy, Math.max(1, childWidth), alpha);
            cx[0] += childWidth;
        });
        if (iter.hasCollapsed()) {
            int hintY = y + padding;
            if (verticalAlign == VerticalAlign.CENTER) hintY += (ch - HINT_LINE_HEIGHT) / 2;
            else if (verticalAlign == VerticalAlign.BOTTOM) hintY += ch - HINT_LINE_HEIGHT;
            if (iter.hasNonCollapsed()) cx[0] += gap;
            int remainingWidth = Math.max(0, x + padding + aw - cx[0]);
            if (remainingWidth > 0) {
                Component hint = Component.translatable("tooltip.datatip.hold_shift",
                    DatatipKeyMappings.SHOW_TIP.getTranslatedKeyMessage());
                boolean clipped = ContentBounds.beginHorizontalClip(
                    context, cx[0], hintY, remainingWidth, HINT_LINE_HEIGHT, context.font().width(hint));
                try {
                    BaseTextContent.renderShiftHint(context, cx[0], hintY);
                } finally {
                    ContentBounds.endHorizontalClip(context, clipped);
                }
            }
        }
    }

    // 垂直对齐方式
    public enum VerticalAlign {
        TOP, CENTER, BOTTOM
    }

    private TipLayoutContext childContext(TipLayoutContext context) {
        if (!context.hasWidthLimit()) {
            return TipLayoutContext.unbounded(context.font(), context.itemStack());
        }
        return TipLayoutContext.bounded(context.font(), context.itemStack(),
            Math.max(1, context.maxWidth() - padding * 2));
    }

    private int reservedHintWidth(Font font, int availableWidth) {
        if (!anyShiftCollapsed() || availableWidth <= 0) return 0;
        Component hint = Component.translatable("tooltip.datatip.hold_shift",
            DatatipKeyMappings.SHOW_TIP.getTranslatedKeyMessage());
        int required = font.width(hint) + (hasVisibleNonCollapsed() ? gap : 0);
        return Math.min(availableWidth, required);
    }

    private boolean hasVisibleNonCollapsed() {
        for (TipContent child : children) {
            if (child.hasContent() && !child.isShiftCollapsed()) return true;
        }
        return false;
    }

    private static TipLayoutContext legacyContext(int maxWidth) {
        return maxWidth > 0
            ? TipLayoutContext.bounded(Minecraft.getInstance().font, net.minecraft.world.item.ItemStack.EMPTY, maxWidth)
            : TipLayoutContext.unbounded(Minecraft.getInstance().font, net.minecraft.world.item.ItemStack.EMPTY);
    }

    @Override
    public com.cooobird.datatip.api.layout.PreparedLayout prepare(
        com.cooobird.datatip.api.layout.TipPrepareContext context
    ) {
        return PreparedContainerSupport.prepareTree(this, context);
    }
}
