package com.cooobird.datatip.api;

import com.cooobird.datatip.api.content.VBoxContent;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tooltip 主渲染器。
 * 负责解析 JSON、管理动画、渲染内容。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TipRenderer {

    private static final int DEFAULT_PADDING = 4;
    private static final int DEFAULT_BACKGROUND_COLOR = 0xF0100010;
    private static final int DEFAULT_BORDER_COLOR = 0x505000FF;

    private final ParseContext parseContext;

    /**
     * 创建渲染器。
     */
    public TipRenderer() {
        this.parseContext = new ParseContext();
    }

    /**
     * 渲染 JSON 定义的 tooltip。
     *
     * @param graphics    GuiGraphics
     * @param font        字体
     * @param json        tooltip JSON 定义
     * @param x           X 坐标
     * @param y           Y 坐标
     * @param tickCount   当前 tick
     * @param partialTick 帧间插值
     */
    public void render(GuiGraphics graphics, Font font, JsonObject json, int x, int y, int tickCount, float partialTick) {
        // 1. 解析 JSON 为内容树
        TipContent content = parseContent(json);
        if (content == null) return;

        // 2. 创建渲染上下文
        TipRenderContext context = new TipRenderContext(graphics, font, tickCount, partialTick);

        // 3. 计算尺寸
        int maxWidth = json.has("maxWidth") ? json.get("maxWidth").getAsInt() : 200;
        int padding = json.has("padding") ? json.get("padding").getAsInt() : DEFAULT_PADDING;

        int contentWidth = content.getWidth(maxWidth);
        int contentHeight = content.getHeight(maxWidth);

        int totalWidth = contentWidth + padding * 2;
        int totalHeight = contentHeight + padding * 2;

        // 4. 渲染背景
        int bgColor = json.has("backgroundColor") ?
            parseColor(json.get("backgroundColor").getAsString()) : DEFAULT_BACKGROUND_COLOR;
        int borderColor = json.has("borderColor") ?
            parseColor(json.get("borderColor").getAsString()) : DEFAULT_BORDER_COLOR;

        renderBackground(context, x, y, totalWidth, totalHeight, bgColor, borderColor);

        // 5. 渲染内容
        content.render(context, x + padding, y + padding, contentWidth, 1.0f);
    }

    /**
     * 渲染内容列表（垂直布局）。
     */
    public void renderContents(GuiGraphics graphics, Font font, List<TipContent> contents, int x, int y, int tickCount, float partialTick) {
        renderContents(graphics, font, contents, x, y, tickCount, partialTick, true);
    }

    /**
     * 渲染内容列表（垂直布局，可控制动画）。
     */
    public void renderContents(GuiGraphics graphics, Font font, List<TipContent> contents, int x, int y, int tickCount, float partialTick, boolean animationsEnabled) {
        TipRenderContext context = new TipRenderContext(graphics, font, tickCount, partialTick);

        VBoxContent vbox = VBoxContent.create();
        contents.forEach(vbox::addChild);

        // 使用配置的最大宽度
        int maxWidth = DatatipConfig.MAX_WIDTH.get();
        int padding = DEFAULT_PADDING;

        int contentWidth = vbox.getWidth(maxWidth);
        int contentHeight = vbox.getHeight(maxWidth);

        int totalWidth = contentWidth + padding * 2;
        int totalHeight = contentHeight + padding * 2;

        renderBackground(context, x, y, totalWidth, totalHeight, DEFAULT_BACKGROUND_COLOR, DEFAULT_BORDER_COLOR);

        // 根据配置决定是否启用动画
        if (animationsEnabled) {
            vbox.render(context, x + padding, y + padding, contentWidth, 1.0f);
        } else {
            // 渲染静态内容
            vbox.renderStatic(context, x + padding, y + padding, contentWidth, 1.0f);
        }
    }

    /**
     * 解析 JSON 为 TipContent。
     */
    @Nullable
    public TipContent parseContent(JsonObject json) {
        return TipContentRegistry.parse(json, parseContext);
    }

    /**
     * 解析 JSON 数组为内容列表。
     */
    public List<TipContent> parseContents(JsonArray json) {
        return parseContext.parseContentArray(json);
    }

    /**
     * 渲染背景。
     */
    private void renderBackground(TipRenderContext context, int x, int y, int width, int height, int bgColor, int borderColor) {
        // 背景填充
        context.fill(x + 1, y, x + width - 1, y + height, bgColor);
        context.fill(x, y + 1, x + 1, y + height - 1, bgColor);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, bgColor);

        // 边框
        context.fill(x + 1, y + 1, x + width - 1, y + 2, borderColor);  // 上边
        context.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, borderColor);  // 下边
        context.fill(x + 1, y + 1, x + 2, y + height - 1, borderColor);  // 左边
        context.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, borderColor);  // 右边
    }

    /**
     * 解析颜色字符串。
     */
    private static int parseColor(String colorStr) {
        if (colorStr.startsWith("#")) {
            try {
                return (int) Long.parseLong(colorStr.substring(1), 16) | 0xFF000000;
            } catch (NumberFormatException e) {
                return 0xFFFFFFFF;
            }
        }

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
            default -> 0xFFFFFFFF;
        };
    }

    /**
     * 获取解析上下文。
     */
    public ParseContext getParseContext() {
        return parseContext;
    }
}
