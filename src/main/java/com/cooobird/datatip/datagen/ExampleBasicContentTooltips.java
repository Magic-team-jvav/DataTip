package com.cooobird.datatip.datagen;

import com.google.gson.JsonObject;

import static com.cooobird.datatip.datagen.TipContentBuilder.*;

/**
 * 基础内容示例 Tooltip。
 */
final class ExampleBasicContentTooltips {
    private ExampleBasicContentTooltips() {
    }

    static void addTo(JsonObject root) {
        root.add("minecraft:diamond_sword", toJson(hbox(8,
            item("minecraft:diamond_sword"),
            vbox(2,
                text("Diamond Sword", "aqua"),
                text("削铁如泥", "gold")
            )
        )));

        root.add("minecraft:iron_sword", toJson(hbox(8,
            item("minecraft:iron_sword", "铁剑"),
            vbox(2,
                text("Iron Sword", "white"),
                text("基础武器", "gray")
            )
        )));

        root.add("minecraft:diamond_pickaxe", toJson(vbox(4,
            text("Diamond Pickaxe", "aqua"),
            progress(0.75f, 100, "75% 耐久")
        )));

        root.add("minecraft:iron_pickaxe", toJson(vbox(4,
            text("Iron Pickaxe", "white"),
            progress(0.5f, 100)
        )));

        root.add("minecraft:stone", toJson(vbox(4,
            text("Stone", "gray"),
            spacer(8),
            text("8px spacer above", "white")
        )));

        root.add("minecraft:cobblestone", toJson(vbox(4,
            text("Cobblestone", "gray"),
            divider("dark_gray"),
            text("基础方块", "white")
        )));

        root.add("minecraft:granite", toJson(vbox(4,
            text("Granite", "gray"),
            divider("dark_gray", "dashed"),
            text("装饰方块", "white")
        )));

        root.add("minecraft:diorite", toJson(vbox(4,
            text("Diorite", "gray"),
            divider("dark_gray", "dotted"),
            text("装饰方块", "white")
        )));
    }
}
