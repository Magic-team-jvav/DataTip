package com.cooobird.datatip.api.render;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 图片阶段的普通二维绘制载荷。
 */
public record Visual2DCommandPayload(
    String token,
    @Nullable PreparedImageDraw draw
) implements RenderPayload {
    public Visual2DCommandPayload(String token, @Nullable PreparedImageDraw draw) {
        this.token = Objects.requireNonNull(token, "token");
        this.draw = draw;
    }

    public Visual2DCommandPayload(String token) {
        this(token, null);
    }
}
