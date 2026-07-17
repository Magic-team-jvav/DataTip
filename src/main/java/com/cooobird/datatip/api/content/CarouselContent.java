package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.*;
import com.cooobird.datatip.api.render.*;
import com.cooobird.datatip.api.session.TooltipInvalidation;
import com.cooobird.datatip.api.session.TooltipSession;
import com.cooobird.datatip.api.session.TooltipSessionContext;
import com.cooobird.datatip.internal.render.PreparedPlanRenderer;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.*;

/**
 * 轮播容器内容。
 * 自动切换显示子内容。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class CarouselContent implements TipContent, PreparedContent {
    private final List<TipContent> frames;
    private final long intervalTicks;
    private final TransitionType transition;
    private volatile List<TipContent> expandedFrames;
    private volatile List<TipContent> collapsedFrames;

    public enum TransitionType {NONE, FADE, SLIDE}

    public CarouselContent(List<TipContent> frames, int intervalSeconds, TransitionType transition) {
        this.frames = new ArrayList<>(List.copyOf(frames != null ? frames : List.of()));
        this.intervalTicks = Math.max(1L, intervalSeconds) * 20L;
        this.transition = transition != null ? transition : TransitionType.FADE;
    }

    public static CarouselContent create() {
        return new CarouselContent(List.of(), 3, TransitionType.FADE);
    }

    public static CarouselContent withInterval(int intervalSeconds) {
        return new CarouselContent(List.of(), intervalSeconds, TransitionType.FADE);
    }

    public CarouselContent addFrame(TipContent frame) {
        frames.add(java.util.Objects.requireNonNull(frame, "frame"));
        expandedFrames = null;
        collapsedFrames = null;
        return this;
    }

    public List<TipContent> getFrames() {
        return Collections.unmodifiableList(frames);
    }

    public TransitionType getTransition() {
        return transition;
    }

    // 获取轮播间隔
    public int getIntervalSeconds() {
        return (int) Math.min(Integer.MAX_VALUE, intervalTicks / 20L);
    }

    @Override
    public int getHeight(int maxWidth) {
        return getHeight(legacyContext(maxWidth));
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        List<TipContent> visible = visibleFrames();
        if (visible.isEmpty()) return 0;
        int maxHeight = 0;
        for (TipContent frame : visible) {
            maxHeight = Math.max(maxHeight, frame.getHeight(context));
        }
        if (visible.size() > 1) maxHeight += 12; // 页码
        return maxHeight;
    }

    @Override
    public int getWidth(int maxWidth) {
        return getWidth(legacyContext(maxWidth));
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        List<TipContent> visible = visibleFrames();
        if (visible.isEmpty()) return 0;
        int maxChildWidth = 0;
        for (TipContent frame : visible) {
            maxChildWidth = Math.max(maxChildWidth, frame.getWidth(context));
        }
        return context.constrainWidth(maxChildWidth);
    }

    @Override
    public boolean isAnimated() {
        return TipAnimationTraversal.isAnimated(this);
    }

    @Override
    public void tick(int tickCount) {
        TipAnimationTraversal.tick(this, tickCount);
    }

    void enqueueTickTargets(
        int tickCount,
        java.util.ArrayDeque<TipContent> destination
    ) {
        List<TipContent> visible = visibleFrames();
        if (visible.isEmpty()) return;
        long elapsedTicks = elapsedTicks(tickCount);
        int currentIndex = (int) (
            (elapsedTicks / intervalTicks) % visible.size()
        );
        int nextIndex = (currentIndex + 1) % visible.size();
        destination.push(visible.get(currentIndex));
        if (nextIndex != currentIndex) {
            destination.push(visible.get(nextIndex));
        }
    }

    @Override
    public PreparedLayout prepare(TipPrepareContext context) {
        return PreparedContainerSupport.prepareTree(this, context);
    }

    PreparedLayout prepareDirect(TipPrepareContext context) {
        List<TipContent> visible = visibleFrames();
        if (visible.isEmpty()) {
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

        ArrayList<PreparedLayout> preparedFrames = new ArrayList<>(visible.size());
        long naturalWidth = 0;
        long naturalHeight = 0;
        long preferredWidth = 0;
        long preferredHeight = 0;
        long allocatedWidth = 0;
        long allocatedHeight = 0;
        for (TipContent frame : visible) {
            PreparedLayout prepared = frame.prepare(context);
            if (prepared.allocatedBounds().width() == 0
                && prepared.allocatedBounds().height() == 0) {
                continue;
            }
            preparedFrames.add(prepared);
            naturalWidth = Math.max(
                naturalWidth,
                prepared.naturalBounds().width()
            );
            naturalHeight = Math.max(
                naturalHeight,
                prepared.naturalBounds().height()
            );
            preferredWidth = Math.max(
                preferredWidth,
                prepared.preferredSize().width()
            );
            preferredHeight = Math.max(
                preferredHeight,
                prepared.preferredSize().height()
            );
            allocatedWidth = Math.max(
                allocatedWidth,
                prepared.allocatedBounds().width()
            );
            allocatedHeight = Math.max(
                allocatedHeight,
                prepared.allocatedBounds().height()
            );
        }
        if (preparedFrames.size() == 1) {
            return preparedFrames.get(0);
        }

        int pageHeight = preparedFrames.size() > 1 ? 12 : 0;
        ArrayList<String> pageTexts = new ArrayList<>(preparedFrames.size());
        ArrayList<Integer> pageWidths = new ArrayList<>(preparedFrames.size());
        if (pageHeight > 0) {
            var font = context.requireLayoutContext().font();
            for (int index = 0; index < preparedFrames.size(); index++) {
                String pageText = (index + 1) + " / " + preparedFrames.size();
                int pageWidth = font.width(pageText);
                pageTexts.add(pageText);
                pageWidths.add(pageWidth);
                naturalWidth = Math.max(naturalWidth, pageWidth);
                preferredWidth = Math.max(preferredWidth, pageWidth);
                allocatedWidth = Math.max(allocatedWidth, pageWidth);
            }
        }

        naturalHeight = TipMath.add(naturalHeight, pageHeight);
        preferredHeight = TipMath.add(preferredHeight, pageHeight);
        allocatedHeight = TipMath.add(allocatedHeight, pageHeight);
        long frozenWidth = allocatedWidth;
        long frozenFrameHeight = Math.max(0, allocatedHeight - pageHeight);
        List<PreparedLayout> frozenFrames = List.copyOf(preparedFrames);
        List<String> frozenPageTexts = List.copyOf(pageTexts);
        List<Integer> frozenPageWidths = List.copyOf(pageWidths);
        PreparedFrameViewportCache viewportCache =
            new PreparedFrameViewportCache();

        TipRect bounds = new TipRect(
            0,
            0,
            allocatedWidth,
            allocatedHeight
        );
        RenderCommand command = RenderCommand.positioned(
            RenderPhase.OVERLAY,
            0,
            0,
            bounds,
            null,
            new OverlayCommandPayload(
                "carousel",
                new PreparedViewportDraw() {
                    @Override
                    public void render(
                        TipRenderContext renderContext,
                        int x,
                        int y,
                        float alpha,
                        int viewportX,
                        int viewportY,
                        int viewportWidth,
                        int viewportHeight
                    ) {
                        renderPrepared(
                            renderContext,
                            x,
                            y,
                            toDimension(frozenWidth),
                            toDimension(frozenFrameHeight),
                            alpha,
                            viewportX,
                            viewportY,
                            viewportWidth,
                            viewportHeight,
                            frozenFrames,
                            frozenPageTexts,
                            frozenPageWidths,
                            viewportCache
                        );
                    }
                }
            )
        );
        return PreparedLayout.create(
            new TipRect(0, 0, naturalWidth, naturalHeight),
            new TipSize(preferredWidth, preferredHeight),
            bounds,
            bounds,
            null,
            OverflowPolicy.WRAP,
            new RenderCommandPipeline(
                List.of(),
                RenderCommandGroup.root(List.of(command))
            )
        );
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        List<TipContent> visible = visibleFrames();
        if (visible.isEmpty() || alpha <= 0) return;

        long tickCount = elapsedTicks(context.tickCount());

        // 计算当前帧和切换进度。
        int currentIndex = (int) ((tickCount / intervalTicks) % visible.size());
        int nextIndex = (currentIndex + 1) % visible.size();
        float cycleProgress =
            (tickCount % intervalTicks + context.partialTick()) / intervalTicks;
        float transitionProgress = Math.max(0.0f, (cycleProgress - 0.8f) / 0.2f);
        int displayedIndex = currentIndex;

        if (visible.size() == 1 || transition == TransitionType.NONE || transitionProgress <= 0) {
            visible.get(currentIndex).render(context, x, y, maxWidth, alpha);
        } else if (transition == TransitionType.FADE) {
            if (transitionProgress < 0.5f) {
                float fadeOut = 1.0f - transitionProgress * 2.0f;
                renderWithAlpha(visible.get(currentIndex), context, x, y, maxWidth, alpha * fadeOut);
            } else {
                float fadeIn = (transitionProgress - 0.5f) * 2.0f;
                renderWithAlpha(visible.get(nextIndex), context, x, y, maxWidth, alpha * fadeIn);
                displayedIndex = nextIndex;
            }
        } else {
            renderSlide(visible, context, x, y, maxWidth, alpha, currentIndex, nextIndex, transitionProgress);
            if (transitionProgress >= 0.5f) displayedIndex = nextIndex;
        }

        // 渲染页码
        if (visible.size() > 1) {
            String pageText = (displayedIndex + 1) + " / " + visible.size();
            int pageWidth = context.getStringWidth(pageText);
            TipLayoutContext layout = TipLayoutContext.bounded(context.font(), context.itemStack(), maxWidth);
            context.drawString(
                pageText,
                coordinate(x, ((long) maxWidth - pageWidth) / 2),
                coordinate(y, (long) getHeight(layout) - 12),
                TipRenderContext.applyAlpha(0x888888, alpha)
            );
        }
    }

    private void renderSlide(
        List<TipContent> visible,
        TipRenderContext context,
        int x,
        int y,
        int maxWidth,
        float alpha,
        int currentIndex,
        int nextIndex,
        float progress
    ) {
        long offset = Math.round((double) maxWidth * progress);
        TipLayoutContext layout = TipLayoutContext.bounded(context.font(), context.itemStack(), maxWidth);
        int clipHeight = Math.max(1, getHeight(layout) - (visible.size() > 1 ? 12 : 0));
        // Tooltip 的原版文字与自定义内容共用延迟绘制缓冲。必须先提交原版文字，
        // 再开启轮播裁剪，否则缓冲区可能在裁剪生效期间提交，导致原版信息被短暂裁掉。
        context.graphics().flush();
        context.graphics().enableScissor(
            x,
            y,
            coordinate(x, maxWidth),
            coordinate(y, clipHeight)
        );
        try {
            visible.get(currentIndex).render(
                context,
                coordinate(x, -offset),
                y,
                maxWidth,
                alpha
            );
            visible.get(nextIndex).render(
                context,
                coordinate(x, (long) maxWidth - offset),
                y,
                maxWidth,
                alpha
            );
        } finally {
            // 在关闭裁剪前提交滑动帧，保证裁剪只作用于本次轮播内容。
            try {
                context.graphics().flush();
            } finally {
                context.graphics().disableScissor();
            }
        }
    }

    private static void renderWithAlpha(
        TipContent frame,
        TipRenderContext context,
        int x,
        int y,
        int maxWidth,
        float alpha
    ) {
        float normalizedAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
        if (normalizedAlpha <= 0) return;

        // Fade 需要覆盖纹理、模型、图表和文本。先提交原版 Tooltip 缓冲，再在局部
        // Shader 颜色作用域内绘制当前帧，最后恢复进入前的状态，避免影响后续原版内容。
        context.graphics().flush();
        float[] shaderColor = RenderSystem.getShaderColor().clone();
        RenderSystem.setShaderColor(
            shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3] * normalizedAlpha);
        try {
            frame.render(context, x, y, maxWidth, 1.0f);
        } finally {
            try {
                context.graphics().flush();
            } finally {
                RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
            }
        }
    }

    private static TipLayoutContext legacyContext(int maxWidth) {
        return maxWidth > 0
            ? TipLayoutContext.bounded(net.minecraft.client.Minecraft.getInstance().font,
            net.minecraft.world.item.ItemStack.EMPTY, maxWidth)
            : TipLayoutContext.unbounded(net.minecraft.client.Minecraft.getInstance().font,
            net.minecraft.world.item.ItemStack.EMPTY);
    }

    private void renderPrepared(
        TipRenderContext context,
        int x,
        int y,
        int width,
        int frameHeight,
        float alpha,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        List<PreparedLayout> preparedFrames,
        List<String> pageTexts,
        List<Integer> pageWidths,
        PreparedFrameViewportCache viewportCache
    ) {
        if (preparedFrames.isEmpty() || alpha <= 0.0f) return;
        int clipLeft = Math.max(x, viewportX);
        int clipTop = Math.max(y, viewportY);
        int clipRight = Math.min(
            coordinate(x, width),
            coordinate(viewportX, viewportWidth)
        );
        int clipBottom = Math.min(
            coordinate(y, frameHeight),
            coordinate(viewportY, viewportHeight)
        );
        if (clipRight <= clipLeft || clipBottom <= clipTop) return;

        long tickCount = elapsedTicks(context.tickCount());
        int currentIndex = (int) (
            (tickCount / intervalTicks) % preparedFrames.size()
        );
        int nextIndex = (currentIndex + 1) % preparedFrames.size();
        float cycleProgress =
            (tickCount % intervalTicks + context.partialTick()) / intervalTicks;
        float transitionProgress = Math.max(
            0.0f,
            (cycleProgress - 0.8f) / 0.2f
        );
        int displayedIndex = currentIndex;

        if (preparedFrames.size() == 1
            || transition == TransitionType.NONE
            || transitionProgress <= 0.0f) {
            renderPreparedFrame(
                preparedFrames.get(currentIndex),
                context,
                x,
                y,
                clipLeft,
                clipTop,
                clipRight - clipLeft,
                clipBottom - clipTop,
                alpha,
                viewportCache
            );
        } else if (transition == TransitionType.FADE) {
            if (transitionProgress < 0.5f) {
                renderPreparedFrame(
                    preparedFrames.get(currentIndex),
                    context,
                    x,
                    y,
                    clipLeft,
                    clipTop,
                    clipRight - clipLeft,
                    clipBottom - clipTop,
                    alpha * (1.0f - transitionProgress * 2.0f),
                    viewportCache
                );
            } else {
                renderPreparedFrame(
                    preparedFrames.get(nextIndex),
                    context,
                    x,
                    y,
                    clipLeft,
                    clipTop,
                    clipRight - clipLeft,
                    clipBottom - clipTop,
                    alpha * ((transitionProgress - 0.5f) * 2.0f),
                    viewportCache
                );
                displayedIndex = nextIndex;
            }
        } else {
            long offset = Math.round((double) width * transitionProgress);
            renderPreparedFrame(
                preparedFrames.get(currentIndex),
                context,
                coordinate(x, -offset),
                y,
                clipLeft,
                clipTop,
                clipRight - clipLeft,
                clipBottom - clipTop,
                alpha,
                viewportCache
            );
            renderPreparedFrame(
                preparedFrames.get(nextIndex),
                context,
                coordinate(x, (long) width - offset),
                y,
                clipLeft,
                clipTop,
                clipRight - clipLeft,
                clipBottom - clipTop,
                alpha,
                viewportCache
            );
            if (transitionProgress >= 0.5f) displayedIndex = nextIndex;
        }

        if (!pageTexts.isEmpty()) {
            String pageText = pageTexts.get(displayedIndex);
            int pageWidth = pageWidths.get(displayedIndex);
            context.graphics().drawString(
                context.font(),
                pageText,
                coordinate(x, ((long) width - pageWidth) / 2),
                coordinate(y, frameHeight),
                TipRenderContext.applyAlpha(0x888888, alpha),
                true
            );
        }
    }

    private static void renderPreparedFrame(
        PreparedLayout frame,
        TipRenderContext context,
        int originX,
        int originY,
        int viewportX,
        int viewportY,
        int viewportWidth,
        int viewportHeight,
        float alpha,
        PreparedFrameViewportCache viewportCache
    ) {
        TipRect localViewport = new TipRect(
            0,
            (long) viewportY - originY,
            frame.allocatedBounds().width(),
            viewportHeight
        );
        TipRect actualViewport = new TipRect(
            (long) viewportX - originX,
            (long) viewportY - originY,
            viewportWidth,
            viewportHeight
        );
        RenderCommandPipeline visiblePlan = viewportCache.planFor(
            frame,
            localViewport,
            actualViewport
        );
        PreparedPlanRenderer.renderPlan(
            visiblePlan,
            context,
            originX,
            originY,
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight,
            Math.max(0.0f, Math.min(1.0f, alpha)),
            true
        );
    }

    private static int toDimension(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value));
    }

    private static int coordinate(int origin, long offset) {
        long value = TipMath.add(origin, offset);
        return (int) Math.max(
            Integer.MIN_VALUE,
            Math.min(Integer.MAX_VALUE, value)
        );
    }

    List<TipContent> visibleFrames() {
        boolean expanded = isShiftExpanded();
        List<TipContent> cached = expanded ? expandedFrames : collapsedFrames;
        if (cached != null) return cached;

        ArrayList<TipContent> visible = new ArrayList<>(frames.size());
        for (TipContent frame : frames) {
            if (frame.hasContent() && !frame.isShiftCollapsed()) {
                visible.add(frame);
            }
        }
        List<TipContent> result = List.copyOf(visible);
        if (expanded) {
            expandedFrames = result;
        } else {
            collapsedFrames = result;
        }
        return result;
    }

    private static boolean isShiftExpanded() {
        try {
            return com.cooobird.datatip.event.TipRenderEventHandler
                .isShowTipDown();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    static final class PreparedFrameViewportCache {
        private final Map<PreparedLayout, CachedFramePlan> plans =
            new IdentityHashMap<>();

        RenderCommandPipeline planFor(
            PreparedLayout frame,
            TipRect verticalViewport,
            TipRect actualViewport
        ) {
            CachedFramePlan cached = plans.get(frame);
            RenderCommandPipeline verticalPlan;
            if (cached != null
                && cached.viewport().equals(verticalViewport)) {
                verticalPlan = cached.plan();
            } else {
                verticalPlan = frame.renderPlan().visibleWithin(
                    verticalViewport
                );
                plans.put(
                    frame,
                    new CachedFramePlan(verticalViewport, verticalPlan)
                );
            }
            return verticalViewport.equals(actualViewport)
                ? verticalPlan
                : verticalPlan.visibleWithin(actualViewport);
        }
    }

    private record CachedFramePlan(
        TipRect viewport,
        RenderCommandPipeline plan
    ) {
    }

    private long elapsedTicks(int currentTick) {
        long unsignedTick = Integer.toUnsignedLong(currentTick);
        TooltipSession session = TooltipSessionContext.current();
        if (session == null) return unsignedTick;
        CarouselClock clock = session.cached(
            TooltipInvalidation.ANIMATION,
            this,
            CarouselClock.class
        );
        if (clock == null) {
            clock = new CarouselClock(unsignedTick);
            session.cache(TooltipInvalidation.ANIMATION, this, clock);
        }
        return clock.elapsed(unsignedTick);
    }

    private record CarouselClock(long startedAt) {

        private long elapsed(long current) {
            return (current - startedAt) & 0xFFFFFFFFL;
        }
    }
}
