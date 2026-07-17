package com.cooobird.datatip.api.layout;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.render.*;

import java.util.List;

/**
 * 将旧 TipContent 保守适配为单次布局快照。
 */
public final class LegacyContentPreparer {
    private LegacyContentPreparer() {
    }

    public static PreparedLayout prepare(TipContent content, TipPrepareContext context) {
        TipMeasureSpec spec = context.measureSpec();
        long preferredLimit = spec.softMaxWidth() > 0
            ? Math.min(spec.softMaxWidth(), spec.hardMaxWidth())
            : spec.hardMaxWidth();
        int widthLimit = (int) Math.min(Integer.MAX_VALUE, preferredLimit);
        TipLayoutContext layoutContext = context.layoutContext();
        int width;
        int height;
        if (layoutContext != null) {
            TipLayoutContext boundedContext = layoutContext.withMaxWidth(widthLimit);
            width = Math.max(0, content.getWidth(boundedContext));
            height = Math.max(0, content.getHeight(boundedContext));
        } else {
            width = Math.max(0, content.getWidth(widthLimit));
            height = Math.max(0, content.getHeight(widthLimit));
        }
        PreparedLayout measured = PreparedLayout.constrain(
            new TipSize(width, height),
            spec,
            OverflowPolicy.CLIP
        );
        int renderWidth = (int) Math.max(
            1,
            Math.min(Integer.MAX_VALUE, measured.allocatedBounds().width())
        );
        RenderCommand command = RenderCommand.positioned(
            RenderPhase.VISUAL_2D,
            content.offsetZ(),
            0,
            measured.visualBounds(),
            measured.clipBounds().orElse(null),
            new Visual2DCommandPayload(
                "legacy:" + content.getClass().getName(),
                (renderContext, x, y, alpha) ->
                    content.render(renderContext, x, y, renderWidth, alpha)
            )
        );
        return PreparedLayout.create(
            measured.naturalBounds(),
            measured.preferredSize(),
            measured.allocatedBounds(),
            measured.visualBounds(),
            measured.clipBounds().orElse(null),
            measured.overflowPolicy(),
            new RenderCommandPipeline(
                List.of(),
                RenderCommandGroup.root(List.of(command))
            )
        );
    }
}
