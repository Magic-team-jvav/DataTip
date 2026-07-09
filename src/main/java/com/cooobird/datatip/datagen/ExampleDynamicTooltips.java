package com.cooobird.datatip.datagen;

import com.google.gson.JsonObject;

import static com.cooobird.datatip.datagen.TipContentBuilder.*;

/**
 * 动态内容示例 Tooltip。
 */
final class ExampleDynamicTooltips {
    private ExampleDynamicTooltips() {
    }

    static void addTo(JsonObject root) {
        root.add("minecraft:enchanted_golden_apple", toJson(carousel(3,
            vbox(2, text("Golden Apple", "gold"), text("恢复生命", "red")),
            vbox(2, text("金苹果", "gold"), text("稀有食物", "light_purple")),
            vbox(2, text("Enchanted Apple", "gold"), text("Absorption", "yellow"))
        )));

        root.add("minecraft:nether_star", toJson(vbox(4,
            text("Nether Star", "light_purple"),
            typewriter("Boss 掉落", "用于合成信标")
        )));

        root.add("minecraft:end_crystal", toJson(vbox(4,
            text("End Crystal", "light_purple"),
            typewriter("生成于末地", "可被引爆")
        )));

        root.add("minecraft:beacon", toJson(vbox(4,
            text("Beacon", "aqua"),
            typewriter(0xFF55FFFF, 5, 2, true, "提供增益效果", "需要金字塔基座")
        )));

        root.add("minecraft:beacon", toJson(vbox(4,
            text("Beacon — loop:true", "aqua"),
            typewriter(0xFFAAAAAA, 6, 1, true, "每次悬停都从头播放", "loop:true + gap检测")
        )));

        root.add("minecraft:end_crystal", toJson(vbox(4,
            text("End Crystal — shift + loop", "light_purple"),
            typewriter(0xFFAAAAAA, 8, 1, true, true, "按 Shift 展开", "每次展开都重播")
        )));

        root.add("minecraft:end_stone", toJson(vbox(4,
            text("End Stone", "yellow"),
            typewriter("生成于末地", "可被引爆")
        )));

        root.add("minecraft:purpur_block", toJson(vbox(4,
            text("Purpur Block", "light_purple"),
            typewriter("生成于末地", "可被引爆")
        )));

        root.add("minecraft:compass", toJson(vbox(4,
            text("Compass", "red"),
            chart("bar", 100, 60)
        )));

        root.add("minecraft:redstone", toJson(vbox(4,
            text("Redstone", "red"),
            chart("pie", 80, 80)
        )));

        root.add("minecraft:wheat_seeds", toJson(vbox(4,
            text("Wheat Seeds", "green"),
            chart("line", 100, 60)
        )));

        root.add("minecraft:coal", toJson(vbox(4,
            text("Coal", "gray"),
            chart("bar", 100, 60, "Fuel Values",
                chartEntry("Coal", 80, "#555555"),
                chartEntry("Charcoal", 80, "#AAAAAA"),
                chartEntry("Blaze Rod", 120, "#FF5555")
            )
        )));
    }
}
