package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 轮播容器内容。
 * 自动切换显示子内容。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class CarouselContent implements TipContent {
    private static final int MAX_INTERVAL_SECONDS = 86_400;

    private final List<TipContent> frames;
    private final int intervalTicks;
    private final TransitionType transition;

    public enum TransitionType {NONE, FADE, SLIDE}

    public CarouselContent(List<TipContent> frames, int intervalSeconds, TransitionType transition) {
        this.frames = new ArrayList<>(List.copyOf(frames != null ? frames : List.of()));
        this.intervalTicks = Math.min(MAX_INTERVAL_SECONDS, Math.max(1, intervalSeconds)) * 20; // 20 ticks/秒
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
        return intervalTicks / 20;
    }

    @Override
    public int getHeight(int maxWidth) {
        return getHeight(legacyContext(maxWidth));
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        if (frames.isEmpty()) return 0;
        int maxHeight = 0;
        for (TipContent frame : frames) {
            maxHeight = Math.max(maxHeight, frame.getHeight(context));
        }
        if (frames.size() > 1) maxHeight += 12; // 页码
        return maxHeight;
    }

    @Override
    public int getWidth(int maxWidth) {
        return getWidth(legacyContext(maxWidth));
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        if (frames.isEmpty()) return 0;
        int maxChildWidth = 0;
        for (TipContent frame : frames) {
            maxChildWidth = Math.max(maxChildWidth, frame.getWidth(context));
        }
        return context.constrainWidth(maxChildWidth);
    }

    @Override
    public boolean isAnimated() {
        if (frames.size() > 1) return true;
        return frames.size() == 1 && frames.get(0).isAnimated();
    }

    @Override
    public void tick(int tickCount) {
        for (TipContent frame : frames) {
            if (frame.isAnimated()) {
                frame.tick(tickCount);
            }
        }
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (frames.isEmpty() || alpha <= 0) return;

        int tickCount = context.tickCount();

        // 计算当前帧和切换进度。
        int currentIndex = (tickCount / intervalTicks) % frames.size();
        int nextIndex = (currentIndex + 1) % frames.size();
        float cycleProgress = (tickCount % intervalTicks + context.partialTick()) / intervalTicks;
        float transitionProgress = Math.max(0.0f, (cycleProgress - 0.8f) / 0.2f);
        int displayedIndex = currentIndex;

        if (frames.size() == 1 || transition == TransitionType.NONE || transitionProgress <= 0) {
            frames.get(currentIndex).render(context, x, y, maxWidth, alpha);
        } else if (transition == TransitionType.FADE) {
            if (transitionProgress < 0.5f) {
                float fadeOut = 1.0f - transitionProgress * 2.0f;
                renderWithAlpha(frames.get(currentIndex), context, x, y, maxWidth, alpha * fadeOut);
            } else {
                float fadeIn = (transitionProgress - 0.5f) * 2.0f;
                renderWithAlpha(frames.get(nextIndex), context, x, y, maxWidth, alpha * fadeIn);
                displayedIndex = nextIndex;
            }
        } else {
            renderSlide(context, x, y, maxWidth, alpha, currentIndex, nextIndex, transitionProgress);
            if (transitionProgress >= 0.5f) displayedIndex = nextIndex;
        }

        // 渲染页码
        if (frames.size() > 1) {
            String pageText = (displayedIndex + 1) + " / " + frames.size();
            int pageWidth = context.getStringWidth(pageText);
            TipLayoutContext layout = TipLayoutContext.bounded(context.font(), context.itemStack(), maxWidth);
            context.drawString(pageText, x + (maxWidth - pageWidth) / 2,
                y + getHeight(layout) - 12, TipRenderContext.applyAlpha(0x888888, alpha));
        }
    }

    private void renderSlide(
        TipRenderContext context,
        int x,
        int y,
        int maxWidth,
        float alpha,
        int currentIndex,
        int nextIndex,
        float progress
    ) {
        int offset = Math.round(maxWidth * progress);
        TipLayoutContext layout = TipLayoutContext.bounded(context.font(), context.itemStack(), maxWidth);
        int clipHeight = Math.max(1, getHeight(layout) - (frames.size() > 1 ? 12 : 0));
        // Tooltip 的原版文字与自定义内容共用延迟绘制缓冲。必须先提交原版文字，
        // 再开启轮播裁剪，否则缓冲区可能在裁剪生效期间提交，导致原版信息被短暂裁掉。
        context.graphics().flush();
        context.graphics().enableScissor(x, y, x + maxWidth, y + clipHeight);
        try {
            frames.get(currentIndex).render(context, x - offset, y, maxWidth, alpha);
            frames.get(nextIndex).render(context, x + maxWidth - offset, y, maxWidth, alpha);
        } finally {
            // 在关闭裁剪前提交滑动帧，保证裁剪只作用于本次轮播内容。
            context.graphics().flush();
            context.graphics().disableScissor();
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
}
