package com.cooobird.datatip.api.render;

import com.cooobird.datatip.api.layout.TipRect;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * 不可变绘制叶命令。
 *
 * @param phase       逻辑绘制阶段
 * @param offsetZ     当前父叠放上下文中的顺序
 * @param sourceIndex JSON 源顺序
 * @param bounds      该命令的实际绘制边界
 * @param clipBounds  可选物理裁剪边界
 * @param payload     交给对应阶段后端执行的类型化载荷
 */
public record RenderCommand(
    RenderPhase phase,
    long offsetZ,
    int sourceIndex,
    TipRect bounds,
    @Nullable TipRect clipBounds,
    RenderPayload payload
) implements RenderCommandNode {
    public RenderCommand(
        RenderPhase phase,
        long offsetZ,
        int sourceIndex,
        TipRect bounds,
        @Nullable TipRect clipBounds,
        RenderPayload payload
    ) {
        RenderPhase checkedPhase = Objects.requireNonNull(phase, "phase");
        RenderPayload checkedPayload = Objects.requireNonNull(
            payload,
            "payload"
        );
        if (sourceIndex < 0) {
            throw new IllegalArgumentException("Source index must be non-negative");
        }
        if (!payloadMatchesPhase(checkedPhase, checkedPayload)) {
            throw new IllegalArgumentException(
                "Render payload type does not match render phase"
            );
        }
        this.phase = checkedPhase;
        this.offsetZ = offsetZ;
        this.sourceIndex = sourceIndex;
        this.bounds = Objects.requireNonNull(bounds, "bounds");
        this.clipBounds = clipBounds;
        this.payload = checkedPayload;
    }

    public RenderCommand(
        RenderPhase phase,
        long offsetZ,
        int sourceIndex,
        String token
    ) {
        this(
            phase,
            offsetZ,
            sourceIndex,
            TipRect.ZERO,
            null,
            payloadFor(phase, token)
        );
    }

    public static RenderCommand leaf(
        RenderPhase phase,
        long offsetZ,
        int sourceIndex,
        String token
    ) {
        return new RenderCommand(phase, offsetZ, sourceIndex, token);
    }

    public static RenderCommand positioned(
        RenderPhase phase,
        long offsetZ,
        int sourceIndex,
        TipRect bounds,
        @Nullable TipRect clipBounds,
        RenderPayload payload
    ) {
        return new RenderCommand(
            phase,
            offsetZ,
            sourceIndex,
            bounds,
            clipBounds,
            payload
        );
    }

    public String token() {
        return payload.token();
    }

    @Override
    public void execute(RenderPass pass, Consumer<String> sink) {
        Objects.requireNonNull(pass, "pass");
        Objects.requireNonNull(sink, "sink");
        boolean selected = phase == RenderPhase.ORDINARY_TEXT
            ? pass == RenderPass.TEXT
            : pass == RenderPass.IMAGE;
        if (selected) {
            sink.accept(token());
        }
    }

    private static RenderPayload payloadFor(RenderPhase phase, String token) {
        Objects.requireNonNull(phase, "phase");
        return switch (phase) {
            case ORDINARY_TEXT -> new TextCommandPayload(token);
            case VISUAL_2D -> new Visual2DCommandPayload(token);
            case ISOLATED_MODEL -> new ModelCommandPayload(token);
            case OVERLAY -> new OverlayCommandPayload(token);
        };
    }

    private static boolean payloadMatchesPhase(
        RenderPhase phase,
        RenderPayload payload
    ) {
        return switch (phase) {
            case ORDINARY_TEXT -> payload instanceof TextCommandPayload;
            case VISUAL_2D -> payload instanceof Visual2DCommandPayload;
            case ISOLATED_MODEL -> payload instanceof ModelCommandPayload;
            case OVERLAY -> payload instanceof OverlayCommandPayload;
        };
    }
}
