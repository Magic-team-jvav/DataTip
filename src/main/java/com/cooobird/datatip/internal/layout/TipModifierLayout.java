package com.cooobird.datatip.internal.layout;

import com.cooobird.datatip.api.layout.*;
import com.cooobird.datatip.api.node.TipModifiers;
import com.cooobird.datatip.api.render.RenderCommandGroup;
import com.cooobird.datatip.api.render.RenderCommandPipeline;
import com.cooobird.datatip.api.render.RenderTransform;

import java.util.List;
import java.util.Objects;

/**
 * 将公共节点修饰符统一应用到一次准备完成的布局。
 *
 * <p>该类只改变 DataTip 组件内部的几何与命令树，不参与原版 Tooltip 的
 * 定位、鼠标锚点、背景或边框绘制。</p>
 */
public final class TipModifierLayout {
    private TipModifierLayout() {
    }

    public static PreparedLayout apply(
        PreparedLayout inner,
        TipModifiers modifiers
    ) {
        Objects.requireNonNull(inner, "inner");
        Objects.requireNonNull(modifiers, "modifiers");
        if (!modifiers.visible()) return empty();
        if (modifiers.equals(TipModifiers.DEFAULT)) return inner;

        TipRect innerAllocated = inner.allocatedBounds();
        long constrainedWidth = constrain(
            innerAllocated.width(),
            modifiers.sizeConstraints().width(),
            modifiers.sizeConstraints().minWidth(),
            modifiers.sizeConstraints().maxWidth()
        );
        long constrainedHeight = constrain(
            innerAllocated.height(),
            modifiers.sizeConstraints().height(),
            modifiers.sizeConstraints().minHeight(),
            modifiers.sizeConstraints().maxHeight()
        );

        double scaleX = modifiers.scaleX();
        double scaleY = modifiers.scaleY();
        if (modifiers.overflow() == OverflowPolicy.SCALE_DOWN) {
            double fit = fitScale(
                innerAllocated.width(),
                innerAllocated.height(),
                constrainedWidth,
                constrainedHeight
            );
            scaleX *= fit;
            scaleY *= fit;
        }

        double pivotX = constrainedWidth * modifiers.pivotX();
        double pivotY = constrainedHeight * modifiers.pivotY();
        RenderTransform contentTransform = RenderTransform.around(
            pivotX,
            pivotY,
            scaleX,
            scaleY,
            modifiers.rotation(),
            modifiers.offsetX(),
            modifiers.offsetY()
        );
        TipModifiers.Margins margins = modifiers.margins();
        contentTransform = RenderTransform.translation(
            margins.left(),
            margins.top()
        ).compose(contentTransform);

        TipRect localClip = clipBounds(
            inner,
            constrainedWidth,
            constrainedHeight,
            modifiers.overflow()
        );
        TipRect contentVisual = localClip != null
            ? inner.visualBounds().intersection(localClip)
            : inner.visualBounds();
        TipRect transformedVisual = contentTransform.transformBounds(
            contentVisual
        );
        TipRect transformedClip = localClip != null
            ? contentTransform.transformBounds(localClip)
            : null;
        long flowRight = TipMath.add(
            TipMath.add(margins.left(), constrainedWidth),
            margins.right()
        );
        long flowBottom = TipMath.add(
            TipMath.add(margins.top(), constrainedHeight),
            margins.bottom()
        );
        long left = Math.min(0, Math.min(flowRight, transformedVisual.x()));
        long top = Math.min(0, Math.min(flowBottom, transformedVisual.y()));
        long right = Math.max(
            Math.max(0, flowRight),
            transformedVisual.right()
        );
        long bottom = Math.max(
            Math.max(0, flowBottom),
            transformedVisual.bottom()
        );
        if (transformedClip != null) {
            left = Math.min(left, transformedClip.x());
            top = Math.min(top, transformedClip.y());
            right = Math.max(right, transformedClip.right());
            bottom = Math.max(bottom, transformedClip.bottom());
        }
        long width = TipMath.distance(right, left);
        long height = TipMath.distance(bottom, top);
        long flowLeft = Math.min(0, flowRight);
        long flowTop = Math.min(0, flowBottom);
        TipRect flowBounds = new TipRect(
            TipMath.subtract(flowLeft, left),
            TipMath.subtract(flowTop, top),
            TipMath.distance(Math.max(0, flowRight), flowLeft),
            TipMath.distance(Math.max(0, flowBottom), flowTop)
        );

        RenderTransform normalized = RenderTransform.translation(
            TipMath.subtract(0, left),
            TipMath.subtract(0, top)
        ).compose(contentTransform);
        TipRect visual = normalized.transformBounds(contentVisual);
        TipRect clip = localClip != null
            ? normalized.transformBounds(localClip)
            : null;

        RenderCommandPipeline source = inner.renderPlan();
        RenderCommandPipeline transformedPlan = new RenderCommandPipeline(
            RenderCommandGroup.transformedLayer(
                0,
                0,
                normalized,
                modifiers.opacity(),
                localClip,
                List.of(source.textRoot())
            ),
            RenderCommandGroup.transformedLayer(
                0,
                0,
                normalized,
                modifiers.opacity(),
                localClip,
                List.of(source.imageRoot())
            )
        );
        TipRect allocated = new TipRect(0, 0, width, height);
        return PreparedLayout.create(
            allocated,
            new TipSize(width, height),
            allocated,
            visual,
            flowBounds,
            clip,
            modifiers.overflow(),
            transformedPlan
        );
    }

