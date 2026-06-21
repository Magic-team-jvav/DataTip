package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.config.DatatipConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 文本内容类。
 * <p>
 * 这是 DataTip 中最基础也是最常用的内容类型，用于在 tooltip 中显示文本。
 * 支持多种文本来源和丰富的样式控制。
 * </p>
 *
 * <h3>继承自 BaseTextContent</h3>
 * <p>
 * 通用样式属性（color、font、bold、italic、underlined、strikethrough、align、shadow、lineHeight、shift）
 * 和方法（resolveColor、buildStyle、parseColorString）由基类提供。
 * </p>
 *
 * @author cooobird
 * @see BaseTextContent 基类
 * @since 1.2.0
 */
public class TextContent extends BaseTextContent {

    private final @Nullable String text;
    private final @Nullable Component component;
    private final @Nullable FormattedText formattedText;
    private final @Nullable Map<String, String> langText;
    private final @Nullable Map<String, LangStyle> langStyledText;
    private final int maxWidth;

    // 构造函数
    public TextContent(@Nullable String text, @Nullable Component component, @Nullable FormattedText formattedText,
                       @Nullable Map<String, String> langText, @Nullable Map<String, LangStyle> langStyledText,
                       @Nullable ResourceLocation font, int color, @Nullable String colorExpression,
                       boolean shadow, TextAlign align, int lineHeight, int maxWidth,
                       boolean bold, boolean italic, boolean underlined, boolean strikethrough, boolean shift) {
        super(font, color, colorExpression, shadow, align, lineHeight, bold, italic, underlined, strikethrough, shift);
        this.text = text;
        this.component = component;
        this.formattedText = formattedText;
        this.langText = langText;
        this.langStyledText = langStyledText;
        this.maxWidth = maxWidth;
    }

    public @Nullable String text() {
        return text;
    }

    public @Nullable Component component() {
        return component;
    }

    public @Nullable FormattedText formattedText() {
        return formattedText;
    }

    public @Nullable Map<String, String> langText() {
        return langText;
    }

    public @Nullable Map<String, LangStyle> langStyledText() {
        return langStyledText;
    }

    public int maxWidth() {
        return maxWidth;
    }

    private static final int FALLBACK_COLOR = 0xFFAAAAAA;
    private static final int FALLBACK_LINE_HEIGHT = 12;

    private static int getDefaultColor() {
        try {
            return DatatipConfig.DEFAULT_COLOR.get();
        } catch (IllegalStateException e) {
            return FALLBACK_COLOR;
        }
    }

    private static int getDefaultLineHeight() {
        try {
            return DatatipConfig.DEFAULT_LINE_HEIGHT.get();
        } catch (IllegalStateException e) {
            return FALLBACK_LINE_HEIGHT;
        }
    }

