package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.expression.ExpressionParser;
import com.cooobird.datatip.event.TipRenderEventHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 文本内容基类。
 * <p>
 * 包含 TextContent 和 TypewriterContent 的公共样式属性和方法。
 * </p>
 *
 * @author cooobird
 * @since 1.2.0
 */
public abstract class BaseTextContent implements TipContent {

    // 多语言样式记录
    public record LangStyle(
        String text,
        int color,
        boolean bold,
        boolean italic,
        boolean underlined,
        boolean strikethrough,
        TextAlign align,
        boolean shift
    ) {
        public LangStyle(String text, int color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
            this(text, color, bold, italic, underlined, strikethrough, TextAlign.LEFT, false);
        }
    }

    // 文本对齐方式
    public enum TextAlign {
        LEFT,
        CENTER,
        RIGHT
    }

    protected final @Nullable ResourceLocation font;
    protected final int color;
    protected final @Nullable String colorExpression;
    protected final boolean shadow;
    protected final TextAlign align;
    protected final int lineHeight;
    protected final boolean bold;
    protected final boolean italic;
    protected final boolean underlined;
    protected final boolean strikethrough;
    protected final boolean shift;

    protected BaseTextContent(@Nullable ResourceLocation font, int color, @Nullable String colorExpression,
                              boolean shadow, TextAlign align, int lineHeight,
                              boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean shift) {
        this.font = font;
        this.color = color;
        this.colorExpression = colorExpression;
        this.shadow = shadow;
        this.align = align;
        this.lineHeight = lineHeight;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.shift = shift;
    }

    // Getter 方法
    public @Nullable ResourceLocation font() {
        return font;
    }

    public int color() {
        return color;
    }

    public @Nullable String colorExpression() {
        return colorExpression;
    }

    public boolean shadow() {
        return shadow;
    }

    public TextAlign align() {
        return align;
    }

    public int lineHeight() {
        return lineHeight;
    }

    public boolean bold() {
        return bold;
    }

    public boolean italic() {
        return italic;
    }

    public boolean underlined() {
        return underlined;
    }

    public boolean strikethrough() {
        return strikethrough;
    }

    public boolean shift() {
        return shift;
    }

    /**
     * 解析颜色（支持表达式）
     */
    protected int resolveColor(TipRenderContext context) {
        if (colorExpression == null || colorExpression.isEmpty()) return color;
        String resolved = context.resolveVariables(colorExpression);
        if (resolved == null || resolved.isEmpty()) return color;
        if (resolved.contains("?") || resolved.contains(">") || resolved.contains("<")) {
            try {
                Map<String, String> variables = new HashMap<>();
                Object result = ExpressionParser.evaluate(resolved, variables);
                if (result instanceof String s) return parseColorString(s, color);
            } catch (Exception e) { /* ignore */ }
        }
        return parseColorString(resolved, color);
    }

    /**
     * 构建样式
     */
    protected Style buildStyle() {
        Style style = Style.EMPTY.withColor(color);
        if (font != null) style = style.withFont(font);
        if (bold) style = style.withBold(true);
        if (italic) style = style.withItalic(true);
        if (underlined) style = style.withUnderlined(true);
        if (strikethrough) style = style.withStrikethrough(true);
        return style;
    }

    /**
     * 构建指定颜色的样式
     */
    protected Style buildStyle(int customColor) {
        Style style = Style.EMPTY.withColor(customColor);
        if (font != null) style = style.withFont(font);
        if (bold) style = style.withBold(true);
        if (italic) style = style.withItalic(true);
        if (underlined) style = style.withUnderlined(true);
        if (strikethrough) style = style.withStrikethrough(true);
        return style;
    }

    /**
     * 检查 Shift 键是否按下
     */
    protected static boolean isShowTipDown() {
        return Screen.hasShiftDown();
    }

    /**
     * 绘制 Shift 提示
     */
    protected void renderShiftHint(TipRenderContext context, int x, int y) {
        Component hint = Component.translatable("tooltip.datatip.hold_shift",
            TipRenderEventHandler.SHOW_TIP.getTranslatedKeyMessage());
        context.drawString(hint, x, y, 0x888888, true);
    }

    /**
     * 根据对齐方式计算 X 坐标
     */
    protected int calcLineX(Font font, String text, int x, int maxWidth) {
        return switch (align) {
            case LEFT -> x;
            case CENTER -> {
                int lineWidth = font.width(text);
                yield x + (maxWidth > 0 ? (maxWidth - lineWidth) / 2 : -lineWidth / 2);
            }
            case RIGHT -> {
                int lineWidth = font.width(text);
                yield x + (maxWidth > 0 ? maxWidth - lineWidth : -lineWidth);
            }
        };
    }

    /**
     * 根据对齐方式计算 X 坐标（FormattedCharSequence 版本）
     */
    protected int calcLineX(Font font, FormattedCharSequence text, int x, int maxWidth) {
        return switch (align) {
            case LEFT -> x;
            case CENTER -> {
                int lineWidth = font.width(text);
                yield x + (maxWidth > 0 ? (maxWidth - lineWidth) / 2 : -lineWidth / 2);
            }
            case RIGHT -> {
                int lineWidth = font.width(text);
                yield x + (maxWidth > 0 ? maxWidth - lineWidth : -lineWidth);
            }
        };
    }

    /**
     * 解析颜色字符串为 ARGB 颜色值
     */
    public static int parseColorString(String colorStr, int defaultValue) {
        if (colorStr == null || colorStr.isEmpty()) return defaultValue;
        return switch (colorStr.toLowerCase()) {
            case "black" -> 0xFF000000;
            case "dark_blue" -> 0xFF0000AA;
            case "dark_green" -> 0xFF00AA00;
            case "dark_aqua" -> 0xFF00AAAA;
            case "dark_red" -> 0xFFAA0000;
            case "dark_purple" -> 0xFFAA00AA;
            case "gold" -> 0xFFFFAA00;
            case "gray", "grey" -> 0xFFAAAAAA;
            case "dark_gray", "dark_grey" -> 0xFF555555;
            case "blue" -> 0xFF5555FF;
            case "green" -> 0xFF55FF55;
            case "aqua" -> 0xFF55FFFF;
            case "red" -> 0xFFFF5555;
            case "light_purple" -> 0xFFFF55FF;
            case "yellow" -> 0xFFFFFF55;
            case "white" -> 0xFFFFFFFF;
            default -> {
                if (colorStr.startsWith("#")) {
                    try {
                        yield (int) Long.parseLong(colorStr.substring(1), 16) | 0xFF000000;
                    } catch (NumberFormatException e) {
                        yield defaultValue;
                    }
                }
                yield defaultValue;
            }
        };
    }
}
