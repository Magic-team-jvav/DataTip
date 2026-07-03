package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;

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

    private final List<TipContent> frames;
    private final int intervalTicks;
    private final TransitionType transition;

    public enum TransitionType {NONE, FADE, SLIDE}

    public CarouselContent(List<TipContent> frames, int intervalSeconds, TransitionType transition) {
        this.frames = new ArrayList<>(frames);
        this.intervalTicks = Math.max(1, intervalSeconds) * 20; // 20 ticks/秒
        this.transition = transition;
    }

    public static CarouselContent create() {
        return new CarouselContent(List.of(), 3, TransitionType.FADE);
    }

    public static CarouselContent withInterval(int intervalSeconds) {
        return new CarouselContent(List.of(), intervalSeconds, TransitionType.FADE);
    }

    public CarouselContent addFrame(TipContent frame) {
        frames.add(frame);
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
        if (frames.isEmpty()) return 0;
        int maxHeight = 0;
        for (TipContent frame : frames) {
            maxHeight = Math.max(maxHeight, frame.getHeight(maxWidth));
        }
        if (frames.size() > 1) maxHeight += 12; // 页码
        return maxHeight;
    }

    @Override
    public int getWidth(int maxWidth) {
        if (frames.isEmpty()) return 0;
        int maxChildWidth = 0;
        for (TipContent frame : frames) {
            maxChildWidth = Math.max(maxChildWidth, frame.getWidth(maxWidth));
        }
        return Math.min(maxChildWidth, maxWidth);
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (frames.isEmpty() || alpha <= 0) return;

        int tickCount = context.tickCount();

        // 计算当前帧
        int currentIndex = (tickCount / intervalTicks) % frames.size();

        // 直接渲染当前帧
        frames.get(currentIndex).render(context, x, y, maxWidth, alpha);

        // 渲染页码
        if (frames.size() > 1) {
            String pageText = (currentIndex + 1) + " / " + frames.size();
            int pageWidth = context.getStringWidth(pageText);
            context.drawString(pageText, x + (maxWidth - pageWidth) / 2, y + getHeight(maxWidth) - 12, 0x888888);
        }
    }
}
