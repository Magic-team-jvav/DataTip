package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.expression.ExpressionParser;
import com.cooobird.datatip.api.util.ColorParser;
import com.cooobird.datatip.client.DatatipKeyMappings;
import com.cooobird.datatip.config.DatatipConfig;
import com.cooobird.datatip.event.TipRenderEventHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
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
    private static final int FALLBACK_SHIFT_HINT_COLOR = 0xFF888888;

    /**
     * 多语言样式记录。
     */
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
        public LangStyle(
            String text,
            int color,
            boolean bold,
            boolean italic,
            boolean underlined,
            boolean strikethrough,
            TextAlign align,
            boolean shift
        ) {
            this.text = text != null ? text : "";
            this.color = color;
            this.bold = bold;
            this.italic = italic;
            this.underlined = underlined;
            this.strikethrough = strikethrough;
            this.align = align != null ? align : TextAlign.LEFT;
            this.shift = shift;
        }

        public LangStyle(String text, int color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
            this(text, color, bold, italic, underlined, strikethrough, TextAlign.LEFT, false);
        }
    }

    /**
     * 文本对齐方式。
     */
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
        this.align = align != null ? align : TextAlign.LEFT;
        this.lineHeight = Math.max(1, lineHeight);
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.shift = shift;
    }

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
     * 解析颜色，支持变量和简单表达式。
     */
    protected int resolveColor(TipRenderContext context) {
        return resolveColor(context.itemStack());
    }

    protected int resolveColor(ItemStack stack) {
        if (colorExpression == null || colorExpression.isEmpty()) return color;

        String resolved = stack != null && !stack.isEmpty()
            ? com.cooobird.datatip.api.util.VariableResolver.resolve(
            colorExpression,
            stack
        )
            : colorExpression;
        if (resolved == null || resolved.isEmpty()) return color;

        if (resolved.contains("?") || resolved.contains(">") || resolved.contains("<")) {
            try {
                Map<String, String> variables = new HashMap<>();
                Object result = ExpressionParser.evaluate(resolved, variables);
                if (result instanceof String s) return parseColorString(s, color);
            } catch (Exception ignored) {
                // 表达式解析失败时，明确回退到普通颜色字符串。
                return parseColorString(resolved, color);
            }
        }

        return parseColorString(resolved, color);
    }

    /**
     * 构建当前文本样式。
     */
    protected Style buildStyle() {
        return buildStyle(color);
    }

    /**
     * 构建指定颜色的文本样式。
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
     * 检查 Shift 键是否按下。
     */
    protected static boolean isShowTipDown() {
        return TipRenderEventHandler.isShowTipDown();
    }

    /**
     * 是否因为 Shift 折叠而显示为提示行。
     */
    @Override
    public boolean isShiftCollapsed() {
        return shift && !isShowTipDown();
    }

    /**
     * 绘制 Shift 提示。
     */
    public static void renderShiftHint(TipRenderContext context, int x, int y) {
        Component hint = Component.translatable("tooltip.datatip.hold_shift",
            DatatipKeyMappings.SHOW_TIP.getTranslatedKeyMessage());
        context.drawString(hint, x, y, shiftHintColor(), true);
    }

    /**
     * 根据对齐方式计算 X 坐标。
     */
    protected int calcLineX(Font font, String text, int x, int maxWidth) {
        return calcLineX(font.width(text), x, maxWidth);
    }

    /**
     * 根据对齐方式计算 X 坐标。
     */
    protected int calcLineX(Font font, FormattedCharSequence text, int x, int maxWidth) {
        return calcLineX(font.width(text), x, maxWidth);
    }

    private int calcLineX(int lineWidth, int x, int maxWidth) {
        return switch (align) {
            case LEFT -> x;
            case CENTER -> x + (maxWidth > 0 ? (maxWidth - lineWidth) / 2 : -lineWidth / 2);
            case RIGHT -> x + (maxWidth > 0 ? maxWidth - lineWidth : -lineWidth);
        };
    }

    /**
     * 解析颜色字符串为 ARGB 颜色值。
     */
    public static int parseColorString(String colorStr, int defaultValue) {
        return ColorParser.parse(colorStr, defaultValue);
    }

    private static int shiftHintColor() {
        try {
            return DatatipConfig.shiftHintColor();
        } catch (IllegalStateException e) {
            return FALLBACK_SHIFT_HINT_COLOR;
        }
    }
}
