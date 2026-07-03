package com.cooobird.datatip.api.loader;

import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipContentRegistry;
import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.api.content.TextContent;
import com.cooobird.datatip.api.content.VBoxContent;
import com.cooobird.datatip.api.util.LegacyFormatConverter;
import com.cooobird.datatip.api.util.PerformanceOptimizer;
import com.cooobird.datatip.api.util.ReloadOptimizer;
import com.cooobird.datatip.config.DatatipConfig;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.*;

/**
 * TipContent 加载器。
 * 从资源包加载 datatip/ 目录下的 JSON 文件。
 * <p>
 * 支持两种 JSON 格式：
 * <p>
 * 1. 老版本格式（兼容）：
 * <pre>{@code
 * {
 *   "minecraft:diamond": ["A shiny diamond", "Worth a fortune"],
 *   "minecraft:diamond_sword": {
 *     "text": {"zh_cn": ["削铁如泥"], "en_us": ["Cuts through iron"]},
 *     "color": "gold",
 *     "shift": true
 *   }
 * }
 * }</pre>
 * <p>
 * 2. 新版本格式：
 * <pre>{@code
 * {
 *   "minecraft:diamond": {
 *     "type": "vbox",
 *     "children": [...]
 *   }
 * }
 * }</pre>
 *
 * @author cooobird
 * @since 1.2.0
 */
public class TipContentLoader extends SimpleJsonResourceReloadListener {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // 按物品 ID 索引的内容（包含条件）
    private final Map<String, List<ContentEntry>> exactContents = new HashMap<>();
    // 按标签索引的内容
    private final Map<String, List<ContentEntry>> tagContents = new HashMap<>();
    // 通配符内容
    private final List<WildcardEntry> wildcardContents = new ArrayList<>();

    /**
     * 内容条目（包含条件和显示选项）。
     */
    public record ContentEntry(
        TipContent content,
        List<ConditionChecker.Condition> conditions,
        boolean shift,      // 需要按住 Shift 才显示
        boolean prepend     // 置顶显示
    ) {
    }

    /**
     * 通配符条目。
     */
    private record WildcardEntry(String pattern, List<ContentEntry> entries) {
        boolean matches(String id) {
            return id.matches(pattern.replace("*", ".*").replace("?", "."));
        }
    }

    /**
     * 创建加载器。
     */
    public TipContentLoader() {
        super(GSON, "datatip");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resourceManager, ProfilerFiller profiler) {
        exactContents.clear();
        tagContents.clear();
        wildcardContents.clear();

        // 资源重载时清除性能缓存
        PerformanceOptimizer.clearAllCaches();

        ParseContext context = new ParseContext();
        int totalEntries = 0;
        int legacyConverted = 0;

        for (var entry : elements.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonElement element = entry.getValue();

            if (!element.isJsonObject()) {
                LOGGER.warn("Invalid datatip at {}: expected object", location);
                continue;
            }

            JsonObject json = element.getAsJsonObject();

            // 检测并转换老版本格式
            if (LegacyFormatConverter.isLegacyFormat(json)) {
                LOGGER.info("Converting legacy format at {}", location);
                json = LegacyFormatConverter.convert(json, location);
                legacyConverted++;
            }

            // 记录热重载优化
            ReloadOptimizer.hasChanged(location, json.toString());

            // 解析每个物品键
            for (var itemEntry : json.entrySet()) {
                String itemKey = itemEntry.getKey();
                JsonElement itemElement = itemEntry.getValue();

                // 跳过注释字段
                if (itemKey.startsWith("_")) {
                    continue;
                }

                // 验证键格式
                if (!isValidItemKey(itemKey)) {
                    LOGGER.warn("Invalid key format: '{}'. Expected item ID, tag (#), or wildcard (*, ?)", itemKey);
                    continue;
                }

                // 解析条件
                List<ConditionChecker.Condition> conditions = parseConditions(itemElement);

                // 解析显示选项
                boolean shift = parseBoolean(itemElement, "shift", false);
                boolean prepend = parseBoolean(itemElement, "prepend", false);

                // 转换为 TipContent
                List<TipContent> contents = parseItemContent(itemKey, itemElement, context);
                if (contents.isEmpty()) {
                    continue;
                }

                // 创建内容条目
                List<ContentEntry> entries = contents.stream()
                    .map(c -> new ContentEntry(c, conditions, shift, prepend))
                    .toList();

                // 根据键类型分类存储
                if (itemKey.startsWith("#")) {
                    // 标签
                    String tag = itemKey.substring(1);
                    tagContents.computeIfAbsent(tag, k -> new ArrayList<>()).addAll(entries);
                } else if (itemKey.contains("*") || itemKey.contains("?")) {
                    // 通配符
                    wildcardContents.add(new WildcardEntry(itemKey, entries));
                } else {
                    // 精确匹配
                    exactContents.computeIfAbsent(itemKey, k -> new ArrayList<>()).addAll(entries);
                }

                totalEntries++;
            }
        }

        LOGGER.info("Loaded {} datatip entries (exact: {}, tag: {}, wildcard: {}, legacy converted: {})",
            totalEntries, exactContents.size(), tagContents.size(), wildcardContents.size(), legacyConverted);

        // 输出解析警告
        if (context.hasWarnings()) {
            LOGGER.warn("Datatip parse warnings ({}):", context.getWarnings().size());
            for (String warning : context.getWarnings()) {
                LOGGER.warn("  - {}", warning);
            }
        }

        // 记录热重载优化摘要
        if (ReloadOptimizer.hasChanges()) {
            LOGGER.info("Reload summary: {}", ReloadOptimizer.getUpdateSummary());
        }
    }

