package com.cooobird.datatip.api.render;

import com.cooobird.datatip.api.TipRenderContext;

/**
 * 已完成测量和数据解析的图片阶段绘制动作。
 */
@FunctionalInterface
public interface PreparedImageDraw {
    void render(TipRenderContext context, int x, int y, float alpha);
}
