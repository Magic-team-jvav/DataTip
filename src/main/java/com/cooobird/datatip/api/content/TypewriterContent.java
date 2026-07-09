package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打字机效果内容。
 * 逐字显示文本，支持与 TextContent 相同的样式和多语言。
 *
 * @author cooobird
 * @see BaseTextContent 基类
 * @since 1.2.0
 */
public class TypewriterContent extends BaseTextContent {

    final List<String> lines;
    @Nullable
    final Map<String, List<String>> langLines;
    @Nullable
    final Map<String, List<LangStyle>> langStyledLines;
    final int charsPerSecond;
    final int pauseSeconds;
    final boolean loop;
    private final TypewriterState state;

    public TypewriterContent(List<String> lines, int charsPerSecond, int pauseSeconds, boolean loop, int color) {
        this(lines, null, null, charsPerSecond, pauseSeconds, loop, color, null, null, false, false, false, false, TextAlign.LEFT, true, TextContentDefaults.lineHeight(), false);
    }

    public TypewriterContent(List<String> lines, int charsPerSecond, int pauseSeconds, boolean loop, int color, @Nullable ResourceLocation font) {
        this(lines, null, null, charsPerSecond, pauseSeconds, loop, color, null, font, false, false, false, false, TextAlign.LEFT, true, TextContentDefaults.lineHeight(), false);
    }

    public TypewriterContent(List<String> lines, @Nullable Map<String, List<String>> langLines,
                             @Nullable Map<String, List<LangStyle>> langStyledLines,
                             int charsPerSecond, int pauseSeconds, boolean loop, int color,
                             @Nullable String colorExpression, @Nullable ResourceLocation font,
                             boolean bold, boolean italic, boolean underlined, boolean strikethrough,
                             TextAlign align, boolean shadow, int lineHeight, boolean shift) {
        super(font, color, colorExpression, shadow, align, lineHeight, bold, italic, underlined, strikethrough, shift);
        this.lines = new ArrayList<>(lines);
        this.langLines = langLines != null ? new HashMap<>(langLines) : null;
        this.langStyledLines = langStyledLines != null ? new HashMap<>(langStyledLines) : null;
        this.charsPerSecond = Math.max(1, charsPerSecond);
        this.pauseSeconds = Math.max(0, pauseSeconds);
        this.loop = loop;
        this.state = new TypewriterState();
    }

    public static TypewriterContent create() {
        return new TypewriterContent(List.of(), 2, 1, false, TextContentDefaults.color());
    }

    public static TypewriterContent of(String... lines) {
        return new TypewriterContent(List.of(lines), 2, 1, false, TextContentDefaults.color());
    }

    public static TypewriterContent of(int color, String... lines) {
        return new TypewriterContent(List.of(lines), 2, 20, false, color);
    }

    public List<String> getLines() {
        return lines;
    }

    public int getCharsPerSecond() {
        return charsPerSecond;
    }

    public boolean isLoop() {
        return loop;
    }

    @Override
    public int getHeight(int maxWidth) {
        return TypewriterLayout.getHeight(this);
    }

    @Override
    public boolean hasContent() {
        return TypewriterLayout.hasContent(this);
    }

    @Override
    public int getWidth(int maxWidth) {
        return TypewriterLayout.getWidth(this, maxWidth);
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    public void tick(int tickPc) {
        state.tick(this, tickPc);
    }

    public void reset() {
        state.reset();
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        TypewriterRenderer.render(this, state, context, x, y, maxWidth, alpha);
    }
}
