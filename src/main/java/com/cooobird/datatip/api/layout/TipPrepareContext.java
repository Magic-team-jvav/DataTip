package com.cooobird.datatip.api.layout;

import com.cooobird.datatip.api.TipLayoutContext;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 准备单个内容节点时使用的不可变上下文。
 */
public record TipPrepareContext(
    @Nullable TipLayoutContext layoutContext,
    TipMeasureSpec measureSpec
) {
    public TipPrepareContext(
        @Nullable TipLayoutContext layoutContext,
        TipMeasureSpec measureSpec
    ) {
        this.layoutContext = layoutContext;
        this.measureSpec = Objects.requireNonNull(measureSpec, "measureSpec");
    }

    /**
     * 为不依赖 Minecraft 字体和物品栈的纯布局测试创建上下文。
     */
    public TipPrepareContext(TipMeasureSpec measureSpec) {
        this(null, measureSpec);
    }

    public TipLayoutContext requireLayoutContext() {
        if (layoutContext == null) {
            throw new IllegalStateException("Tip layout context is required for this content");
        }
        return layoutContext;
    }
}