    /**
     * 解析物品内容。
     * 支持老版本和新版本格式。
     */
    private List<TipContent> parseItemContent(String itemKey, JsonElement element, ParseContext context) {
        List<TipContent> result = new ArrayList<>();

        if (element.isJsonArray()) {
            // 老版本格式：字符串数组
            // "minecraft:diamond": ["Line 1", "Line 2"]
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                if (item.isJsonPrimitive()) {
                    String text = item.getAsString();
                    result.add(TextContent.of(text));
                }
            }
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            if (obj.has("type")) {
                // 新版本格式：包含 type 字段
                // "minecraft:diamond": {"type": "vbox", "children": [...]}
                TipContent content = TipContentRegistry.parse(obj, context);
                if (content != null) {
                    result.add(content);
                } else {
                    LOGGER.warn("Failed to parse content for '{}'", itemKey);
                }
            } else if (obj.has("text")) {
                // 老版本格式：包含 text 字段
                // "minecraft:diamond": {"text": {"zh_cn": [...], "en_us": [...]}, "color": "gold"}
                result.add(parseLegacyFormat(obj));
            }
        } else if (element.isJsonPrimitive()) {
            // 老版本格式：单个字符串
            // "minecraft:diamond": "A shiny diamond"
            result.add(TextContent.of(element.getAsString()));
        }

