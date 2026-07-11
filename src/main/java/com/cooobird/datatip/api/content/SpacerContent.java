package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;

/**
 * 间距内容，用于在内容之间添加空白间距。
 */
public record SpacerContent(int height) implements TipContent {

    public SpacerContent {
        height = ContentBounds.spacing(height);
    }

    // 创建默认间距
    public static SpacerContent create() {
        return new SpacerContent(4);
    }

    // 创建指定高度的间距
    public static SpacerContent of(int height) {
        return new SpacerContent(height);
    }

    // 创建小间距
    public static SpacerContent small() {
        return new SpacerContent(2);
    }

    // 创建中等间距
    public static SpacerContent medium() {
        return new SpacerContent(8);
    }

    // 创建大间距
    public static SpacerContent large() {
        return new SpacerContent(16);
    }

    @Override
    public int getHeight(int maxWidth) {
        return height;
    }

    @Override
    public int getWidth(int maxWidth) {
        return maxWidth;
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        return context.hasWidthLimit() ? context.maxWidth() : 0;
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        // 间距不需要渲染任何内容
    }
}
