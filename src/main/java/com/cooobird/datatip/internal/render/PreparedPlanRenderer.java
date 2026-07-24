package com.cooobird.datatip.internal.render;

import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.PreparedLayout;
import com.cooobird.datatip.api.layout.TipMath;
import com.cooobird.datatip.api.layout.TipRect;
import com.cooobird.datatip.api.render.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayDeque;

/**
 * 执行已经完成测量、分层和裁剪计算的图片阶段命令。
 *
 * <p>轮播等动态容器也通过这里执行子布局，避免重新退回旧的直接
 * {@code TipContent.render} 路径。</p>
 */
public final class PreparedPlanRenderer {
    private static final ThreadLocal<Integer> TRANSFORMED_VIEWPORT_DEPTH =
        ThreadLocal.withInitial(() -> 0);

    private PreparedPlanRenderer() {
    }

    public static void renderLayout(
        PreparedLayout layout,
        TipRenderContext context,
        int originX,
        int originY,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha
    ) {
        renderLayout(
            layout,
            context,
            originX,
            originY,
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight,
            alpha,
            true
        );
    }

    public static void renderImageLayout(
        PreparedLayout layout,
        TipRenderContext context,
        int originX,
        int originY,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha
    ) {
        renderLayout(
            layout,
            context,
            originX,
            originY,
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight,
            alpha,
            false
        );
    }

    private static void renderLayout(
        PreparedLayout layout,
        TipRenderContext context,
        int originX,
        int originY,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha,
        boolean includeText
    ) {
        renderPlan(
            layout.renderPlan(),
            context,
            originX,
            originY,
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight,
            alpha,
            includeText
        );
    }

    public static void renderPlan(
        RenderCommandPipeline plan,
        TipRenderContext context,
        int originX,
        int originY,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha,
        boolean includeText
    ) {
        if (alpha <= 0.0f) return;
        int viewportRight = addCoordinate(viewportX, viewportWidth);
        int viewportBottom = addCoordinate(viewportY, viewportHeight);
        if (viewportRight <= viewportX || viewportBottom <= viewportY) return;

        boolean managePhysicalScissor = TRANSFORMED_VIEWPORT_DEPTH.get() == 0;
        Matrix4f hostTooltipPose = new Matrix4f(
            context.graphics().pose().last().pose()
        );
        if (managePhysicalScissor) {
            context.graphics().flush();
            context.graphics().enableScissor(
                viewportX,
                viewportY,
                viewportRight,
                viewportBottom
            );
        }
        try {
            if (includeText) {
                renderTree(
                    plan.textRoot(),
                    RenderPhase.ORDINARY_TEXT,
                    context,
                    originX,
                    originY,
                    viewportX,
                    viewportY,
                    viewportWidth,
                    viewportHeight,
                    alpha,
                    hostTooltipPose
                );
            }
            renderTree(
                plan.imageRoot(),
                null,
                context,
                originX,
                originY,
                viewportX,
                viewportY,
                viewportWidth,
                viewportHeight,
                alpha,
                hostTooltipPose
            );
        } finally {
            if (managePhysicalScissor) {
                try {
                    context.graphics().flush();
                } finally {
                    context.graphics().disableScissor();
                }
            }
        }
    }

