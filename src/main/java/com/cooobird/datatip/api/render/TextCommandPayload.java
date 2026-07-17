package com.cooobird.datatip.api.render;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 原版文字阶段的不可变载荷。
 */
public record TextCommandPayload(
    String token,
    @Nullable PreparedTextDraw draw
) implements RenderPayload {
    public TextCommandPayload(String token, @Nullable PreparedTextDraw draw) {
        this.token = Objects.requireNonNull(token, "token");
        this.draw = draw;
    }

    public TextCommandPayload(String token) {
        this(token, null);
    }
}
