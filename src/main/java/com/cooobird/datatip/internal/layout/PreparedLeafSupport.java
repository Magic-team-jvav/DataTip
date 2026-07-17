package com.cooobird.datatip.internal.layout;

import com.cooobird.datatip.api.layout.OverflowPolicy;
import com.cooobird.datatip.api.layout.PreparedLayout;
import com.cooobird.datatip.api.layout.TipRect;
import com.cooobird.datatip.api.layout.TipSize;
import com.cooobird.datatip.api.render.*;

import java.util.List;

/**
 * 已冻结二维或模型绘制动作的叶节点布局构造器。
 */
public final class PreparedLeafSupport {
    private PreparedLeafSupport() {
    }

    public static PreparedLayout draw(
        long naturalWidth,
        long naturalHeight,
        long preferredWidth,
        long preferredHeight,
        long allocatedWidth,
        long allocatedHeight,
        OverflowPolicy overflowPolicy,
        RenderPhase phase,
        String token,
        PreparedImageDraw draw
    ) {
        TipRect natural = new TipRect(
            0,
            0,
            Math.max(0, naturalWidth),
            Math.max(0, naturalHeight)
        );
        TipRect allocated = new TipRect(
            0,
            0,
            Math.max(0, allocatedWidth),
            Math.max(0, allocatedHeight)
        );
        if (allocated.width() == 0 && allocated.height() == 0) {
            return empty(0, 0);
        }

        RenderPayload payload = switch (phase) {
            case VISUAL_2D -> new Visual2DCommandPayload(token, draw);
            case ISOLATED_MODEL -> new ModelCommandPayload(token, draw);
            case OVERLAY -> new OverlayCommandPayload(token, draw);
            case ORDINARY_TEXT -> throw new IllegalArgumentException(
                "Prepared leaf support does not accept ordinary text"
            );
        };
        RenderCommand command = RenderCommand.positioned(
            phase,
            0,
            0,
            allocated,
            null,
            payload
        );
        return PreparedLayout.create(
            natural,
            new TipSize(
                Math.max(0, preferredWidth),
                Math.max(0, preferredHeight)
            ),
            allocated,
            allocated,
            null,
            overflowPolicy,
            new RenderCommandPipeline(
                List.of(),
                RenderCommandGroup.root(List.of(command))
            )
        );
    }

    public static PreparedLayout empty(int width, int height) {
        TipRect bounds = new TipRect(0, 0, width, height);
        return PreparedLayout.create(
            bounds,
            new TipSize(width, height),
            bounds,
            TipRect.ZERO,
            null,
            OverflowPolicy.NONE,
            RenderCommandPipeline.empty()
        );
    }
}