    private static void renderTree(
        RenderCommandGroup root,
        @Nullable RenderPhase requiredPhase,
        TipRenderContext context,
        int originX,
        int originY,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha,
        Matrix4f hostTooltipPose
    ) {
        ArrayDeque<RenderTraversal> work = new ArrayDeque<>();
        work.push(new RenderTraversal(
            root,
            RenderTransform.translation(originX, originY),
            alpha,
            null
        ));
        while (!work.isEmpty()) {
            RenderTraversal current = work.pop();
            if (current.node() instanceof RenderCommand command) {
                if (requiredPhase != null && command.phase() != requiredPhase) {
                    continue;
                }
                if (requiredPhase == null
                    && command.phase() == RenderPhase.ORDINARY_TEXT) {
                    continue;
                }
                renderResolvedCommand(
                    command,
                    current.transform(),
                    current.alpha(),
                    current.clipBounds(),
                    context,
                    viewportX,
                    viewportY,
                    viewportWidth,
                    viewportHeight,
                    hostTooltipPose
                );
                continue;
            }

            RenderCommandGroup group = (RenderCommandGroup) current.node();
            RenderTransform transform = current.transform().compose(
                group.localTransform()
            );
            float resolvedAlpha = (float) Math.max(
                0.0,
                Math.min(1.0, current.alpha() * group.opacity())
            );
            if (resolvedAlpha <= 0.0f) continue;
            TipRect clip = current.clipBounds();
            if (group.clipBounds() != null) {
                clip = intersect(
                    clip,
                    transform.transformBounds(group.clipBounds())
                );
            }
            var children = group.children();
            for (int index = children.size() - 1; index >= 0; index--) {
                work.push(new RenderTraversal(
                    children.get(index),
                    transform,
                    resolvedAlpha,
                    clip
                ));
            }
        }
    }

    private static void renderResolvedCommand(
        RenderCommand command,
        RenderTransform transform,
        float alpha,
        @Nullable TipRect inheritedClip,
        TipRenderContext context,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        Matrix4f hostTooltipPose
    ) {
        TipRect bounds = transform.transformBounds(command.bounds());
        TipRect clip = inheritedClip;
        if (command.clipBounds() != null) {
            clip = intersect(
                clip,
                transform.transformBounds(command.clipBounds())
            );
        }
        RenderCommand resolved = RenderCommand.positioned(
            command.phase(),
            command.offsetZ(),
            command.sourceIndex(),
            bounds,
            clip,
            command.payload()
        );
        if (transform.isTranslationOnly()) {
            renderResolvedWithoutPose(
                resolved,
                context,
                viewportX,
                viewportY,
                viewportWidth,
                viewportHeight,
                alpha,
                hostTooltipPose
            );
            return;
        }

        if (!intersects(bounds, viewportX, viewportY, viewportWidth,
            viewportHeight)) {
            return;
        }
        TipRect physicalClip = intersect(
            clip,
            new TipRect(
                viewportX,
                viewportY,
                Math.max(0, viewportWidth),
                Math.max(0, viewportHeight)
            )
        );
        if (physicalClip == null
            || physicalClip.width() == 0
            || physicalClip.height() == 0) {
            return;
        }
        boolean narrowed = physicalClip.x() != viewportX
            || physicalClip.y() != viewportY
            || physicalClip.width() != viewportWidth
            || physicalClip.height() != viewportHeight;
        if (narrowed) {
            context.graphics().flush();
            context.graphics().enableScissor(
                dimension(physicalClip.x()),
                dimension(physicalClip.y()),
                dimension(physicalClip.right()),
                dimension(physicalClip.bottom())
            );
        }
        context.graphics().pose().pushPose();
        try {
            context.graphics().pose().last().pose().mul(transform.matrix());
            if (command.phase() == RenderPhase.ORDINARY_TEXT) {
                renderTextPayload(command, context, alpha);
            } else {
                boolean viewportDraw = PreparedCommandTransformPlan
                    .usesLocalViewport(command.payload());
                try {
                    if (viewportDraw) {
                        TRANSFORMED_VIEWPORT_DEPTH.set(
                            TRANSFORMED_VIEWPORT_DEPTH.get() + 1
                        );
                    }
                    drawPayload(
                        command,
                        context,
                        dimension(command.bounds().x()),
                        dimension(command.bounds().y()),
                        dimension(command.bounds().x()),
                        dimension(command.bounds().y()),
                        dimension(command.bounds().width()),
                        dimension(command.bounds().height()),
                        dimension(physicalClip.x()),
                        dimension(physicalClip.y()),
                        dimension(physicalClip.width()),
                        dimension(physicalClip.height()),
                        alpha,
                        hostTooltipPose
                    );
                } finally {
                    if (viewportDraw) {
                        int depth = TRANSFORMED_VIEWPORT_DEPTH.get() - 1;
                        if (depth == 0) {
                            TRANSFORMED_VIEWPORT_DEPTH.remove();
                        } else {
                            TRANSFORMED_VIEWPORT_DEPTH.set(depth);
                        }
                    }
                }
            }
        } finally {
            context.graphics().pose().popPose();
            restoreOuterScissor(
                narrowed,
                context
            );
        }
    }

