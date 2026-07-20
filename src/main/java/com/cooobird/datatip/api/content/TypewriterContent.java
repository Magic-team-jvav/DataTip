package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.session.TooltipInvalidation;
import com.cooobird.datatip.api.session.TooltipSession;
import com.cooobird.datatip.api.session.TooltipSessionContext;
import com.cooobird.datatip.internal.text.FormattedTextLineBreaks;
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
public class TypewriterContent extends BaseTextContent
    implements com.cooobird.datatip.api.layout.PreparedContent {

    final List<String> lines;
    @Nullable
    final Map<String, List<String>> langLines;
    @Nullable
    final Map<String, List<LangStyle>> langStyledLines;
    final int charsPerSecond;
    final int pauseSeconds;
    final boolean loop;
    private final TypewriterState fallbackState;

    public TypewriterContent(List<String> lines, int charsPerSecond, int pauseSeconds, boolean loop, int color) {
        this(lines, null, null, charsPerSecond, pauseSeconds, loop, color, null, null, false, false, false, false, TextAlign.LEFT, true, TextContentDefaults.lineHeight(), false);
    }

    public TypewriterContent(List<String> lines, int charsPerSecond, int pauseSeconds, boolean loop, int color, @Nullable ResourceLocation font) {
        this(lines, null, null, charsPerSecond, pauseSeconds, loop, color, null, font, false, false, false, false, TextAlign.LEFT, true, TextContentDefaults.lineHeight(), false);
    }

    public TypewriterContent(@Nullable List<String> lines, @Nullable Map<String, List<String>> langLines,
                             @Nullable Map<String, List<LangStyle>> langStyledLines,
                             int charsPerSecond, int pauseSeconds, boolean loop, int color,
                             @Nullable String colorExpression, @Nullable ResourceLocation font,
                             boolean bold, boolean italic, boolean underlined, boolean strikethrough,
                             TextAlign align, boolean shadow, int lineHeight, boolean shift) {
        super(font, color, colorExpression, shadow, align, lineHeight, bold, italic, underlined, strikethrough, shift);
        this.lines = copyLines(lines);
        this.langLines = copyTextLineMap(langLines);
        this.langStyledLines = copyStyledLineMap(langStyledLines);
        this.charsPerSecond = ContentBounds.dimension(charsPerSecond);
        this.pauseSeconds = ContentBounds.spacing(pauseSeconds);
        this.loop = loop;
        this.fallbackState = new TypewriterState();
    }

    public static TypewriterContent create() {
        return new TypewriterContent(List.of(), 2, 1, false, TextContentDefaults.color());
    }

    public static TypewriterContent of(String... lines) {
        return new TypewriterContent(List.of(lines), 2, 1, false, TextContentDefaults.color());
    }

    public static TypewriterContent of(int color, String... lines) {
        return new TypewriterContent(List.of(lines), 2, 1, false, color);
    }

    public List<String> getLines() {
        return List.copyOf(lines);
    }

    public int getCharsPerSecond() {
        return charsPerSecond;
    }

    public int getPauseSeconds() {
        return pauseSeconds;
    }

    @Nullable
    public Map<String, List<String>> getLangLines() {
        return langLines;
    }

    @Nullable
    public Map<String, List<LangStyle>> getLangStyledLines() {
        return langStyledLines;
    }

    public boolean isLoop() {
        return loop;
    }

    @Override
    public int getHeight(int maxWidth) {
        return TypewriterLayout.getHeight(this);
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        return TypewriterLayout.getHeight(this, context);
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
    public int getWidth(TipLayoutContext context) {
        return TypewriterLayout.getWidth(this, context);
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    public void tick(int tickPc) {
        state().tick(this, tickPc);
    }

    public void reset() {
        state().reset();
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        TypewriterRenderer.render(this, state(), context, x, y, maxWidth, alpha);
    }

    @Nullable
    private static Map<String, List<String>> copyTextLineMap(
        @Nullable Map<String, List<String>> source
    ) {
        if (source == null) return null;
        Map<String, List<String>> copy = new HashMap<>();
        source.forEach((language, values) -> {
            if (language == null || values == null) return;
            copy.put(language, copyLines(values));
        });
        return Map.copyOf(copy);
    }

    @Nullable
    private static Map<String, List<LangStyle>> copyStyledLineMap(
        @Nullable Map<String, List<LangStyle>> source
    ) {
        if (source == null) return null;
        Map<String, List<LangStyle>> copy = new HashMap<>();
        source.forEach((language, values) -> {
            if (language == null || values == null) return;
            ArrayList<LangStyle> lines = new ArrayList<>();
            for (LangStyle value : values) {
                if (value == null) continue;
                for (String line : splitLines(value.text())) {
                    lines.add(new LangStyle(
                        line,
                        value.color(),
                        value.bold(),
                        value.italic(),
                        value.underlined(),
                        value.strikethrough(),
                        value.align(),
                        value.shift()
                    ));
                }
            }
            copy.put(language, List.copyOf(lines));
        });
        return Map.copyOf(copy);
    }

    private static List<String> copyLines(@Nullable List<String> source) {
        if (source == null) return List.of();
        List<String> copy = new ArrayList<>();
        for (String value : source) {
            if (value != null) copy.addAll(splitLines(value));
        }
        return List.copyOf(copy);
    }

    private static List<String> splitLines(String value) {
        return List.of(FormattedTextLineBreaks.decode(value).split("\n", -1));
    }

    @Override
    public com.cooobird.datatip.api.layout.PreparedLayout prepare(
        com.cooobird.datatip.api.layout.TipPrepareContext context
    ) {
        return PreparedTypewriterLayout.prepare(this, context);
    }

    TypewriterState state() {
        TooltipSession session = TooltipSessionContext.current();
        if (session == null) return fallbackState;
        TypewriterState state = session.cached(
            TooltipInvalidation.ANIMATION,
            this,
            TypewriterState.class
        );
        if (state != null) return state;
        TypewriterState created = new TypewriterState();
        session.cache(TooltipInvalidation.ANIMATION, this, created);
        return created;
    }
}
