package com.cooobird.datatip.api.node;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.api.content.TipAnimationTraversal;
import com.cooobird.datatip.api.layout.PreparedLayout;
import com.cooobird.datatip.api.layout.TipPrepareContext;
import com.cooobird.datatip.event.TipRenderEventHandler;
import com.cooobird.datatip.internal.layout.TipModifierLayout;

import java.util.Objects;

/**
 * 统一承载内容定义和通用修饰符的节点。
 */
public record TipNode(TipContent inner, TipModifiers modifiers) implements TipContent {
    public TipNode(TipContent inner, TipModifiers modifiers) {
        this.inner = Objects.requireNonNull(inner, "inner");
        this.modifiers = Objects.requireNonNull(modifiers, "modifiers");
    }

    /**
     * 规范化内容节点，避免无意义的重复包装。
     */
    public static TipNode wrap(TipContent content, TipModifiers modifiers) {
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(modifiers, "modifiers");
        if (content instanceof TipNode node && modifiers.equals(TipModifiers.DEFAULT)) {
            return node;
        }
        return new TipNode(content, modifiers);
    }

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
        inner.render(context, x, y, maxWidth, alpha);
    }

    @Override
    public PreparedLayout prepare(TipPrepareContext context) {
        if (isHidden(context)) {
            return TipModifierLayout.empty();
        }
        return TipModifierLayout.apply(
            inner.prepare(TipModifierLayout.childContext(context, modifiers)),
            modifiers
        );
    }

    private boolean isHidden(TipPrepareContext context) {
        return !modifiers.visible()
            || isShiftHidden()
            || !passesConditions(context);
    }

    private boolean isShiftHidden() {
        return modifiers.shift() && !TipRenderEventHandler.isShowTipDown();
    }

    private boolean passesConditions(TipPrepareContext context) {
        if (modifiers.conditions().isEmpty()) {
            return true;
        }
        TipLayoutContext layoutContext = context.layoutContext();
        if (layoutContext == null) {
            return false;
        }
        return ConditionChecker.checkAll(
            modifiers.conditions(),
            layoutContext.itemStack()
        );
    }

    @Override
    public boolean isShiftCollapsed() {
        return isShiftHidden() || inner.isShiftCollapsed();
    }

    @Override
    public boolean hasContent() {
        return inner.hasContent();
    }

    @Override
    public boolean isAnimated() {
        return TipAnimationTraversal.isAnimated(this);
    }

    @Override
    public void tick(int tickCount) {
        TipAnimationTraversal.tick(this, tickCount);
    }

    @Override
    public long offsetZ() {
        return modifiers.offsetZ();
    }

    @Override
    public long layoutOffsetX() {
        return modifiers.offsetX();
    }

    @Override
    public long layoutOffsetY() {
        return modifiers.offsetY();
    }
}
