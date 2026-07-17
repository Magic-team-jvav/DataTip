package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.PreparedLayout;
import com.cooobird.datatip.api.layout.TipPrepareContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 让所有子节点共享同一布局区域的叠放容器。
 * <p>
 * 子节点按 {@code offsetZ} 升序稳定绘制；相同值保持 JSON 源顺序。
 * </p>
 */
public record StackContent(List<TipContent> children, int padding, HorizontalAlign horizontalAlign,
                           VerticalAlign verticalAlign) implements ContainerContent {
    public StackContent(
        List<TipContent> children,
        int padding,
        HorizontalAlign horizontalAlign,
        VerticalAlign verticalAlign
    ) {
        this.children = new ArrayList<>(List.copyOf(children != null ? children : List.of()));
        this.padding = ContentBounds.spacing(padding);
        this.horizontalAlign = horizontalAlign != null ? horizontalAlign : HorizontalAlign.LEFT;
        this.verticalAlign = verticalAlign != null ? verticalAlign : VerticalAlign.TOP;
    }

    @Override
    public List<TipContent> children() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void addChild(TipContent child) {
        children.add(Objects.requireNonNull(child, "child"));
    }

    @Override
    public int getHeight(int maxWidth) {
        return getHeight(legacyContext(maxWidth));
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        TipLayoutContext childContext = childContext(context);
        int maxHeight = 0;
        boolean visible = false;
        for (TipContent child : children) {
            if (!child.hasContent()) continue;
            if (child.isShiftCollapsed()) continue;
            visible = true;
            maxHeight = Math.max(maxHeight, child.getHeight(childContext));
        }
        return visible ? padding * 2 + maxHeight : 0;
    }

    @Override
    public int getWidth(int maxWidth) {
        return getWidth(legacyContext(maxWidth));
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        TipLayoutContext childContext = childContext(context);
        int maxWidth = 0;
        boolean visible = false;
        for (TipContent child : children) {
            if (!child.hasContent()) continue;
            if (child.isShiftCollapsed()) continue;
            visible = true;
            maxWidth = Math.max(maxWidth, child.getWidth(childContext));
        }
        return visible ? context.constrainWidth(maxWidth + padding * 2) : 0;
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0 || children.isEmpty()) return;

        int availableWidth = Math.max(1, maxWidth - padding * 2);
        TipLayoutContext layout = TipLayoutContext.bounded(context.font(), context.itemStack(), availableWidth);
        int contentHeight = contentHeight(layout);

        for (TipContent child : childrenInPaintOrder()) {
            if (!child.hasContent() || child.isShiftCollapsed()) continue;
            int childWidth = Math.min(availableWidth, child.getWidth(layout));
            int childHeight = child.getHeight(layout);
            int childX = switch (horizontalAlign) {
                case LEFT -> x + padding;
                case CENTER -> x + padding + (availableWidth - childWidth) / 2;
                case RIGHT -> x + padding + availableWidth - childWidth;
            };
            int childY = switch (verticalAlign) {
                case TOP -> y + padding;
                case CENTER -> y + padding + (contentHeight - childHeight) / 2;
                case BOTTOM -> y + padding + contentHeight - childHeight;
            };
            child.render(context, childX, childY, Math.max(1, childWidth), alpha);
        }
    }

    /**
     * 返回仅用于绘制的稳定排序副本，不修改源 children 顺序。
     */
    List<TipContent> childrenInPaintOrder() {
        List<IndexedContent> indexed = new ArrayList<>(children.size());
        for (int index = 0; index < children.size(); index++) {
            indexed.add(new IndexedContent(index, children.get(index)));
        }
        indexed.sort(Comparator
            .comparingLong((IndexedContent entry) -> entry.content().offsetZ())
            .thenComparingInt(IndexedContent::index));
        return indexed.stream().map(IndexedContent::content).toList();
    }

    private int contentHeight(TipLayoutContext context) {
        int maxHeight = 0;
        for (TipContent child : children) {
            if (child.hasContent() && !child.isShiftCollapsed()) {
                maxHeight = Math.max(maxHeight, child.getHeight(context));
            }
        }
        return maxHeight;
    }

    private TipLayoutContext childContext(TipLayoutContext context) {
        if (!context.hasWidthLimit()) {
            return TipLayoutContext.unbounded(context.font(), context.itemStack());
        }
        return TipLayoutContext.bounded(
            context.font(),
            context.itemStack(),
            Math.max(1, context.maxWidth() - padding * 2)
        );
    }

    private static TipLayoutContext legacyContext(int maxWidth) {
        return maxWidth > 0
            ? TipLayoutContext.bounded(Minecraft.getInstance().font, ItemStack.EMPTY, maxWidth)
            : TipLayoutContext.unbounded(Minecraft.getInstance().font, ItemStack.EMPTY);
    }

    public enum HorizontalAlign {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum VerticalAlign {
        TOP,
        CENTER,
        BOTTOM
    }

    private record IndexedContent(int index, TipContent content) {
    }

    @Override
    public PreparedLayout prepare(
        TipPrepareContext context
    ) {
        return PreparedContainerSupport.prepareTree(this, context);
    }
}
