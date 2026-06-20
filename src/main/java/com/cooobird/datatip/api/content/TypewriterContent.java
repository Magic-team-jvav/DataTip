package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.config.DatatipConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 打字机效果内容。
 * 逐字显示文本。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TypewriterContent implements TipContent {

    private final List<String> lines;
    private final int charsPerSecond;
    private final int pauseSeconds;
    private final boolean loop;
    private final int color;
    @Nullable
    private final ResourceLocation font;

    private int currentLine;
    private int currentChar;
    private int tickCount;
    private int pauseCounter;
    private boolean completed;

    public TypewriterContent(List<String> lines, int charsPerSecond, int pauseSeconds, boolean loop, int color) {
        this(lines, charsPerSecond, pauseSeconds, loop, color, null);
    }

    public TypewriterContent(List<String> lines, int charsPerSecond, int pauseSeconds, boolean loop, int color, @Nullable ResourceLocation font) {
        this.lines = new ArrayList<>(lines);
        this.charsPerSecond = Math.max(1, charsPerSecond);
        this.pauseSeconds = Math.max(0, pauseSeconds);
        this.loop = loop;
        this.color = color;
        this.font = font;
        this.currentLine = 0;
        this.currentChar = 0;
        this.tickCount = 0;
        this.pauseCounter = 0;
        this.completed = false;
    }

    public static TypewriterContent create() {
        return new TypewriterContent(List.of(), 2, 1, false, DatatipConfig.DEFAULT_COLOR.get());
    }

    public static TypewriterContent of(String... lines) {
        return new TypewriterContent(List.of(lines), 2, 1, false, DatatipConfig.DEFAULT_COLOR.get());
    }

    public static TypewriterContent of(int color, String... lines) {
        return new TypewriterContent(List.of(lines), 2, 20, false, color);
    }

    public TypewriterContent addLine(String line) {
        lines.add(line);
        return this;
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

    public int color() {
        return color;
    }

    @Nullable
    public ResourceLocation font() {
        return font;
    }

    @Override
    public int getHeight(int maxWidth) {
        return lines.size() * 12;
    }

    @Override
    public int getWidth(int maxWidth) {
        Font font = Minecraft.getInstance().font;
        int maxLineWidth = 0;
        for (String line : lines) {
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
        if (completed && !loop) return;

        this.tickCount++;

        if (pauseCounter > 0) {
            pauseCounter--;
            return;
        }

        int ticksPerChar = Math.max(1, 20 / charsPerSecond);
        if (this.tickCount % ticksPerChar == 0) {
            if (currentLine < lines.size()) {
                String currentLineText = lines.get(currentLine);

                if (currentChar < currentLineText.length()) {
                    currentChar++;
                } else {
                    currentLine++;
                    currentChar = 0;

                    if (currentLine >= lines.size()) {
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
        if (alpha <= 0 || lines.isEmpty()) return;

        int renderY = y;

        for (int i = 0; i <= currentLine && i < lines.size(); i++) {
            String line = lines.get(i);
            String displayText;

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

            context.drawString(displayText, x, renderY, color, this.font);
            renderY += 12;
        }
    }
}