    public static TextContent of(String text) {
        return new TextContent(text, null, null, null, null, null,
            getDefaultColor(), null, true, TextAlign.LEFT,
            getDefaultLineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent of(String text, int color) {
        return new TextContent(text, null, null, null, null, null, color, null, true, TextAlign.LEFT,
            getDefaultLineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent centered(String text) {
        return new TextContent(text, null, null, null, null, null,
            getDefaultColor(), null, true, TextAlign.CENTER,
            getDefaultLineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent centered(String text, int color) {
        return new TextContent(text, null, null, null, null, null, color, null, true, TextAlign.CENTER,
            getDefaultLineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent rightAligned(String text) {
        return new TextContent(text, null, null, null, null, null,
            getDefaultColor(), null, true, TextAlign.RIGHT,
            getDefaultLineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent rightAligned(String text, int color) {
        return new TextContent(text, null, null, null, null, null, color, null, true, TextAlign.RIGHT,
            getDefaultLineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent of(Component component) {
        return new TextContent(null, component, null, null, null, null,
            getDefaultColor(), null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    public static TextContent ofLang(Map<String, String> langText) {
        return new TextContent(null, null, null, langText, null, null,
            getDefaultColor(), null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    public static TextContent ofLang(Map<String, String> langText, int color) {
        return new TextContent(null, null, null, langText, null, null,
            color, null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    public static TextContent ofLang(Map<String, String> langText, int color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return new TextContent(null, null, null, langText, null, null,
            color, null, true, TextAlign.LEFT, 12, 0, bold, italic, underlined, strikethrough, false);
    }

    public static TextContent ofLang(Map<String, String> langText, String colorExpression) {
        return new TextContent(null, null, null, langText, null, null,
            getDefaultColor(), colorExpression, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    public static TextContent ofLang(Map<String, String> langText, String colorExpression, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return new TextContent(null, null, null, langText, null, null,
            getDefaultColor(), colorExpression, true, TextAlign.LEFT, 12, 0, bold, italic, underlined, strikethrough, false);
    }

    public static TextContent ofLangStyled(Map<String, LangStyle> langStyledText) {
        return new TextContent(null, null, null, null, langStyledText, null,
            getDefaultColor(), null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    public static TextContent ofLangStyled(Map<String, LangStyle> langStyledText, boolean shift) {
        return new TextContent(null, null, null, null, langStyledText, null,
            getDefaultColor(), null, true, TextAlign.LEFT, 12, 0, false, false, false, false, shift);
    }

    public static TextContent wrapped(String text, int maxWidth) {
        return new TextContent(text, null, null, null, null, null,
            getDefaultColor(), null, true, TextAlign.LEFT, 12, maxWidth, false, false, false, false, false);
    }

    public static TextContent wrapped(String text, int maxWidth, int color) {
        return new TextContent(text, null, null, null, null, null,
            color, null, true, TextAlign.LEFT, 12, maxWidth, false, false, false, false, false);
    }

    public static TextContent of(FormattedText formattedText, int maxWidth) {
        return new TextContent(null, null, formattedText, null, null, null,
            getDefaultColor(), null, true, TextAlign.LEFT, 12, maxWidth, false, false, false, false, false);
    }

    public static TextContent styled(String text, Style style) {
        Component component = Component.literal(text).withStyle(style);
        return new TextContent(null, component, null, null, null, null,
            getDefaultColor(), null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    public static TextContent withFont(String text, ResourceLocation font) {
        return new TextContent(text, null, null, null, null, font,
            getDefaultColor(), null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    public static TextContent withFont(String text, ResourceLocation font, int color) {
        return new TextContent(text, null, null, null, null, font,
            color, null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    private FormattedText getFormattedText(@Nullable TipRenderContext context) {
        if (formattedText != null) return formattedText;
        if (component != null) return component;

        String resolvedText = null;
        if (text != null) {
            resolvedText = (context != null) ? context.resolveVariables(text) : text;
        }

        // 优先使用带样式的多语言文本
        if (langStyledText != null && !langStyledText.isEmpty()) {
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            LangStyle langStyle = langStyledText.get(lang);
            if (langStyle != null) {
                Style style = Style.EMPTY.withColor(langStyle.color());
                if (font != null) style = style.withFont(font);
                if (langStyle.bold()) style = style.withBold(true);
                if (langStyle.italic()) style = style.withItalic(true);
                if (langStyle.underlined()) style = style.withUnderlined(true);
                if (langStyle.strikethrough()) style = style.withStrikethrough(true);
                return Component.literal(langStyle.text()).withStyle(style);
            }
        }

        // 其次使用简单多语言文本
        Style style = buildStyle();
        if (langText != null && !langText.isEmpty()) {
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            String langContent = langText.get(lang);
            if (langContent != null) {
                return Component.literal(langContent).withStyle(style);
            }
        }

        if (resolvedText != null) {
            return Component.literal(resolvedText).withStyle(style);
        }
        return FormattedText.EMPTY;
    }

    private FormattedText getFormattedText() {
        return getFormattedText(null);
    }

    @Override
    public int getHeight(int availableWidth) {
        Font font = Minecraft.getInstance().font;
        return getHeight(font, availableWidth);
    }

    public int getHeight(Font font, int availableWidth) {
        int effectiveMaxWidth = (maxWidth > 0) ? maxWidth : availableWidth;
        int effectiveWidth = Math.min(effectiveMaxWidth, availableWidth);

        FormattedText text = getFormattedText();
        if (text == FormattedText.EMPTY) return 0;

        if (effectiveWidth <= 0) return lineHeight;

        List<FormattedCharSequence> lines = font.split(text, effectiveWidth);
        return Math.max(1, lines.size()) * lineHeight;
    }

    @Override
    public int getWidth(int availableWidth) {
        Font font = Minecraft.getInstance().font;
        return getWidth(font, availableWidth);
    }

    public int getWidth(Font font, int availableWidth) {
        int effectiveMaxWidth = (maxWidth > 0) ? maxWidth : availableWidth;
        int effectiveWidth = Math.min(effectiveMaxWidth, availableWidth);

        FormattedText text = getFormattedText();
        if (text == FormattedText.EMPTY) return 0;

        if (effectiveWidth <= 0) return 0;

        if (align == TextAlign.CENTER || align == TextAlign.RIGHT) return effectiveWidth;

        if (maxWidth > 0) return effectiveWidth;

        int textWidth = font.width(text);
        return Math.min(textWidth, availableWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;
        if (getFormattedText(context) == FormattedText.EMPTY) return;

        if (shift && !isShowTipDown()) {
            renderShiftHint(context, x, y);
            return;
        }

        Font font = context.font();
        int resolvedColor = resolveColor(context);

        if (maxWidth > 0) {
            renderWrapped(context, font, x, y, maxWidth, resolvedColor);
        } else {
            renderSingleLine(context, font, x, y, maxWidth, resolvedColor);
        }
    }

    private void renderWrapped(TipRenderContext context, Font font, int x, int y, int maxWidth, int resolvedColor) {
        FormattedText text = getFormattedText(context);
        List<FormattedCharSequence> lines = font.split(text, maxWidth);

        for (FormattedCharSequence line : lines) {
            int lineX = calcLineX(font, line, x, maxWidth);
            context.graphics().drawString(font, line, lineX, y, resolvedColor, shadow);
            y += lineHeight;
        }
    }

    private void renderSingleLine(TipRenderContext context, Font font, int x, int y, int maxWidth, int resolvedColor) {
        FormattedText text = getFormattedText(context);
        List<FormattedCharSequence> lines = font.split(text, Integer.MAX_VALUE);
        if (lines.isEmpty()) return;

        FormattedCharSequence visualText = lines.get(0);
        int lineX = calcLineX(font, visualText, x, maxWidth);
        context.graphics().drawString(font, visualText, lineX, y, resolvedColor, shadow);
    }
}
