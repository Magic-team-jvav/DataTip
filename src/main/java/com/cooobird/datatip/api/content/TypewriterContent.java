package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.config.DatatipConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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

    private final List<String> lines;
    @Nullable
    private final Map<String, List<String>> langLines;
    @Nullable
    private final Map<String, List<LangStyle>> langStyledLines;
    private final int charsPerSecond;
    private final int pauseSeconds;
    private final boolean loop;

    private int currentLine;
    private int currentChar;
    private int tickCount;
    private int pauseCounter;
    private boolean completed;

    public TypewriterContent(List<String> lines, int charsPerSecond, int pauseSeconds, boolean loop, int color) {
        this(lines, null, null, charsPerSecond, pauseSeconds, loop, color, null, null, false, false, false, false, TextAlign.LEFT, true, 12, false);
    }

    public TypewriterContent(List<String> lines, int charsPerSecond, int pauseSeconds, boolean loop, int color, @Nullable ResourceLocation font) {
        this(lines, null, null, charsPerSecond, pauseSeconds, loop, color, null, font, false, false, false, false, TextAlign.LEFT, true, 12, false);
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
        this.currentLine = 0;
        this.currentChar = 0;
        this.tickCount = 0;
        this.pauseCounter = 0;
        this.completed = false;
    }

    private static final int FALLBACK_COLOR = 0xFFAAAAAA;

    private static int getDefaultColor() {
        try {
            return DatatipConfig.DEFAULT_COLOR.get();
        } catch (IllegalStateException e) {
            return FALLBACK_COLOR;
        }
    }

    public static TypewriterContent create() {
        return new TypewriterContent(List.of(), 2, 1, false, getDefaultColor());
    }

    public static TypewriterContent of(String... lines) {
        return new TypewriterContent(List.of(lines), 2, 1, false, getDefaultColor());
    }

    public static TypewriterContent of(int color, String... lines) {
        return new TypewriterContent(List.of(lines), 2, 20, false, color);
    }

    private List<String> getCurrentLines() {
        if (langStyledLines != null && !langStyledLines.isEmpty()) {
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            List<LangStyle> styledLines = langStyledLines.get(lang);
            if (styledLines != null) {
                List<String> result = new ArrayList<>();
                for (LangStyle ls : styledLines) result.add(ls.text());
                return result;
            }
            return List.of();
        }
        if (langLines != null && !langLines.isEmpty()) {
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            List<String> langLinesList = langLines.get(lang);
            if (langLinesList != null) return langLinesList;
            return List.of();
        }
        return lines;
    }

    @Nullable
    private LangStyle getCurrentLineStyle(int lineIndex) {
        if (langStyledLines != null && !langStyledLines.isEmpty()) {
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            List<LangStyle> styledLines = langStyledLines.get(lang);
            if (styledLines != null && lineIndex < styledLines.size()) {
                return styledLines.get(lineIndex);
            }
        }
        return null;
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
        return getCurrentLines().size() * lineHeight;
    }

    @Override
    public int getWidth(int maxWidth) {
        List<String> currentLines = getCurrentLines();
        if (currentLines.isEmpty()) return 0;
        Font font = Minecraft.getInstance().font;
        int maxLineWidth = 0;
        for (String line : currentLines) {
            maxLineWidth = Math.max(maxLineWidth, font.width(line));
        }
        return Math.min(maxLineWidth, maxWidth);
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    public void tick(int tickCount) {
        List<String> currentLines = getCurrentLines();
        if (currentLines.isEmpty()) return;
        if (completed && !loop) return;

        this.tickCount++;

        if (pauseCounter > 0) {
            pauseCounter--;
            return;
        }

        int ticksPerChar = Math.max(1, 20 / charsPerSecond);
        if (this.tickCount % ticksPerChar == 0) {
            if (currentLine < currentLines.size()) {
                String currentLineText = currentLines.get(currentLine);
                if (currentChar < currentLineText.length()) {
                    currentChar++;
                } else {
                    currentLine++;
                    currentChar = 0;
                    if (currentLine >= currentLines.size()) {
                        if (loop) {
                            currentLine = 0;
                            pauseCounter = pauseSeconds * 20;
                        } else {
                            completed = true;
                        }
                    } else {
                        pauseCounter = pauseSeconds * 20;
                    }
                }
            }
        }
    }

    @Override
    public void onShow() {
        reset();
    }

    public void reset() {
        currentLine = 0;
        currentChar = 0;
        tickCount = 0;
        pauseCounter = 0;
        completed = false;
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        List<String> currentLines = getCurrentLines();
        if (alpha <= 0 || currentLines.isEmpty()) return;

        if (shift && !isShowTipDown()) {
            renderShiftHint(context, x, y);
            return;
        }

        int resolvedColor = resolveColor(context);
        Font mcFont = context.font();
        int renderY = y;

        for (int i = 0; i <= currentLine && i < currentLines.size(); i++) {
            String line = currentLines.get(i);
            String displayText;
            int lineColor = resolvedColor;
            boolean lineBold = bold;
            boolean lineItalic = italic;
            boolean lineUnderlined = underlined;
            boolean lineStrikethrough = strikethrough;

            LangStyle lineStyle = getCurrentLineStyle(i);
            if (lineStyle != null) {
                lineColor = lineStyle.color();
                lineBold = lineStyle.bold();
                lineItalic = lineStyle.italic();
                lineUnderlined = lineStyle.underlined();
                lineStrikethrough = lineStyle.strikethrough();
            }

            if (i < currentLine) {
                displayText = line;
            } else if (i == currentLine) {
                displayText = line.substring(0, Math.min(currentChar, line.length()));
                if (!completed && tickCount % 20 < 10) {
                    displayText += "▌";
                }
            } else {
                break;
            }

            Style style = buildStyle(lineColor);
            if (lineBold != bold) style = lineBold ? style.withBold(true) : style.withBold(false);
            if (lineItalic != italic) style = lineItalic ? style.withItalic(true) : style.withItalic(false);
            if (lineUnderlined != underlined)
                style = lineUnderlined ? style.withUnderlined(true) : style.withUnderlined(false);
            if (lineStrikethrough != strikethrough)
                style = lineStrikethrough ? style.withStrikethrough(true) : style.withStrikethrough(false);

            int lineX = calcLineX(mcFont, displayText, x, maxWidth);
            context.graphics().drawString(mcFont, Component.literal(displayText).withStyle(style), lineX, renderY, lineColor, shadow);
            renderY += lineHeight;
        }
    }
}
