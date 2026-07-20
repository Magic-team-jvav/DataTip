package com.cooobird.datatip.internal.render;

import com.cooobird.datatip.api.render.RenderPhase;

/**
 * 标记会写入独立三维深度的绘制阶段。
 */
final class RenderDepthIsolation {
    private RenderDepthIsolation() {
    }

    static boolean requiresBarrier(RenderPhase phase) {
        return phase == RenderPhase.ISOLATED_MODEL;
    }
}
