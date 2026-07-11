package com.cooobird.datatip.datagen;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import static com.cooobird.datatip.datagen.TipContentBuilder.entry;
import static com.cooobird.datatip.datagen.TipContentBuilder.text;

/**
 * 使用 TipContentBuilder 覆盖全部内置条件及其常用值形态。
 */
final class ExampleConditionTooltips {
    private ExampleConditionTooltips() {
    }

    static JsonObject create() {
        JsonObject root = new JsonObject();
        add(root, "minecraft:grass_block", "dimension", Map.of("dimension", "minecraft:overworld"));
        add(root, "minecraft:oak_sapling", "biome array",
            Map.of("biome", List.of("minecraft:plains", "minecraft:forest")));
        add(root, "minecraft:map", "holding array",
            Map.of("holding", List.of("minecraft:compass", "minecraft:recovery_compass")));
        add(root, "minecraft:feather", "sneaking true", Map.of("sneaking", true));
        add(root, "minecraft:flint", "sneaking false", Map.of("sneaking", false));
        add(root, "minecraft:command_block", "creative true", Map.of("creative", true));
        add(root, "minecraft:wooden_sword", "survival true", Map.of("survival", true));
        add(root, "minecraft:golden_apple", "health percent", Map.of("health", "50%"));
        add(root, "minecraft:bread", "hunger number", Map.of("hunger", 15));
        add(root, "minecraft:experience_bottle", "experience", Map.of("experience", 5));
        add(root, "minecraft:bookshelf", "level alias", Map.of("level", 5));
        add(root, "minecraft:sunflower", "time day", Map.of("time", "day"));
        add(root, "minecraft:clock", "time number", Map.of("time", 6000));
        add(root, "minecraft:lightning_rod", "weather clear", Map.of("weather", "clear"));
        add(root, "minecraft:torch", "light bright", Map.of("light", "bright"));
        add(root, "minecraft:glowstone", "light number", Map.of("light", 8));
        add(root, "minecraft:scaffolding", "altitude expression", Map.of("altitude", ">=64"));
        add(root, "minecraft:stick", "enchanted false", Map.of("enchanted", false));
        add(root, "minecraft:diamond_pickaxe", "damage", Map.of("damage", 100));
        add(root, "minecraft:cobblestone", "count", Map.of("count", 16));
        add(root, "minecraft:name_tag", "component", Map.of("component", "custom_name"));
        add(root, "minecraft:paper", "custom_data path", Map.of("custom_data", "quality"));
        add(root, "minecraft:book", "custom_data values",
            Map.of("custom_data", Map.of("quality", "rare", "score", 5)));
        add(root, "minecraft:iron_sword", "item_tag", Map.of("item_tag", "minecraft:swords"));
        add(root, "minecraft:diamond_block", "combined conditions",
            Map.of("dimension", "minecraft:overworld", "creative", false, "health", 1, "count", 1));
        root.add("minecraft:obsidian", entry(text("shift with condition", "dark_purple"),
            Map.of("dimension", "minecraft:overworld"), true, false));
        return root;
    }

    private static void add(JsonObject root, String item, String label, Map<String, ?> conditions) {
        root.add(item, entry(text(label, "green"), conditions));
    }
}
