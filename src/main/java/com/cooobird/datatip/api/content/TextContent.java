package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipLayoutContext;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.layout.PreparedContent;
import com.cooobird.datatip.api.layout.PreparedLayout;
import com.cooobird.datatip.api.layout.TipPrepareContext;
import com.cooobird.datatip.internal.layout.PreparedTextLayout;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
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
public class TextContent extends BaseTextContent implements PreparedContent {

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
        this.langText = copyNonNullMap(langText);
        this.langStyledText = copyNonNullMap(langStyledText);
        this.maxWidth = ContentBounds.spacing(maxWidth);
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

    @Nullable
    private static <T> Map<String, T> copyNonNullMap(@Nullable Map<String, T> source) {
        if (source == null) return null;
        Map<String, T> copy = new LinkedHashMap<>();
        source.forEach((language, value) -> {
            if (language != null && value != null) copy.put(language, value);
        });
        return Map.copyOf(copy);
    }

    public static TextContent of(String text) {
        return new TextContent(text, null, null, null, null, null,
            TextContentDefaults.color(), null, true, TextAlign.LEFT,
            TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent of(String text, int color) {
        return new TextContent(text, null, null, null, null, null, color, null, true, TextAlign.LEFT,
            TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent centered(String text) {
        return new TextContent(text, null, null, null, null, null,
            TextContentDefaults.color(), null, true, TextAlign.CENTER,
            TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent centered(String text, int color) {
        return new TextContent(text, null, null, null, null, null, color, null, true, TextAlign.CENTER,
            TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent rightAligned(String text) {
        return new TextContent(text, null, null, null, null, null,
            TextContentDefaults.color(), null, true, TextAlign.RIGHT,
            TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent rightAligned(String text, int color) {
        return new TextContent(text, null, null, null, null, null, color, null, true, TextAlign.RIGHT,
            TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent of(Component component) {
        return new TextContent(null, component, null, null, null, null,
            TextContentDefaults.color(), null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent ofLang(Map<String, String> langText) {
        return new TextContent(null, null, null, langText, null, null,
            TextContentDefaults.color(), null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent ofLang(Map<String, String> langText, int color) {
        return new TextContent(null, null, null, langText, null, null,
            color, null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent ofLang(Map<String, String> langText, int color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return new TextContent(null, null, null, langText, null, null,
            color, null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, bold, italic, underlined, strikethrough, false);
    }

    public static TextContent ofLang(Map<String, String> langText, String colorExpression) {
        return new TextContent(null, null, null, langText, null, null,
            TextContentDefaults.color(), colorExpression, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent ofLang(Map<String, String> langText, String colorExpression, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return new TextContent(null, null, null, langText, null, null,
            TextContentDefaults.color(), colorExpression, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, bold, italic, underlined, strikethrough, false);
    }

    public static TextContent ofLangStyled(Map<String, LangStyle> langStyledText) {
        return new TextContent(null, null, null, null, langStyledText, null,
            TextContentDefaults.color(), null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent ofLangStyled(Map<String, LangStyle> langStyledText, boolean shift) {
        return new TextContent(null, null, null, null, langStyledText, null,
            TextContentDefaults.color(), null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, false, false, false, false, shift);
    }

    public static TextContent wrapped(String text, int maxWidth) {
        return new TextContent(text, null, null, null, null, null,
            TextContentDefaults.color(), null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), maxWidth, false, false, false, false, false);
    }

    public static TextContent wrapped(String text, int maxWidth, int color) {
        return new TextContent(text, null, null, null, null, null,
            color, null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), maxWidth, false, false, false, false, false);
    }

    public static TextContent of(FormattedText formattedText, int maxWidth) {
        return new TextContent(null, null, formattedText, null, null, null,
            TextContentDefaults.color(), null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), maxWidth, false, false, false, false, false);
    }

    public static TextContent styled(String text, Style style) {
        Component component = Component.literal(text).withStyle(style);
        return new TextContent(null, component, null, null, null, null,
            TextContentDefaults.color(), null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent withFont(String text, ResourceLocation font) {
        return new TextContent(text, null, null, null, null, font,
            TextContentDefaults.color(), null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    public static TextContent withFont(String text, ResourceLocation font, int color) {
        return new TextContent(text, null, null, null, null, font,
            color, null, true, TextAlign.LEFT, TextContentDefaults.lineHeight(), 0, false, false, false, false, false);
    }

    FormattedText formattedText(@Nullable net.minecraft.world.item.ItemStack stack) {
        return TextFormattedTextResolver.resolve(this, stack);
    }

    @Override
    public int getHeight(int availableWidth) {
        return TextContentLayout.getHeight(this, availableWidth);
    }

    @Override
    public int getHeight(TipLayoutContext context) {
        return TextContentLayout.getHeight(this, context);
    }

    public int getHeight(Font font, int availableWidth) {
        return TextContentLayout.getHeight(this, font, availableWidth);
    }

    @Override
    public boolean hasContent() {
        return TextContentLayout.hasContent(this);
    }

    @Override
    public int getWidth(int availableWidth) {
        return TextContentLayout.getWidth(this, availableWidth);
    }

    @Override
    public int getWidth(TipLayoutContext context) {
        return TextContentLayout.getWidth(this, context);
    }

    public int getWidth(Font font, int availableWidth) {
        return TextContentLayout.getWidth(this, font, availableWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        TextContentLayout.render(this, context, x, y, maxWidth, alpha);
    }

    @Override
    public PreparedLayout prepare(TipPrepareContext context) {
        var stack = context.requireLayoutContext().itemStack();
        return PreparedTextLayout.prepare(
            this,
            context,
            formattedText(stack),
            resolveColor(stack)
        );
    }
}
