package com.cooobird.datatip.datagen;

import com.cooobird.datatip.api.node.TipModifiers;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import static com.cooobird.datatip.datagen.TipContentBuilder.*;

/**
 * 使用 TipContentBuilder 覆盖全部内容类型及代表性布局、文本和动画组合。
 */
final class ExampleShowcaseTooltips {
    private ExampleShowcaseTooltips() {
    }

    static JsonObject create() {
        JsonObject root = new JsonObject();

        root.add("minecraft:diamond", toJson(vbox(3, 2, "center",
            langText(languages("DataTip 全内容展示", "DataTip Complete Showcase"), "aqua", true, false, false, false),
            text("Bold · Italic · Underline · Strike", "white", true, true, true, true),
            text("Custom font", "gray", "minecraft:uniform"),
            divider("#35C9E8", 2, 160, 1, 1, "solid", "fixed"),
            hbox(7, 1, "center",
                item("minecraft:diamond", 16, 32, true, true, true,
                    languages("物品渲染", "Item rendering"), "#55FFFF", 1, 1),
                vbox(1,
                    langText(languages("名称：{item_name}", "Name: {item_name}"), "white"),
                    text("ID: {item_id}", "gray", false, true, false, false),
                    text("Count: {count}/{max_stack_size}", "yellow")
                )
            ),
            progress(0.78f, 160, 7, "#35E8A0", "#252A34", "#B8FFE2", "#11151B",
                "gradient", languages("渐变 · 居中", "Gradient · Center"), "center", 0),
            centeredAligned(atlas("minecraft:diamond", 16)),
            rightAlignedContent(text("Aligned wrapper", "dark_gray")),
            text("按住 Shift / Hold Shift", "green", false, false, false, false, true)
        )));

        root.add("minecraft:diamond_sword", entry(vbox(2,
            langText(languages("NBT 变量", "NBT variables"), "yellow", true, false, false, false),
            text("Name: {nbt:display.Name}", "white"),
            text("Damage: {nbt:Damage}/{max_durability}", "gray"),
            text("Quality: {nbt:quality}", "gold")
        ), Map.of(), false, true));

        root.add("minecraft:diamond_pickaxe", toJson(vbox(3,
            progress(0.25f, 150, 6, "#55FF55", "#222222", "#88FF88", "#111111",
                "flat", languages("平面 · 左", "Flat · Left"), "left", 0),
            progress(0.5f, 150, 6, "#55FF55", "#222222", "#88FF88", "#111111",
                "gradient", languages("渐变 · 居中", "Gradient · Center"), "center", 0),
            progress(0.75f, 150, 6, "#55FF55", "#222222", "#88FF88", "#111111",
                "segmented", languages("分段 · 右", "Segmented · Right"), "right", 0),
            progress(0.9f, 150, 6, "#55FFFF", "#222222", "#DDFFFF", "#111111",
                "animated", languages("流光动画", "Animated"), "center", 2)
        )));

        root.add("minecraft:clock", toJson(carousel(4, "fade",
            langText(languages("Fade · 文本", "Fade · Text"), "gold", true, false, false, false),
            item("minecraft:clock", 1, 28, false, false, true,
                languages("Fade · 物品", "Fade · Item"), "#FFAA00", 0, 0),
            image("minecraft:textures/item/clock_00.png", 16, 16, 0, 0, 16, 16, 1.5f, 0, 0),
            block("minecraft:redstone_block", 34, 1.2f, true,
                languages("Fade · 方块", "Fade · Block"), 0, 0)
        )));

        root.add("minecraft:compass", toJson(carousel(4, "slide",
            entity("minecraft:pig", 38, -1.5f, true,
                languages("Slide · 实体", "Slide · Entity"), 0, 2),
            atlas("minecraft:compass", 32, languages("Slide · 图集", "Slide · Atlas"), 2, 0)
        )));

        root.add("minecraft:recovery_compass", toJson(carousel(4, "none",
            text("None · Frame A", "aqua"),
            block("minecraft:iron_block", 30, 0.0f, false,
                languages("静止方块", "Static block"), 0, 0)
        )));

        root.add("minecraft:enchanted_book", toJson(typewriter(
            languageLines(List.of("多语言打字机", "循环、样式与行高"),
                List.of("Localized typewriter", "Loop, style and line height")),
            8, 2, true, "light_purple", "minecraft:uniform",
            true, true, true, false, "center", false, 14, false
        )));

        root.add("minecraft:emerald", toJson(chart("bar", 150, 48,
            languages("柱状图", "Bar chart"), true, true,
            "#55FFFF", "#AAAAAA", "#FFFFFF", "#888888",
            chartEntry(languages("数量", "Count"), "{count}", "#55FF55"),
            chartEntry("Fixed", 18, "#55FFFF")
        )));

        root.add("minecraft:ender_eye", toJson(chart("pie", 96, 96,
            languages("饼图", "Pie chart"), true, false,
            "#FFFFFF", "#AAAAAA", "#FFFFFF", "#888888",
            chartEntry("A", 55, "#55FF55"), chartEntry("B", 30, "#FF5555"),
            chartEntry("C", 15, "#AA55FF")
        )));

        root.add("minecraft:wheat_seeds", toJson(chart("line", 150, 52,
            languages("折线图", "Line chart"), false, true,
            "#FFFFFF", "#AAAAAA", "#FFFFFF", "#888888",
            chartEntry("1", 4, "#55FF55"), chartEntry("2", 12, "#55FFFF"),
            chartEntry("3", 8, "#FFAA00")
        )));

        root.add("minecraft:brick", toJson(vbox(2,
            divider("#FF5555", 1, 0, 1, 1, "solid", "fill"),
            divider("#55FF55", 1, 120, 1, 1, "dashed", "fixed"),
            divider("#55FFFF", 1, 80, 1, 1, "dotted", "centered"),
            spacer(8), rightAligned("Spacer: 8px", "gray")
        )));

        root.add("minecraft:filled_map", toJson(image(
            "minecraft:textures/map/map_background.png", 64, 64,
            0, 0, 128, 128, 0.75f, 2, 2)));
        root.add("minecraft:nether_star", toJson(stack(
            2,
            "center",
            "center",
            modifiers(
                block("minecraft:obsidian", 42),
                false,
                -20,
                TipModifiers.SelfAlignment.CENTER
            ),
            modifiers(
                item("minecraft:nether_star"),
                false,
                0,
                TipModifiers.SelfAlignment.CENTER
            ),
            modifiers(
                translate("datatip.example.stack_foreground"),
                true,
                20,
                TipModifiers.SelfAlignment.CENTER
            )
        )));
        root.add("#minecraft:swords", toJson(langText(languages("标签匹配", "Tag match"), "aqua")));
        root.add("minecraft:*_axe", toJson(langText(languages("通配符匹配", "Wildcard match"), "yellow")));
        return root;
    }
}
