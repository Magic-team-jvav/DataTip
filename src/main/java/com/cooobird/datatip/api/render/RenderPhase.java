package com.cooobird.datatip.api.render;

/**
 * Tooltip 内容命令所属的逻辑绘制阶段。
 */
public enum RenderPhase {
    ORDINARY_TEXT,
    VISUAL_2D,
    ISOLATED_MODEL,
    OVERLAY
}
