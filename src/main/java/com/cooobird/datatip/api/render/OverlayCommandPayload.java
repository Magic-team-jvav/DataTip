package com.cooobird.datatip.api.render;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 在当前局部叠放上下文中覆盖二维或模型内容的载荷。
 */
public record OverlayCommandPayload(
    String token,
    @Nullable PreparedImageDraw draw
) implements RenderPayload {
    public OverlayCommandPayload(String token, @Nullable PreparedImageDraw draw) {
        this.token = Objects.requireNonNull(token, "token");
        this.draw = draw;
    }

    public OverlayCommandPayload(String token) {
        this(token, null);
    }
}
