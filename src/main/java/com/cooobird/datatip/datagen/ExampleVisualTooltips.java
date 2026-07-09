package com.cooobird.datatip.datagen;

import com.google.gson.JsonObject;

import static com.cooobird.datatip.datagen.TipContentBuilder.*;

/**
 * 视觉内容示例 Tooltip。
 */
final class ExampleVisualTooltips {
    private ExampleVisualTooltips() {
    }

    static void addTo(JsonObject root) {
        root.add("minecraft:wolf_spawn_egg", toJson(vbox(4,
            text("Wolf Spawn Egg", "white"),
            entity("minecraft:wolf", 48),
            text("Can be tamed", "gray")
        )));

        root.add("minecraft:cat_spawn_egg", toJson(vbox(4,
            text("Cat Spawn Egg", "white"),
            entity("minecraft:cat", 48, 0, 8),
            text("Can be tamed", "gray")
        )));

        root.add("minecraft:crafting_table", toJson(vbox(4,
            text("Crafting Table", "white"),
            block("minecraft:crafting_table", 48),
            text("Used for crafting", "gray")
        )));

        root.add("minecraft:furnace", toJson(vbox(4,
            text("Furnace", "white"),
            block("minecraft:furnace", 48, 0, 8),
            text("Smelts items", "gray")
        )));

        root.add("minecraft:apple", toJson(vbox(4,
            text("Apple", "red"),
            atlas("minecraft:apple", 32),
            text("Restores hunger", "gray")
        )));

        root.add("minecraft:golden_apple", toJson(vbox(4,
            text("Golden Apple", "gold"),
            atlas("minecraft:golden_apple", 32, 0, 4),
            text("Restores health", "red")
        )));

        root.add("minecraft:painting", toJson(vbox(4,
            text("Painting", "white"),
            image("minecraft:textures/painting/paintings_kristoffer_zetterstrand.png", 64, 64),
            text("Decorative item", "gray")
        )));

        root.add("minecraft:map", toJson(vbox(4,
            text("Map", "white"),
            image("minecraft:textures/map/map_background.png", 64, 64, 0, 4),
            text("Shows explored area", "gray")
        )));
    }
}
