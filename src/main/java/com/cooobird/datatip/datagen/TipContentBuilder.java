package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.content.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
            if (textContent.align() == TextContent.TextAlign.CENTER) {
                json.addProperty("align", "center");
            } else if (textContent.align() == TextContent.TextAlign.RIGHT) {
                json.addProperty("align", "right");
            }
        } else if (content instanceof SpacerContent(int height)) {
            json.addProperty("type", "spacer");
            json.addProperty("height", height);
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