        return result;
    }

    /**
     * 解析老版本格式。
     * 将老版本格式转换为新的 TipContent。
     */
    private TipContent parseLegacyFormat(JsonObject json) {
        VBoxContent vbox = VBoxContent.create();

        // 获取颜色
        int color = DatatipConfig.DEFAULT_COLOR.get();
        if (json.has("color")) {
            String colorStr = json.get("color").getAsString();
            color = parseColor(colorStr);
        }

        // 获取文本
        JsonElement textElement = json.get("text");
        if (textElement.isJsonArray()) {
            // 简单数组格式
            JsonArray array = textElement.getAsJsonArray();
            for (JsonElement item : array) {
                if (item.isJsonPrimitive()) {
                    vbox.addChild(TextContent.of(item.getAsString(), color));
                }
            }
        } else if (textElement.isJsonObject()) {
            // 多语言格式
            JsonObject textObj = textElement.getAsJsonObject();
            // 使用第一个语言
            for (var langEntry : textObj.entrySet()) {
                JsonArray lines = langEntry.getValue().getAsJsonArray();
                for (JsonElement line : lines) {
                    if (line.isJsonPrimitive()) {
                        vbox.addChild(TextContent.of(line.getAsString(), color));
                    }
                }
                break; // 只使用第一个语言
            }
        }

        return vbox;
    }

    /**
     * 解析颜色字符串。
     */
    private int parseColor(String colorStr) {
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
            // NeoForge 扩展颜色
            case "light_blue" -> 0xFF5555FF;
            case "light_green" -> 0xFF55FF55;
            case "light_red" -> 0xFFFF5555;
            case "pink" -> 0xFFFFAACC;
            case "cyan" -> 0xFF00FFFF;
            case "magenta" -> 0xFFFF00FF;
            case "orange" -> 0xFFFFAA00;
            case "lime" -> 0xFFAAFF00;
            case "brown" -> 0xFFAA5500;
            default -> 0xFFFFFFFF;
        };
    }

    /**
     * 获取指定物品的内容条目（检查条件）。
     *
     * @param itemId 物品 ID（如 "minecraft:diamond"）
     * @param stack  物品栈（用于条件检查和变量解析）
     * @return 内容条目列表，如果没有则返回空列表
     */
    public List<ContentEntry> getEntries(String itemId, ItemStack stack) {
        List<ContentEntry> result = new ArrayList<>();

        // 精确匹配
        List<ContentEntry> exact = exactContents.get(itemId);
        if (exact != null) {
            for (ContentEntry entry : exact) {
                if (ConditionChecker.checkAll(entry.conditions(), stack)) {
                    result.add(entry);
                }
            }
        }

        // 通配符匹配
        for (WildcardEntry wildcardEntry : wildcardContents) {
            if (wildcardEntry.matches(itemId)) {
                for (ContentEntry entry : wildcardEntry.entries()) {
                    if (ConditionChecker.checkAll(entry.conditions(), stack)) {
                        result.add(entry);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 获取指定标签的内容条目（检查条件）。
     *
     * @param tag   标签名（如 "minecraft:swords"）
     * @param stack 物品栈（用于条件检查）
     * @return 内容条目列表，如果没有则返回空列表
     */
    public List<ContentEntry> getEntriesByTag(String tag, ItemStack stack) {
        List<ContentEntry> result = new ArrayList<>();
        List<ContentEntry> entries = tagContents.getOrDefault(tag, List.of());

        for (ContentEntry entry : entries) {
            if (ConditionChecker.checkAll(entry.conditions(), stack)) {
                result.add(entry);
            }
        }

        return result;
    }

    /**
     * 获取所有已加载的精确物品 ID（用于日志统计）。
     */
    public Set<String> getExactItemIds() {
        return exactContents.keySet();
    }

    /**
     * 验证物品键格式。
     */
    private boolean isValidItemKey(String key) {
        if (key.isEmpty()) return false;

        // 标签格式
        if (key.startsWith("#")) {
            return key.contains(":");
        }

        // 通配符格式
        if (key.contains("*") || key.contains("?")) {
            return key.contains(":");
        }

        // 精确匹配格式
        return key.contains(":");
    }

    /**
     * 解析条件列表。
     */
    private List<ConditionChecker.Condition> parseConditions(JsonElement element) {
        List<ConditionChecker.Condition> conditions = new ArrayList<>();

        if (!element.isJsonObject()) {
            return conditions;
        }

        JsonObject obj = element.getAsJsonObject();
        if (!obj.has("conditions") || !obj.get("conditions").isJsonObject()) {
            return conditions;
        }

        JsonObject conditionsObj = obj.getAsJsonObject("conditions");
        for (var entry : conditionsObj.entrySet()) {
            String type = entry.getKey();
            Object value = parseValue(entry.getValue());
            conditions.add(new ConditionChecker.Condition(type, value));
        }

        return conditions;
    }

    /**
     * 解析 JSON 值为 Java 对象。
     */
    private Object parseValue(JsonElement element) {
        if (element.isJsonPrimitive()) {
            var prim = element.getAsJsonPrimitive();
            if (prim.isBoolean()) return prim.getAsBoolean();
            if (prim.isNumber()) return prim.getAsNumber();
            return prim.getAsString();
        } else if (element.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement item : element.getAsJsonArray()) {
                list.add(parseValue(item));
            }
            return list;
        }
        return element.toString();
    }

    /**
     * 解析布尔值。
     */
    private boolean parseBoolean(JsonElement element, String key, boolean defaultValue) {
        if (!element.isJsonObject()) {
            return defaultValue;
        }

        JsonObject obj = element.getAsJsonObject();
        if (!obj.has(key)) {
            return defaultValue;
        }

        JsonElement val = obj.get(key);
        if (val.isJsonPrimitive() && val.getAsJsonPrimitive().isBoolean()) {
            return val.getAsBoolean();
        }

        return defaultValue;
    }

}