    /**
     * 在内容测量前下发宽高约束，使文本等原生可换行内容按最终宽度准备。
     */
    public static TipPrepareContext childContext(
        TipPrepareContext parent,
        TipModifiers modifiers
    ) {
        TipModifiers.SizeConstraints constraints = modifiers.sizeConstraints();
        TipMeasureSpec source = parent.measureSpec();
        long hardWidth = constrainedHardMaximum(
            source.hardMaxWidth(),
            constraints.width(),
            constraints.maxWidth()
        );
        long hardHeight = constrainedHardMaximum(
            source.hardMaxHeight(),
            constraints.height(),
            constraints.maxHeight()
        );
        long softWidth = source.softMaxWidth();
        if (constraints.width() != null) {
            softWidth = Math.min(hardWidth, constraints.width());
        } else if (constraints.maxWidth() != null) {
            softWidth = softWidth == 0
                ? hardWidth
                : Math.min(softWidth, hardWidth);
        }
        var layout = parent.layoutContext();
        if (layout != null) {
            layout = layout.withMaxWidth(
                (int) Math.min(Integer.MAX_VALUE, hardWidth)
            );
        }
        return new TipPrepareContext(
            layout,
            new TipMeasureSpec(softWidth, hardWidth, hardHeight)
        );
    }

    public static PreparedLayout empty() {
        return PreparedLayout.create(
            TipRect.ZERO,
            new TipSize(0, 0),
            TipRect.ZERO,
            TipRect.ZERO,
            null,
            OverflowPolicy.NONE,
            RenderCommandPipeline.empty()
        );
    }

    private static long constrain(
        long natural,
        Long fixed,
        Long minimum,
        Long maximum
    ) {
        long result = fixed != null ? fixed : natural;
        if (minimum != null) result = Math.max(result, minimum);
        if (maximum != null) result = Math.min(result, maximum);
        return result;
    }

    private static long constrainedHardMaximum(
        long parentMaximum,
        Long fixed,
        Long maximum
    ) {
        long result = parentMaximum;
        if (fixed != null && fixed > 0) result = Math.min(result, fixed);
        if (maximum != null && maximum > 0) result = Math.min(result, maximum);
        return Math.max(1, result);
    }

    private static double fitScale(
        long naturalWidth,
        long naturalHeight,
        long targetWidth,
        long targetHeight
    ) {
        double x = naturalWidth > 0
            ? Math.min(1.0, (double) targetWidth / naturalWidth)
            : 1.0;
        double y = naturalHeight > 0
            ? Math.min(1.0, (double) targetHeight / naturalHeight)
            : 1.0;
        return Math.max(Double.MIN_NORMAL, Math.min(x, y));
    }

    private static TipRect clipBounds(
        PreparedLayout inner,
        long width,
        long height,
        OverflowPolicy overflow
    ) {
        if (overflow != OverflowPolicy.CLIP) {
            return inner.clipBounds().orElse(null);
        }
        TipRect constraint = new TipRect(0, 0, width, height);
        return inner.clipBounds()
            .map(constraint::intersection)
            .orElse(constraint);
    }
}
