package com.cooobird.datatip.internal.render;

import com.cooobird.datatip.api.render.*;
import org.joml.Matrix4f;

/**
 * 选择公共仿射变换传递到各类绘制后端的方式。
 */
final class PreparedCommandTransformPlan {
    private PreparedCommandTransformPlan() {
    }

    static Mode mode(RenderPayload payload) {
        return payload instanceof ModelCommandPayload
            ? Mode.MODEL_COMPOSITE
            : Mode.POSE_STACK;
    }

    static boolean usesLocalViewport(RenderPayload payload) {
        if (payload instanceof Visual2DCommandPayload visual) {
            return visual.draw() instanceof PreparedViewportDraw;
        }
        if (payload instanceof OverlayCommandPayload overlay) {
            return overlay.draw() instanceof PreparedViewportDraw;
        }
        return false;
    }

    static Matrix4f compositeMatrix(
        Matrix4f base,
        RenderTransform transform
    ) {
        return new Matrix4f(base).mul(transform.matrix());
    }

    enum Mode {
        POSE_STACK,
        MODEL_COMPOSITE
    }
}
