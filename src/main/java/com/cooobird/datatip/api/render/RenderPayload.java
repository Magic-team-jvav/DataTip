package com.cooobird.datatip.api.render;

/**
 * 绘制命令的类型化载荷。
 */
public sealed interface RenderPayload
    permits TextCommandPayload, Visual2DCommandPayload,
    ModelCommandPayload, OverlayCommandPayload {
    String token();
}
