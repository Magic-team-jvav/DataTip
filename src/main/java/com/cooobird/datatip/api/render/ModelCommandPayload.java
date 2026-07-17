package com.cooobird.datatip.api.render;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 需要独立维护三维渲染状态的模型载荷。
 */
public record ModelCommandPayload(
    String token,
    @Nullable PreparedImageDraw draw
) implements RenderPayload {
    public ModelCommandPayload(String token, @Nullable PreparedImageDraw draw) {
        this.token = Objects.requireNonNull(token, "token");
        this.draw = draw;
    }

    public ModelCommandPayload(String token) {
        this(token, null);
    }
}
