package com.cooobird.datatip.datagen;

import com.google.gson.JsonObject;

import static com.cooobird.datatip.datagen.TipContentBuilder.*;

/**
 * 布局与 Shift 展开示例 Tooltip。
 */
final class ExampleLayoutTooltips {
    private ExampleLayoutTooltips() {
    }

    static void addTo(JsonObject root) {
        root.add("minecraft:emerald_block", toJson(vbox(4,
            centeredAligned(text("Centered Item", "green")),
            text("Emerald Block", "green")
        )));

        root.add("minecraft:diamond_block", toJson(vbox(4,
            rightAlignedContent(text("Right Aligned", "aqua")),
            text("Diamond Block", "aqua")
        )));

        root.add("minecraft:chest", toJson(vbox(4,
            text("Chest", "white"),
            text("Stores items", "gray"),
            text("27 slots", "yellow")
        )));

        root.add("minecraft:barrel", toJson(hbox(8,
            text("Barrel", "white"),
            text("Stores items", "gray"),
            text("27 slots", "yellow")
        )));

        root.add("minecraft:ender_pearl", toJson(
            text("Ender Pearl — 按住 Shift 显示", "#00AAAA", false, false, false, false, true)
        ));

        root.add("minecraft:obsidian", toJson(vbox(2,
            text("Obsidian — 全部 shift", "dark_purple", true, false, false, false, true),
            text("硬度: 50", "gray", false, false, false, false, true),
            text("用于下界传送门", "gray", false, false, false, false, true)
        )));

        root.add("minecraft:crying_obsidian", toJson(vbox(2,
            text("Crying Obsidian", "dark_purple", true, false, false, false),
            text("用于合成重生锚", "gray", false, false, false, false, true),
            text("可在遗迹中找到", "gray", false, false, false, false, true)
        )));

        root.add("minecraft:enchanted_book", toJson(vbox(4,
            text("Enchanted Book", "light_purple", true, false, false, false),
            divider("light_purple", "dashed"),
            hbox(8,
                item("minecraft:enchanted_book"),
                vbox(2,
                    text("Contains enchantment", "white"),
                    progress(0.8f, 80, "80%"),
                    text("Right-click to apply", "gray", false, true, false, false)
                )
            ),
            spacer(4),
            typewriter(0xFFAAAAAA, 3, 1, false, "魔法的力量", "蕴含其中")
        )));
    }
}
