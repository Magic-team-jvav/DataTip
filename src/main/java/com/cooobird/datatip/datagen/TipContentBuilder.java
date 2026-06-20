package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;

/**
 * 新版本 Tooltip 构建器
 * 用于 datagen 生成 JSON 配置
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TipContentBuilder {

    /**
     * 创建文本内容
     */
    public static TextContent text(String text) {
        return TextContent.of(text);
    }

    /**
     * 创建带颜色的文本内容
     */
    public static TextContent text(String text, String color) {
        return TextContent.of(text, parseColor(color));
    }

    /**
     * 创建带样式的文本内容
     */
    public static TextContent text(String text, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return new TextContent(text, null, null, null, null, null,
            parseColor(color), null, true, BaseTextContent.TextAlign.LEFT, 12, 0,
            bold, italic, underlined, strikethrough, false);
    }

    /**
     * 创建带自定义字体的文本内容
     */
    public static TextContent text(String text, String color, String font) {
        return new TextContent(text, null, null, null, null,
            ResourceLocation.tryParse(font),
            parseColor(color), null, true, BaseTextContent.TextAlign.LEFT, 12, 0,
            false, false, false, false, false);
    }

    /**
     * 创建多语言文本内容
     */
    public static TextContent langText(Map<String, String> langText) {
        return TextContent.ofLang(langText);
    }

    /**
     * 创建多语言文本内容（带颜色）
     */
    public static TextContent langText(Map<String, String> langText, String color) {
        return TextContent.ofLang(langText, parseColor(color));
    }

    /**
     * 创建多语言文本内容（带样式）
     */
    public static TextContent langText(Map<String, String> langText, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return TextContent.ofLang(langText, parseColor(color), bold, italic, underlined, strikethrough);
    }

    /**
     * 创建多语言文本内容（带字体）
     */
    public static TextContent langText(Map<String, String> langText, String color, String font) {
        return new TextContent(null, null, null, langText, null,
            ResourceLocation.tryParse(font),
            parseColor(color), null, true, BaseTextContent.TextAlign.LEFT, 12, 0,
            false, false, false, false, false);
    }

    /**
     * 创建居中文本
     */
    public static TextContent centered(String text) {
        return TextContent.centered(text);
    }

    /**
     * 创建居中文本（带颜色）
     */
    public static TextContent centered(String text, String color) {
        return TextContent.centered(text, parseColor(color));
    }

    /**
     * 创建居中文本（带样式）
     */
    public static TextContent centered(String text, String color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
        return new TextContent(text, null, null, null, null, null,
            parseColor(color), null, true, BaseTextContent.TextAlign.CENTER, 12, 0,
            bold, italic, underlined, strikethrough, false);
    }

    /**
     * 创建右对齐文本
     */
    public static TextContent rightAligned(String text) {
        return TextContent.rightAligned(text);
    }

    /**
     * 创建右对齐文本（带颜色）
     */
    public static TextContent rightAligned(String text, String color) {
        return TextContent.rightAligned(text, parseColor(color));
    }

    /**
     * 创建间距
     */
    public static SpacerContent spacer(int height) {
        return SpacerContent.of(height);
    }

    /**
     * 创建分割线
     */
    public static DividerContent divider() {
        return DividerContent.create();
    }

    /**
     * 创建分割线（带颜色）
     */
    public static DividerContent divider(String color) {
        return DividerContent.of(parseColor(color));
    }

    /**
     * 创建分割线（带颜色和样式）
     */
    public static DividerContent divider(String color, String style) {
        return DividerContent.of(parseColor(color), switch (style.toLowerCase()) {
            case "dashed" -> DividerContent.DividerStyle.DASHED;
            case "dotted" -> DividerContent.DividerStyle.DOTTED;
            default -> DividerContent.DividerStyle.SOLID;
        });
    }

    /**
     * 创建物品内容
     */
    public static ItemContent item(String itemId) {
        return ItemContent.of(new ItemStack(
            BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))
        ));
    }

    /**
     * 创建物品内容（带标签）
     */
    public static ItemContent item(String itemId, String label) {
        return ItemContent.withLabel(
            new ItemStack(
                BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))
            ),
            Component.literal(label)
        );
    }

    /**
     * 创建进度条
     */
    public static ProgressContent progress(float progress, int width) {
        return ProgressContent.of(progress, width);
    }

    /**
     * 创建进度条（带标签）
     */
    public static ProgressContent progress(float progress, int width, String label) {
        return ProgressContent.withCustomLabel(progress, width,
            net.minecraft.network.chat.Component.literal(label));
    }

    /**
     * 创建垂直布局
     */
    public static VBoxContent vbox(TipContent... children) {
        VBoxContent vbox = VBoxContent.create();
        for (TipContent child : children) {
            vbox.addChild(child);
        }
        return vbox;
    }

    /**
     * 创建垂直布局（带间距）
     */
    public static VBoxContent vbox(int gap, TipContent... children) {
        VBoxContent vbox = VBoxContent.withGap(gap);
        for (TipContent child : children) {
            vbox.addChild(child);
        }
        return vbox;
    }

    /**
     * 创建水平布局
     */
    public static HBoxContent hbox(TipContent... children) {
        HBoxContent hbox = HBoxContent.create();
        for (TipContent child : children) {
            hbox.addChild(child);
        }
        return hbox;
    }

    /**
     * 创建水平布局（带间距）
     */
    public static HBoxContent hbox(int gap, TipContent... children) {
        HBoxContent hbox = HBoxContent.withGap(gap);
        for (TipContent child : children) {
            hbox.addChild(child);
        }
        return hbox;
    }

    /**
     * 创建轮播容器
     */
    public static CarouselContent carousel(int intervalSeconds, TipContent... frames) {
        CarouselContent carousel = CarouselContent.withInterval(intervalSeconds);
        for (TipContent frame : frames) {
            carousel.addFrame(frame);
        }
        return carousel;
    }

    /**
     * 创建打字机效果
     */
    public static TypewriterContent typewriter(String... lines) {
        return TypewriterContent.of(lines);
    }

    /**
     * 创建打字机效果（带颜色）
     */
    public static TypewriterContent typewriter(int color, String... lines) {
        return TypewriterContent.of(color, lines);
    }

    /**
     * 创建打字机效果（带颜色和字体）
     */
    public static TypewriterContent typewriter(int color, String font, String... lines) {
        return new TypewriterContent(List.of(lines), 2, 1, false, color, ResourceLocation.tryParse(font));
    }

    /**
     * 创建打字机效果（全参数）
     */
    public static TypewriterContent typewriter(int color, int charsPerSecond, int pauseSeconds, boolean loop, String... lines) {
        return new TypewriterContent(List.of(lines), charsPerSecond, pauseSeconds, loop, color);
    }

    /**
     * 创建实体内容
     */
    public static EntityContent entity(String entityId, int size) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityId));
        return EntityContent.of(entityType, size);
    }

    /**
     * 创建实体内容（带偏移）
     */
    public static EntityContent entity(String entityId, int size, int offsetX, int offsetY) {
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityId));
        return EntityContent.withOffset(entityType, size, offsetX, offsetY);
    }

    /**
     * 创建方块内容
     */
    public static BlockContent block(String blockId, int size) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
        return BlockContent.of(block, size);
    }

    /**
     * 创建方块内容（带偏移）
     */
    public static BlockContent block(String blockId, int size, int offsetX, int offsetY) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
        return BlockContent.withOffset(block, size, offsetX, offsetY);
    }

    /**
     * 创建纹理内容（从物品 ID）
     */
    public static AtlasContent atlas(String itemId, int size) {
        return AtlasContent.fromItem(ResourceLocation.parse(itemId), size);
    }

    /**
     * 创建纹理内容（带偏移）
     */
    public static AtlasContent atlas(String itemId, int size, int offsetX, int offsetY) {
        return AtlasContent.withOffset(ResourceLocation.parse(itemId), size, offsetX, offsetY);
    }

    /**
     * 创建图片内容
     */
    public static ImageContent image(String texture, int width, int height) {
        return ImageContent.of(ResourceLocation.parse(texture), width, height);
    }

    /**
     * 创建图片内容（带偏移）
     */
    public static ImageContent image(String texture, int width, int height, int offsetX, int offsetY) {
        return ImageContent.withOffset(ResourceLocation.parse(texture), width, height, offsetX, offsetY);
    }

    /**
     * 创建图表内容
     */
    public static ChartContent chart(String chartType, int width, int height) {
        return switch (chartType.toLowerCase()) {
            case "pie" -> ChartContent.pie(width);
            case "line" -> ChartContent.line(width, height);
            default -> ChartContent.bar(width, height);
        };
    }

    /**
     * 创建图表内容（带标题和数据）
     */
    public static ChartContent chart(String chartType, int width, int height, String title, ChartContent.ChartEntry... entries) {
        ChartContent chart = switch (chartType.toLowerCase()) {
            case "pie" -> ChartContent.pie(width);
            case "line" -> ChartContent.line(width, height);
            default -> ChartContent.bar(width, height);
        };
        chart = chart.title(Component.literal(title));
        for (ChartContent.ChartEntry entry : entries) {
            chart = chart.addEntry(entry.label(), entry.valueExpr(), entry.color());
        }
        return chart;
    }

    /**
     * 创建图表数据条目
     */
    public static ChartContent.ChartEntry chartEntry(String label, double value, String color) {
        return new ChartContent.ChartEntry(label, String.valueOf(value), parseColor(color));
    }

    /**
     * 创建图表数据条目（带变量表达式）
     */
    public static ChartContent.ChartEntry chartEntry(String label, String valueExpr, String color) {
        return new ChartContent.ChartEntry(label, valueExpr, parseColor(color));
    }

    /**
     * 创建对齐包装
     */
    public static AlignedContent aligned(TipContent content, VBoxContent.HorizontalAlign align) {
        return new AlignedContent(content, align);
    }

    /**
     * 创建居中对齐包装
     */
    public static AlignedContent centeredAligned(TipContent content) {
        return new AlignedContent(content, VBoxContent.HorizontalAlign.CENTER);
    }

    /**
     * 创建右对齐包装
     */
    public static AlignedContent rightAlignedContent(TipContent content) {
        return new AlignedContent(content, VBoxContent.HorizontalAlign.RIGHT);
    }

    /**
     * 将 TipContent 转换为 JSON
     */
    public static JsonObject toJson(TipContent content) {
        JsonObject json = new JsonObject();

        if (content instanceof TextContent textContent) {
            json.addProperty("type", "text");
            if (textContent.text() != null) {
                json.addProperty("text", textContent.text());
            }
            if (textContent.color() != 0xFFFFFF) {
                json.addProperty("color", String.format("#%06X", textContent.color() & 0xFFFFFF));
            }
            if (textContent.font() != null) {
                json.addProperty("font", textContent.font().toString());
            }
            if (textContent.align() == BaseTextContent.TextAlign.CENTER) {
                json.addProperty("align", "center");
            } else if (textContent.align() == BaseTextContent.TextAlign.RIGHT) {
                json.addProperty("align", "right");
            }
            if (textContent.bold()) json.addProperty("bold", true);
            if (textContent.italic()) json.addProperty("italic", true);
            if (textContent.underlined()) json.addProperty("underlined", true);
            if (textContent.strikethrough()) json.addProperty("strikethrough", true);
        } else if (content instanceof SpacerContent spacer) {
            json.addProperty("type", "spacer");
            json.addProperty("height", spacer.height());
        } else if (content instanceof DividerContent divider) {
            json.addProperty("type", "divider");
            json.addProperty("color", String.format("#%06X", divider.color() & 0xFFFFFF));
        } else if (content instanceof ItemContent item) {
            json.addProperty("type", "item");
            json.addProperty("item", item.getStack().getItem().toString());
        } else if (content instanceof ProgressContent progress) {
            json.addProperty("type", "progress");
            json.addProperty("progress", progress.progress());
            json.addProperty("width", progress.width());
        } else if (content instanceof VBoxContent vbox) {
            json.addProperty("type", "vbox");
            json.addProperty("gap", vbox.gap());
            JsonArray children = new JsonArray();
            for (TipContent child : vbox.children()) {
                children.add(toJson(child));
            }
            json.add("children", children);
        } else if (content instanceof HBoxContent hbox) {
            json.addProperty("type", "hbox");
            JsonArray children = new JsonArray();
            for (TipContent child : hbox.children()) {
                children.add(toJson(child));
            }
            json.add("children", children);
        } else if (content instanceof CarouselContent carousel) {
            json.addProperty("type", "carousel");
            json.addProperty("intervalSeconds", carousel.getIntervalSeconds());
            JsonArray frames = new JsonArray();
            for (TipContent frame : carousel.getFrames()) {
                frames.add(toJson(frame));
            }
            json.add("frames", frames);
        } else if (content instanceof EntityContent entity) {
            json.addProperty("type", "entity");
            json.addProperty("entity", BuiltInRegistries.ENTITY_TYPE.getKey(entity.entityType()).toString());
            json.addProperty("size", entity.size());
            json.addProperty("autoRotate", entity.autoRotate());
            if (entity.offsetX() != 0) {
                json.addProperty("offsetX", entity.offsetX());
            }
            if (entity.offsetY() != 0) {
                json.addProperty("offsetY", entity.offsetY());
            }
        } else if (content instanceof BlockContent block) {
            json.addProperty("type", "block");
            json.addProperty("block", BuiltInRegistries.BLOCK.getKey(block.block()).toString());
            json.addProperty("size", block.size());
            json.addProperty("autoRotate", block.autoRotate());
            if (block.offsetX() != 0) {
                json.addProperty("offsetX", block.offsetX());
            }
            if (block.offsetY() != 0) {
                json.addProperty("offsetY", block.offsetY());
            }
        } else if (content instanceof AtlasContent atlas) {
            json.addProperty("type", "atlas");
            json.addProperty("texture", atlas.texturePath().toString());
            json.addProperty("size", atlas.width());
            if (atlas.offsetX() != 0) {
                json.addProperty("offsetX", atlas.offsetX());
            }
            if (atlas.offsetY() != 0) {
                json.addProperty("offsetY", atlas.offsetY());
            }
        } else if (content instanceof ImageContent image) {
            json.addProperty("type", "image");
            json.addProperty("texture", image.texture().toString());
            json.addProperty("width", image.width());
            json.addProperty("height", image.height());
            if (image.offsetX() != 0) {
                json.addProperty("offsetX", image.offsetX());
            }
            if (image.offsetY() != 0) {
                json.addProperty("offsetY", image.offsetY());
            }
        } else if (content instanceof ChartContent chart) {
            json.addProperty("type", "chart");
            json.addProperty("chartType", chart.type().toString().toLowerCase());
            json.addProperty("width", chart.width());
            json.addProperty("height", chart.height());
            JsonArray entries = new JsonArray();
            for (var entry : chart.entries()) {
                JsonObject entryJson = new JsonObject();
                entryJson.addProperty("label", entry.label());
                entryJson.addProperty("value", entry.valueExpr());
                entryJson.addProperty("color", String.format("#%06X", entry.color() & 0xFFFFFF));
                entries.add(entryJson);
            }
            json.add("entries", entries);
        } else if (content instanceof TypewriterContent typewriter) {
            json.addProperty("type", "typewriter");
            JsonArray lines = new JsonArray();
            for (String line : typewriter.getLines()) {
                lines.add(line);
            }
            json.add("lines", lines);
            json.addProperty("charsPerSecond", typewriter.getCharsPerSecond());
            json.addProperty("loop", typewriter.isLoop());
            if (typewriter.color() != 0xFFFFFF) {
                json.addProperty("color", String.format("#%06X", typewriter.color() & 0xFFFFFF));
            }
            if (typewriter.font() != null) {
                json.addProperty("font", typewriter.font().toString());
            }
        }

        return json;
    }

    /**
     * 解析颜色字符串
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
            case "white" -> 0xFFFFFFFF;
            default -> 0xFFFFFFFF;
        };
    }
}
