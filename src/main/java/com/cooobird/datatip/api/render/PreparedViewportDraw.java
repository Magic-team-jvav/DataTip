package com.cooobird.datatip.api.render;

import com.cooobird.datatip.api.TipRenderContext;

/**
 * 需要获知当前实际裁剪区域的准备后绘制动作。
 */
public interface PreparedViewportDraw extends PreparedImageDraw {
    void render(
        TipRenderContext context,
        int x,
        int y,
        float alpha,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight
    );

    @Override
    default void render(
        TipRenderContext context,
        int x,
        int y,
        float alpha
    ) {
        render(
            context,
            x,
            y,
            alpha,
            Integer.MIN_VALUE,
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE
        );
    }
}