    private static void restoreOuterScissor(
        boolean narrowed,
        TipRenderContext context
    ) {
        if (!narrowed) return;
        context.graphics().flush();
        // GuiGraphics 的裁剪是栈结构；弹出窄裁剪后，外层视口会自动恢复。
        context.graphics().disableScissor();
    }

    private static void renderResolvedWithoutPose(
        RenderCommand command,
        TipRenderContext context,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha,
        Matrix4f hostTooltipPose
    ) {
        if (command.phase() == RenderPhase.ORDINARY_TEXT) {
            renderTextCommand(
                command,
                context,
                0,
                0,
                viewportX,
                viewportY,
                viewportWidth,
                viewportHeight,
                alpha
            );
        } else {
            renderImageCommandInsideViewport(
                command,
                context,
                0,
                0,
                viewportX,
                viewportY,
                viewportWidth,
                viewportHeight,
                alpha,
                hostTooltipPose
            );
        }
    }

    private static void renderTextPayload(
        RenderCommand command,
        TipRenderContext context,
        float alpha
    ) {
        if (!(command.payload() instanceof TextCommandPayload payload)
            || payload.draw() == null) {
            return;
        }
        payload.draw().render(
            context.font(),
            dimension(command.bounds().x()),
            dimension(command.bounds().y()),
            context.graphics().pose().last().pose(),
            context.graphics().bufferSource(),
            alpha
        );
    }

    @Nullable
    private static TipRect intersect(
        @Nullable TipRect first,
        @Nullable TipRect second
    ) {
        if (first == null) return second;
        if (second == null) return first;
        return first.intersection(second);
    }

    private static boolean intersects(
        TipRect bounds,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight
    ) {
        long right = (long) viewportX + viewportWidth;
        long bottom = (long) viewportY + viewportHeight;
        return bounds.right() > viewportX
            && bounds.bottom() > viewportY
            && bounds.x() < right
            && bounds.y() < bottom;
    }

