package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipRenderContext;
import com.cooobird.datatip.api.expression.ExpressionParser;
import com.cooobird.datatip.api.util.VariableResolver;
import com.cooobird.datatip.config.DatatipConfig;
import com.cooobird.datatip.event.TipRenderEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本内容类。
 * <p>
 * 这是 DataTip 中最基础也是最常用的内容类型，用于在 tooltip 中显示文本。
 * 支持多种文本来源和丰富的样式控制。
 * </p>
 *
 * <h3>功能特性</h3>
 * <ul>
 *   <li><b>纯文本</b>：直接传入字符串，支持变量替换</li>
 *   <li><b>Component</b>：使用 Minecraft 的 Component 对象</li>
 *   <li><b>多语言</b>：根据游戏语言自动切换文本</li>
 *   <li><b>颜色表达式</b>：支持动态颜色，根据条件变化</li>
 *   <li><b>文本样式</b>：粗体、斜体、下划线、删除线</li>
 *   <li><b>对齐方式</b>：左对齐、居中、右对齐</li>
 *   <li><b>自动换行</b>：超过指定宽度自动换行</li>
 *   <li><b>Shift 显示</b>：按住 Shift 键才显示内容</li>
 *   <li><b>自定义字体</b>：支持 ResourceLocation 指定字体</li>
 * </ul>
 *
 * <h3>JSON 示例</h3>
 * <pre>{@code
 * // 基础文本
 * {"type": "text", "text": "Hello World", "color": "white"}
 *
 * // 带样式的文本
 * {"type": "text", "text": "粗体红色", "color": "red", "bold": true}
 *
 * // 居中对齐
 * {"type": "text", "text": "居中文本", "color": "gold", "align": "center"}
 *
 * // 多语言文本
 * {"type": "text", "text": {"zh_cn": "你好", "en_us": "Hello"}, "color": "aqua"}
 *
 * // 颜色表达式
 * {"type": "text", "text": "状态", "color": "{durability > 100 ? 'green' : 'red'}"}
 *
 * // Shift 显示
 * {"type": "text", "text": "需要按 Shift", "color": "gray", "shift": true}
 * }</pre>
 *
 * <h3>变量支持</h3>
 * <p>
 * 在 text 字段中可以使用 {@code {variable}} 语法引用变量，
 * 变量会在渲染时自动替换为实际值。
 * </p>
 *
 * @author cooobird
 * @see VariableResolver 支持的变量列表
 * @see ExpressionParser 表达式语法
 * @since 1.2.0
 */
