package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.PreparedContent;
import com.cooobird.datatip.api.layout.PreparedLayout;
import com.cooobird.datatip.api.layout.TipPrepareContext;
import com.cooobird.datatip.internal.layout.PreparedTextLayout;
import com.cooobird.datatip.internal.text.CyclingTextColor;
import com.cooobird.datatip.internal.text.FormattedTextStyles;
import net.minecraft.network.chat.FormattedText;

import java.util.List;
import java.util.Objects;

/**
 * 按调色板让整段文本随时间循环变色。
 */
public final class CyclingTextContent implements com.cooobird.datatip.api.TipContent, PreparedContent {
    public enum Transition {
        SMOOTH,
        STEP
    }

    private final TextContent text;
    private final List<Integer> colors;
    private final double cycleSeconds;
    private final Transition transition;
    private final double phase;

    public CyclingTextContent(
        TextContent text,
        List<Integer> colors,
        double cycleSeconds,
        Transition transition,
        double phase
    ) {
        this.text = Objects.requireNonNull(text, "text");
        this.colors = List.copyOf(Objects.requireNonNull(colors, "colors"));
        if (this.colors.isEmpty()) {
            throw new IllegalArgumentException("Cycling text requires at least one color");
        }
        if (!Double.isFinite(cycleSeconds) || cycleSeconds <= 0.0) {
            throw new IllegalArgumentException("cycleSeconds must be a positive finite number");
        }
        if (!Double.isFinite(phase)) {
            throw new IllegalArgumentException("phase must be a finite number");
        }
        this.cycleSeconds = cycleSeconds;
        this.transition = Objects.requireNonNull(transition, "transition");
        this.phase = phase;
    }

    public TextContent textContent() {
        return text;
    }

    public List<Integer> colors() {
        return colors;
    }

    public double cycleSeconds() {
        return cycleSeconds;
    }

    public Transition transition() {
        return transition;
    }

    public double phase() {
        return phase;
    }

    @Override
    public int getHeight(int maxWidth) {
        return text.getHeight(maxWidth);
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        return text.getHeight(context);
    }

    @Override
    public int getWidth(int maxWidth) {
        return text.getWidth(maxWidth);
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        return text.getWidth(context);
    }

    @Override
    public boolean hasContent() {
        return text.hasContent();
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    public void tick(int tickCount) {
        // 颜色直接由渲染时间计算，不保留逐帧可变状态。
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        FormattedText formatted = FormattedTextStyles.withoutColor(
            text.formattedText(context.itemStack())
        );
        TextContent current = copyWith(formatted, color(context));
        current.render(context, x, y, maxWidth, alpha);
    }

    @Override
    public PreparedLayout prepare(TipPrepareContext context) {
        var stack = context.requireLayoutContext().itemStack();
        FormattedText formatted = FormattedTextStyles.withoutColor(
            text.formattedText(stack)
        );
        return PreparedTextLayout.prepareDynamic(
            text,
            context,
            formatted,
            this::color
        );
    }

    private int color(TipRenderContext context) {
        double seconds = (context.tickCount() + context.partialTick()) / 20.0;
        double progress = phase + seconds / cycleSeconds;
        return CyclingTextColor.at(
            colors,
            progress,
            transition == Transition.SMOOTH
        );
    }

    private TextContent copyWith(FormattedText formatted, int color) {
        return new TextContent(
            null,
            null,
            formatted,
            null,
            null,
            text.font(),
            color,
            null,
            text.shadow(),
            text.align(),
            text.lineHeight(),
            text.maxWidth(),
            text.bold(),
            text.italic(),
            text.underlined(),
            text.strikethrough(),
            false
        );
    }
}

