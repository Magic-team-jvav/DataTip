package com.cooobird.datatip.datagen;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.cooobird.datatip.datagen.TipContentBuilder.*;

/**
 * 文本相关示例 Tooltip。
 */
final class ExampleTextTooltips {
    private ExampleTextTooltips() {
    }

    static void addTo(JsonObject root) {
        root.add("minecraft:diamond", toJson(vbox(2,
            text("A shiny diamond"),
            text("Worth a fortune")
        )));

        root.add("minecraft:emerald", toJson(vbox(2,
            text("Emerald", "green"),
            text("Used for trading", "gray")
        )));

        root.add("minecraft:iron_ingot", toJson(vbox(2,
            text("Iron Ingot", "white", true, false, false, false),
            text("基础材料", "gray", false, true, false, false),
            text("已过时", "dark_gray", false, false, false, true)
        )));

        root.add("minecraft:gold_ingot", toJson(vbox(2,
            text("Gold Ingot", "gold", "minecraft:alt"),
            text("贵重金属", "yellow")
        )));

        root.add("minecraft:netherite_ingot", toJson(vbox(2,
            centered("Netherite Ingot", "gold"),
            text("Immune to lava", "dark_red")
        )));

        root.add("minecraft:netherite_scrap", toJson(vbox(2,
            centered("Netherite Scrap", "gold", true, false, false, false),
            text("From ancient debris", "gray")
        )));

        root.add("minecraft:ancient_debris", toJson(vbox(2,
            rightAligned("Ancient Debris", "gold"),
            text("Found in Nether", "dark_red")
        )));

        Map<String, String> netheriteLang = new LinkedHashMap<>();
        netheriteLang.put("zh_cn", "下界合金锭");
        netheriteLang.put("en_us", "Netherite Ingot");
        root.add("minecraft:netherite_ingot", toJson(vbox(2,
            langText(netheriteLang, "gold"),
            text("不会被熔岩烧毁", "dark_red")
        )));

        Map<String, String> styledLang = new LinkedHashMap<>();
        styledLang.put("zh_cn", "传说武器");
        styledLang.put("en_us", "Legendary Weapon");
        root.add("minecraft:netherite_sword", toJson(vbox(2,
            langText(styledLang, "light_purple", true, false, false, false),
            text("最强近战武器", "gold")
        )));
    }
}