public record TextContent(
    @Nullable String text,                    // 纯文本内容
    @Nullable Component component,            // Minecraft Component 对象
    @Nullable FormattedText formattedText,    // FormattedText 对象
    @Nullable Map<String, String> langText,   // 多语言文本映射
    @Nullable ResourceLocation font,          // 自定义字体
    int color,                                // 静态颜色值
    @Nullable String colorExpression,         // 颜色表达式
    boolean shadow,                           // 是否显示文字阴影
    TextAlign align,                          // 文本对齐方式
    int lineHeight,                           // 行高
    int maxWidth,                             // 最大宽度
    boolean bold,                             // 粗体
    boolean italic,                           // 斜体
    boolean underlined,                       // 下划线
    boolean strikethrough,                    // 删除线
    boolean shift                             // 需要按住 Shift 才显示
) implements TipContent {

    // 文本对齐方式
    public enum TextAlign {
        LEFT,    // 左对齐
        CENTER,  // 居中对齐
        RIGHT    // 右对齐
    }

    // 创建纯文本内容
    public static TextContent of(String text) {
        return new TextContent(text, null, null, null, null,
            DatatipConfig.DEFAULT_COLOR.get(), null, true, TextAlign.LEFT,
            DatatipConfig.DEFAULT_LINE_HEIGHT.get(), 0, false, false, false, false, false);
    }

    // 创建带颜色的文本内容
    public static TextContent of(String text, int color) {
        return new TextContent(text, null, null, null, null, color, null, true, TextAlign.LEFT,
            DatatipConfig.DEFAULT_LINE_HEIGHT.get(), 0, false, false, false, false, false);
    }

    // 创建居中文本内容
    public static TextContent centered(String text) {
        return new TextContent(text, null, null, null, null,
            DatatipConfig.DEFAULT_COLOR.get(), null, true, TextAlign.CENTER,
            DatatipConfig.DEFAULT_LINE_HEIGHT.get(), 0, false, false, false, false, false);
    }

    // 创建居中带颜色的文本内容
    public static TextContent centered(String text, int color) {
        return new TextContent(text, null, null, null, null, color, null, true, TextAlign.CENTER,
            DatatipConfig.DEFAULT_LINE_HEIGHT.get(), 0, false, false, false, false, false);
    }

    // 创建右对齐文本内容
    public static TextContent rightAligned(String text) {
        return new TextContent(text, null, null, null, null,
            DatatipConfig.DEFAULT_COLOR.get(), null, true, TextAlign.RIGHT,
            DatatipConfig.DEFAULT_LINE_HEIGHT.get(), 0, false, false, false, false, false);
    }

    // 创建右对齐带颜色的文本内容
    public static TextContent rightAligned(String text, int color) {
        return new TextContent(text, null, null, null, null, color, null, true, TextAlign.RIGHT,
            DatatipConfig.DEFAULT_LINE_HEIGHT.get(), 0, false, false, false, false, false);
    }

    // 创建 Component 内容
    public static TextContent of(Component component) {
        return new TextContent(null, component, null, null, null,
            0xFFFFFF, null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    // 创建多语言文本内容
    public static TextContent ofLang(Map<String, String> langText) {
        return new TextContent(null, null, null, langText, null,
            0xFFFFFF, null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    // 创建多语言文本内容
    public static TextContent ofLang(Map<String, String> langText, int color) {
        return new TextContent(null, null, null, langText, null,
            color, null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    // 创建自动换行的文本内容
    public static TextContent wrapped(String text, int maxWidth) {
        return new TextContent(text, null, null, null, null,
            0xFFFFFF, null, true, TextAlign.LEFT, 12, maxWidth, false, false, false, false, false);
    }

    // 创建自动换行的文本内容
    public static TextContent wrapped(String text, int maxWidth, int color) {
        return new TextContent(text, null, null, null, null,
            color, null, true, TextAlign.LEFT, 12, maxWidth, false, false, false, false, false);
    }

    // 创建 FormattedText 内容
    public static TextContent of(FormattedText formattedText, int maxWidth) {
        return new TextContent(null, null, formattedText, null, null,
            0xFFFFFF, null, true, TextAlign.LEFT, 12, maxWidth, false, false, false, false, false);
    }

    // 创建带样式的文本内容
    public static TextContent styled(String text, Style style) {
        Component component = Component.literal(text).withStyle(style);
        return new TextContent(null, component, null, null, null,
            0xFFFFFF, null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    // 创建带字体的文本内容
    public static TextContent withFont(String text, ResourceLocation font) {
        return new TextContent(text, null, null, null, font,
            0xFFFFFF, null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    // 创建带字体和颜色的文本内容
    public static TextContent withFont(String text, ResourceLocation font, int color) {
        return new TextContent(text, null, null, null, font,
            color, null, true, TextAlign.LEFT, 12, 0, false, false, false, false, false);
    }

    // 解析颜色值
    private int resolveColor(TipRenderContext context) {
        // 没有颜色表达式，直接返回静态颜色
        if (colorExpression == null || colorExpression.isEmpty()) {
            return color;
        }

        // 替换表达式中的变量
        String resolved = context.resolveVariables(colorExpression);
        if (resolved == null || resolved.isEmpty()) {
            return color;
        }

        // 检查是否是表达式
        if (resolved.contains("?") || resolved.contains(">") || resolved.contains("<")) {
            try {
                Map<String, String> variables = new HashMap<>();
                Object result = ExpressionParser.evaluate(resolved, variables);
                if (result instanceof String s) {
                    return parseColorString(s, color);
                }
            } catch (Exception e) {
                // 解析失败，返回默认颜色
            }
        }

        return parseColorString(resolved, color);
    }

    // 解析颜色字符串为 ARGB 颜色值
    private static int parseColorString(String colorStr, int defaultValue) {
        if (colorStr == null || colorStr.isEmpty()) return defaultValue;

        // 命名颜色映射
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
                // 尝试解析十六进制格式
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

    // 构建文本样式
    private Style buildStyle() {
        Style style = Style.EMPTY.withColor(color);
        if (font != null) style = style.withFont(font);
        if (bold) style = style.withBold(true);
        if (italic) style = style.withItalic(true);
        if (underlined) style = style.withUnderlined(true);
        if (strikethrough) style = style.withStrikethrough(true);
        return style;
    }

    // 获取要处理的 FormattedText
    private FormattedText getFormattedText(@Nullable TipRenderContext context) {
        if (formattedText != null) return formattedText;
        if (component != null) return component;

        Style style = buildStyle();

        // 获取文本内容
        String resolvedText = null;
        if (text != null) {
            resolvedText = (context != null) ? context.resolveVariables(text) : text;
        }

        // 优先使用多语言文本
        if (langText != null && !langText.isEmpty()) {
            String lang = Minecraft.getInstance().getLanguageManager().getSelected();
            String langContent = langText.get(lang);
            // 如果没有当前语言的翻译，使用第一个可用的翻译
            if (langContent == null) {
                langContent = langText.values().iterator().next();
            }
            if (langContent != null) {
                return Component.literal(langContent).withStyle(style);
            }
        }

        if (resolvedText != null) {
            return Component.literal(resolvedText).withStyle(style);
        }
        return FormattedText.EMPTY;
    }

    // 获取要处理的 FormattedText
    private FormattedText getFormattedText() {
        return getFormattedText(null);
    }

    // 分割文本为多行
    private List<FormattedCharSequence> splitLines(Font font, int maxWidth) {
        FormattedText text = getFormattedText();
        if (maxWidth <= 0) {
            // 不换行，返回单行
            if (component != null) {
                return List.of(component.getVisualOrderText());
            }
            return font.split(text, Integer.MAX_VALUE);
        }
        return font.split(text, maxWidth);
    }

    @Override
    public int getHeight(int availableWidth) {
        // 使用精确计算方法
        Font font = Minecraft.getInstance().font;
        return getHeight(font, availableWidth);
    }

    // 获取精确高度
    public int getHeight(Font font, int availableWidth) {
        int effectiveMaxWidth = (maxWidth > 0) ? maxWidth : availableWidth;
        int effectiveWidth = Math.min(effectiveMaxWidth, availableWidth);

        if (effectiveWidth <= 0) {
            return lineHeight;
        }

        FormattedText text = getFormattedText();
        List<FormattedCharSequence> lines = font.split(text, effectiveWidth);
        return Math.max(1, lines.size()) * lineHeight;
    }

    @Override
    public int getWidth(int availableWidth) {
        // 使用精确计算方法
        Font font = Minecraft.getInstance().font;
        return getWidth(font, availableWidth);
    }

    // 获取精确宽度
    public int getWidth(Font font, int availableWidth) {
        int effectiveMaxWidth = (maxWidth > 0) ? maxWidth : availableWidth;
        int effectiveWidth = Math.min(effectiveMaxWidth, availableWidth);

        if (effectiveWidth <= 0) {
            return 0;
        }

        if (align == TextAlign.CENTER || align == TextAlign.RIGHT) {
            return effectiveWidth;
        }

        FormattedText text = getFormattedText();

        // 如果需要换行，返回换行宽度
        if (maxWidth > 0) {
            return effectiveWidth;
        }

        int textWidth = font.width(text);
        return Math.min(textWidth, availableWidth);
    }

    @Override
    public void render(TipRenderContext context, int x, int y, int maxWidth, float alpha) {
        if (alpha <= 0) return;

        if (shift && !isShowTipDown()) {
            Component hint = Component.translatable("tooltip.datatip.hold_shift",
                TipRenderEventHandler.SHOW_TIP.getTranslatedKeyMessage());
            // 左对齐显示提示文本
            context.drawString(hint, x, y, 0x888888, true);
            return;
        }

        Font font = context.font();

        // 如果有自定义字体，获取对应的 Font 对象
        if (this.font != null) {
            font = Minecraft.getInstance().font;
        }

        // 解析颜色
        int resolvedColor = resolveColor(context);

        if (maxWidth > 0) {
            // 自动换行渲染
            renderWrapped(context, font, x, y, maxWidth, resolvedColor);
        } else {
            // 单行渲染
            renderSingleLine(context, font, x, y, maxWidth, resolvedColor);
        }
    }

    // 检查 Shift 键是否按下
    private static boolean isShowTipDown() {
        return Screen.hasShiftDown();
    }

    // 渲染自动换行文本
    private void renderWrapped(TipRenderContext context, Font font, int x, int y, int maxWidth, int resolvedColor) {
        FormattedText text = getFormattedText(context);
        List<FormattedCharSequence> lines = font.split(text, maxWidth);

        for (FormattedCharSequence line : lines) {
            // 根据对齐方式计算 X 坐标
            int lineX = switch (align) {
                case LEFT -> x;
                case CENTER -> {
                    int lineWidth = font.width(line);
                    yield x + (maxWidth - lineWidth) / 2;
                }
                case RIGHT -> {
                    int lineWidth = font.width(line);
                    yield x + maxWidth - lineWidth;
                }
            };
            renderFormattedCharSequence(context, font, line, lineX, y, resolvedColor);
            y += lineHeight;
        }
    }

    // 渲染单行文本
    private void renderSingleLine(TipRenderContext context, Font font, int x, int y, int maxWidth, int resolvedColor) {
        FormattedText text = getFormattedText(context);

        List<FormattedCharSequence> lines = font.split(text, Integer.MAX_VALUE);
        if (lines.isEmpty()) return;

        FormattedCharSequence visualText = lines.getFirst();
        int lineWidth = font.width(visualText);

        // 根据对齐方式计算 X 坐标
        int lineX = switch (align) {
            case LEFT -> x;
            case CENTER -> {
                if (maxWidth > 0) yield x + (maxWidth - lineWidth) / 2;
                else yield x - lineWidth / 2;
            }
            case RIGHT -> {
                if (maxWidth > 0) yield x + maxWidth - lineWidth;
                else yield x - lineWidth;
            }
        };

        renderFormattedCharSequence(context, font, visualText, lineX, y, resolvedColor);
    }

    // 渲染 FormattedCharSequence
    private void renderFormattedCharSequence(TipRenderContext context, Font font, FormattedCharSequence text, int x, int y, int resolvedColor) {
        context.graphics().drawString(font, text, x, y, resolvedColor, shadow);
    }
}