    private static int dimension(long value) {
        return (int) Math.max(
            Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, value)
        );
    }

    private record RenderTraversal(
        RenderCommandNode node,
        RenderTransform transform,
        float alpha,
        @Nullable TipRect clipBounds
    ) {
    }

    private static void renderTextCommand(
        RenderCommand command,
        TipRenderContext context,
        int originX,
        int originY,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha
    ) {
        if (!(command.payload() instanceof TextCommandPayload payload)
            || payload.draw() == null) {
            return;
        }
        int commandLeft = addCoordinate(originX, command.bounds().x());
        int commandTop = addCoordinate(originY, command.bounds().y());
        int commandRight = addCoordinate(originX, command.bounds().right());
        int commandBottom = addCoordinate(originY, command.bounds().bottom());
        int viewportRight = addCoordinate(viewportX, viewportWidth);
        int viewportBottom = addCoordinate(viewportY, viewportHeight);
        if (commandRight <= viewportX
            || commandBottom <= viewportY
            || commandLeft >= viewportRight
            || commandTop >= viewportBottom) {
            return;
        }

        if (command.clipBounds() == null) {
            payload.draw().render(
                context.font(),
                addCoordinate(originX, command.bounds().x()),
                addCoordinate(originY, command.bounds().y()),
                context.graphics().pose().last().pose(),
                context.graphics().bufferSource(),
                alpha
            );
            return;
        }
        int left = Math.max(
            viewportX,
            addCoordinate(originX, command.clipBounds().x())
        );
        int top = Math.max(
            viewportY,
            addCoordinate(originY, command.clipBounds().y())
        );
        int right = Math.min(
            viewportRight,
            addCoordinate(originX, command.clipBounds().right())
        );
        int bottom = Math.min(
            viewportBottom,
            addCoordinate(originY, command.clipBounds().bottom())
        );
        if (right <= left || bottom <= top) return;
        context.graphics().flush();
        context.graphics().enableScissor(left, top, right, bottom);
        try {
            payload.draw().render(
                context.font(),
                addCoordinate(originX, command.bounds().x()),
                addCoordinate(originY, command.bounds().y()),
                context.graphics().pose().last().pose(),
                context.graphics().bufferSource(),
                alpha
            );
        } finally {
            try {
                context.graphics().flush();
            } finally {
                context.graphics().disableScissor();
            }
        }
    }

    public static void renderImageCommand(
        RenderCommand command,
        TipRenderContext context,
        int originX,
        int originY,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha
    ) {
        int viewportRight = addCoordinate(viewportX, viewportWidth);
        int viewportBottom = addCoordinate(viewportY, viewportHeight);
        if (viewportRight <= viewportX || viewportBottom <= viewportY) return;
        context.graphics().flush();
        context.graphics().enableScissor(
            viewportX,
            viewportY,
            viewportRight,
            viewportBottom
        );
        Matrix4f hostTooltipPose = new Matrix4f(
            context.graphics().pose().last().pose()
        );
        try {
            renderImageCommandInsideViewport(
                command,
                context,
                originX,
                originY,
                viewportX,
                viewportY,
                viewportWidth,
                viewportHeight,
                alpha,
                hostTooltipPose
            );
        } finally {
            try {
                context.graphics().flush();
            } finally {
                context.graphics().disableScissor();
            }
        }
    }

    private static void renderImageCommandInsideViewport(
        RenderCommand command,
        TipRenderContext context,
        int originX,
        int originY,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha,
        Matrix4f hostTooltipPose
    ) {
        int commandLeft = addCoordinate(originX, command.bounds().x());
        int commandTop = addCoordinate(originY, command.bounds().y());
        int commandRight = addCoordinate(originX, command.bounds().right());
        int commandBottom = addCoordinate(originY, command.bounds().bottom());
        int viewportRight = addCoordinate(viewportX, viewportWidth);
        int viewportBottom = addCoordinate(viewportY, viewportHeight);
        if (commandRight <= viewportX
            || commandBottom <= viewportY
            || commandLeft >= viewportRight
            || commandTop >= viewportBottom) {
            return;
        }

        int x = addCoordinate(originX, command.bounds().x());
        int y = addCoordinate(originY, command.bounds().y());
        if (command.clipBounds() == null) {
            drawPayload(
                command,
                context,
                x,
                y,
                viewportX,
                viewportY,
                viewportWidth,
                viewportHeight,
                viewportX,
                viewportY,
                viewportWidth,
                viewportHeight,
                alpha,
                hostTooltipPose
            );
            return;
        }
        int left = Math.max(
            viewportX,
            addCoordinate(originX, command.clipBounds().x())
        );
        int top = Math.max(
            viewportY,
            addCoordinate(originY, command.clipBounds().y())
        );
        int right = Math.min(
            viewportRight,
            addCoordinate(originX, command.clipBounds().right())
        );
        int bottom = Math.min(
            viewportBottom,
            addCoordinate(originY, command.clipBounds().bottom())
        );
        if (right <= left || bottom <= top) return;
        context.graphics().flush();
        context.graphics().enableScissor(left, top, right, bottom);
        try {
            drawPayload(
                command,
                context,
                x,
                y,
                left,
                top,
                right - left,
                bottom - top,
                left,
                top,
                right - left,
                bottom - top,
                alpha,
                hostTooltipPose
            );
        } finally {
            try {
                context.graphics().flush();
            } finally {
                context.graphics().disableScissor();
            }
        }
    }

    private static void drawPayload(
        RenderCommand command,
        TipRenderContext context,
        int x,
        int y,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        int hostViewportX,
        int hostViewportY,
        int hostViewportWidth,
        int hostViewportHeight,
        float alpha,
        Matrix4f hostTooltipPose
    ) {
        switch (command.payload()) {
            case Visual2DCommandPayload payload -> {
                if (payload.draw() != null) {
                    renderPreparedDraw(
                        payload.draw(),
                        context,
                        x,
                        y,
                        viewportX,
                        viewportY,
                        viewportWidth,
                        viewportHeight,
                        alpha
                    );
                }
            }
            case ModelCommandPayload payload -> {
                if (payload.draw() != null) {
                    try {
                        renderPreparedDraw(
                            payload.draw(),
                            context,
                            x,
                            y,
                            viewportX,
                            viewportY,
                            viewportWidth,
                            viewportHeight,
                            alpha
                        );
                    } finally {
                        finishDepthIsolatedCommand(
                            command,
                            context,
                            hostTooltipPose,
                            hostViewportX,
                            hostViewportY,
                            hostViewportWidth,
                            hostViewportHeight
                        );
                    }
                }
            }
            case OverlayCommandPayload payload -> {
                if (payload.draw() != null) {
                    renderPreparedDraw(
                        payload.draw(),
                        context,
                        x,
                        y,
                        viewportX,
                        viewportY,
                        viewportWidth,
                        viewportHeight,
                        alpha
                    );
                }
            }
            case TextCommandPayload ignored -> {
            }
        }
    }

    /**
     * 用原版 Tooltip 的宿主平面覆盖当前模型留下的局部三维深度。
     * 该屏障只写深度，不清理共享缓冲，也不改变已经完成的颜色结果。
     */
    private static void finishDepthIsolatedCommand(
        RenderCommand command,
        TipRenderContext context,
        Matrix4f hostTooltipPose,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight
    ) {
        if (command.phase() != RenderPhase.ISOLATED_MODEL) return;
        restoreHostTooltipDepth(
            context,
            hostTooltipPose,
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight
        );
    }

    private static void restoreHostTooltipDepth(
        TipRenderContext context,
        Matrix4f hostTooltipPose,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight
    ) {
        int right = addCoordinate(viewportX, viewportWidth);
        int bottom = addCoordinate(viewportY, viewportHeight);
        if (right <= viewportX || bottom <= viewportY) return;

        context.graphics().flush();
        BufferBuilder buffer = Tesselator.getInstance().begin(
            VertexFormat.Mode.QUADS,
            DefaultVertexFormat.POSITION_COLOR
        );
        buffer.addVertex(hostTooltipPose, viewportX, viewportY, 0)
            .setColor(0xFFFFFFFF);
        buffer.addVertex(hostTooltipPose, viewportX, bottom, 0)
            .setColor(0xFFFFFFFF);
        buffer.addVertex(hostTooltipPose, right, bottom, 0)
            .setColor(0xFFFFFFFF);
        buffer.addVertex(hostTooltipPose, right, viewportY, 0)
            .setColor(0xFFFFFFFF);

        // 用宿主 Tooltip 平面覆盖模型深度，不触碰屏幕上其他 GUI 的深度。
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.colorMask(false, false, false, false);
        try {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        } finally {
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            RenderSystem.disableDepthTest();
        }
    }

    private static void renderPreparedDraw(
        PreparedImageDraw draw,
        TipRenderContext context,
        int x,
        int y,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha
    ) {
        if (draw instanceof PreparedViewportDraw viewportDraw) {
            viewportDraw.render(
                context,
                x,
                y,
                alpha,
                viewportX,
                viewportY,
                viewportWidth,
                viewportHeight
            );
        } else {
            draw.render(context, x, y, alpha);
        }
    }

    private static int addCoordinate(int origin, long relative) {
        long result = TipMath.add(origin, relative);
        return (int) Math.max(
            Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, result)
        );
    }
}
