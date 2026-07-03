package com.cooobird.datatip.datagen;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.cooobird.datatip.datagen.TipContentBuilder.*;

/**
 * 示例 Tooltip 数据生成器。
 * 展示所有内容类型的所有组合。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class ExampleTooltipProvider implements DataProvider {
    private final PackOutput output;

    public ExampleTooltipProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path path = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
            .resolve("minecraft/datatip/datatip.json");

        JsonObject root = new JsonObject();

        // 基础文本
        root.add("minecraft:diamond", toJson(vbox(2,
            text("A shiny diamond"),
            text("Worth a fortune")
        )));

        // 带颜色
        root.add("minecraft:emerald", toJson(vbox(2,
            text("Emerald", "green"),
            text("Used for trading", "gray")
        )));

        // 带样式
        root.add("minecraft:iron_ingot", toJson(vbox(2,
            text("Iron Ingot", "white", true, false, false, false),
            text("基础材料", "gray", false, true, false, false),
            text("已过时", "dark_gray", false, false, false, true)
        )));

        // 带自定义字体
        root.add("minecraft:gold_ingot", toJson(vbox(2,
            text("Gold Ingot", "gold", "minecraft:alt"),
            text("贵重金属", "yellow")
        )));

        // 居中
        root.add("minecraft:netherite_ingot", toJson(vbox(2,
            centered("Netherite Ingot", "gold"),
            text("Immune to lava", "dark_red")
        )));

        // 居中带样式
        root.add("minecraft:netherite_scrap", toJson(vbox(2,
            centered("Netherite Scrap", "gold", true, false, false, false),
            text("From ancient debris", "gray")
        )));

        // 右对齐
        root.add("minecraft:ancient_debris", toJson(vbox(2,
            rightAligned("Ancient Debris", "gold"),
            text("Found in Nether", "dark_red")
        )));

        // 多语言
        Map<String, String> netheriteLang = new LinkedHashMap<>();
        netheriteLang.put("zh_cn", "下界合金锭");
        netheriteLang.put("en_us", "Netherite Ingot");
        root.add("minecraft:netherite_ingot", toJson(vbox(2,
            langText(netheriteLang, "gold"),
            text("不会被熔岩烧毁", "dark_red")
        )));

        // 多语言带样式
        Map<String, String> styledLang = new LinkedHashMap<>();
        styledLang.put("zh_cn", "传说武器");
        styledLang.put("en_us", "Legendary Weapon");
        root.add("minecraft:netherite_sword", toJson(vbox(2,
            langText(styledLang, "light_purple", true, false, false, false),
            text("最强近战武器", "gold")
        )));

        // 基础物品
        root.add("minecraft:diamond_sword", toJson(hbox(8,
            item("minecraft:diamond_sword"),
            vbox(2,
                text("Diamond Sword", "aqua"),
                text("削铁如泥", "gold")
            )
        )));

        // 物品带标签
        root.add("minecraft:iron_sword", toJson(hbox(8,
            item("minecraft:iron_sword", "铁剑"),
            vbox(2,
                text("Iron Sword", "white"),
                text("基础武器", "gray")
            )
        )));

        // 基础进度条
        root.add("minecraft:diamond_pickaxe", toJson(vbox(4,
            text("Diamond Pickaxe", "aqua"),
            progress(0.75f, 100, "75% 耐久")
        )));

        // 进度条无标签
        root.add("minecraft:iron_pickaxe", toJson(vbox(4,
            text("Iron Pickaxe", "white"),
            progress(0.5f, 100)
        )));

        root.add("minecraft:stone", toJson(vbox(4,
            text("Stone", "gray"),
            spacer(8),
            text("8px spacer above", "white")
        )));

        // 基础分割线
        root.add("minecraft:cobblestone", toJson(vbox(4,
            text("Cobblestone", "gray"),
            divider("dark_gray"),
            text("基础方块", "white")
        )));

        // 虚线分割线
        root.add("minecraft:granite", toJson(vbox(4,
            text("Granite", "gray"),
            divider("dark_gray", "dashed"),
            text("装饰方块", "white")
        )));

        // 点线分割线
        root.add("minecraft:diorite", toJson(vbox(4,
            text("Diorite", "gray"),
            divider("dark_gray", "dotted"),
            text("装饰方块", "white")
        )));

        // 基础实体
        root.add("minecraft:wolf_spawn_egg", toJson(vbox(4,
            text("Wolf Spawn Egg", "white"),
            entity("minecraft:wolf", 48),
            text("Can be tamed", "gray")
        )));

        // 实体带偏移
        root.add("minecraft:cat_spawn_egg", toJson(vbox(4,
            text("Cat Spawn Egg", "white"),
            entity("minecraft:cat", 48, 0, 8),
            text("Can be tamed", "gray")
        )));

        // 基础方块
        root.add("minecraft:crafting_table", toJson(vbox(4,
            text("Crafting Table", "white"),
            block("minecraft:crafting_table", 48),
            text("Used for crafting", "gray")
        )));

        // 方块带偏移
        root.add("minecraft:furnace", toJson(vbox(4,
            text("Furnace", "white"),
            block("minecraft:furnace", 48, 0, 8),
            text("Smelts items", "gray")
        )));

        // 基础纹理
        root.add("minecraft:apple", toJson(vbox(4,
            text("Apple", "red"),
            atlas("minecraft:apple", 32),
            text("Restores hunger", "gray")
        )));

        // 纹理带偏移
        root.add("minecraft:golden_apple", toJson(vbox(4,
            text("Golden Apple", "gold"),
            atlas("minecraft:golden_apple", 32, 0, 4),
            text("Restores health", "red")
        )));

        // 基础图片
        root.add("minecraft:painting", toJson(vbox(4,
            text("Painting", "white"),
            image("minecraft:textures/painting/paintings_kristoffer_zetterstrand.png", 64, 64),
            text("Decorative item", "gray")
        )));

        // 图片带偏移
        root.add("minecraft:map", toJson(vbox(4,
            text("Map", "white"),
            image("minecraft:textures/map/map_background.png", 64, 64, 0, 4),
            text("Shows explored area", "gray")
        )));

        // 基础轮播图
        root.add("minecraft:enchanted_golden_apple", toJson(carousel(3,
            vbox(2, text("Golden Apple", "gold"), text("恢复生命", "red")),
            vbox(2, text("金苹果", "gold"), text("稀有食物", "light_purple")),
            vbox(2, text("Enchanted Apple", "gold"), text("Absorption", "yellow"))
        )));

        // 基础打字机
        root.add("minecraft:nether_star", toJson(vbox(4,
            text("Nether Star", "light_purple"),
            typewriter("Boss 掉落", "用于合成信标")
        )));

        // 打字机带颜色
        root.add("minecraft:end_crystal", toJson(vbox(4,
            text("End Crystal", "light_purple"),
            typewriter("生成于末地", "可被引爆")
        )));

        // 打字机全参数
        root.add("minecraft:beacon", toJson(vbox(4,
            text("Beacon", "aqua"),
            typewriter(0xFF55FFFF, 5, 2, true, "提供增益效果", "需要金字塔基座")
        )));

        // 打字机 loop:true（悬停重播）
        root.add("minecraft:beacon", toJson(vbox(4,
            text("Beacon — loop:true", "aqua"),
            typewriter(0xFFAAAAAA, 6, 1, true, "每次悬停都从头播放", "loop:true + gap检测")
        )));

        // 打字机 + shift（展开重播）
        root.add("minecraft:end_crystal", toJson(vbox(4,
            text("End Crystal — shift + loop", "light_purple"),
            typewriter(0xFFAAAAAA, 8, 1, true, true, "按 Shift 展开", "每次展开都重播")
        )));

        // 打字机多语言
        root.add("minecraft:end_stone", toJson(vbox(4,
            text("End Stone", "yellow"),
            typewriter("生成于末地", "可被引爆")
        )));

        // 打字机带样式
        root.add("minecraft:purpur_block", toJson(vbox(4,
            text("Purpur Block", "light_purple"),
            typewriter("生成于末地", "可被引爆")
        )));

        // 柱状图
        root.add("minecraft:compass", toJson(vbox(4,
            text("Compass", "red"),
            chart("bar", 100, 60)
        )));

        // 饼图
        root.add("minecraft:redstone", toJson(vbox(4,
            text("Redstone", "red"),
            chart("pie", 80, 80)
        )));

        // 折线图
        root.add("minecraft:wheat_seeds", toJson(vbox(4,
            text("Wheat Seeds", "green"),
            chart("line", 100, 60)
        )));

        // 图表带标题和数据
        root.add("minecraft:coal", toJson(vbox(4,
            text("Coal", "gray"),
            chart("bar", 100, 60, "Fuel Values",
                chartEntry("Coal", 80, "#555555"),
                chartEntry("Charcoal", 80, "#AAAAAA"),
                chartEntry("Blaze Rod", 120, "#FF5555")
            )
        )));

        // 居中对齐
        root.add("minecraft:emerald_block", toJson(vbox(4,
            centeredAligned(text("Centered Item", "green")),
            text("Emerald Block", "green")
        )));

        // 右对齐
        root.add("minecraft:diamond_block", toJson(vbox(4,
            rightAlignedContent(text("Right Aligned", "aqua")),
            text("Diamond Block", "aqua")
        )));

        // 垂直布局
        root.add("minecraft:chest", toJson(vbox(4,
            text("Chest", "white"),
            text("Stores items", "gray"),
            text("27 slots", "yellow")
        )));

        // 水平布局
        root.add("minecraft:barrel", toJson(hbox(8,
            text("Barrel", "white"),
            text("Stores items", "gray"),
            text("27 slots", "yellow")
        )));

        // shift 文本（单行）
        root.add("minecraft:ender_pearl", toJson(
            text("Ender Pearl — 按住 Shift 显示", "#00AAAA", false, false, false, false, true)
        ));

        // VBox 全部 shift（合并提示）
        root.add("minecraft:obsidian", toJson(vbox(2,
            text("Obsidian — 全部 shift", "dark_purple", true, false, false, false, true),
            text("硬度: 50", "gray", false, false, false, false, true),
            text("用于下界传送门", "gray", false, false, false, false, true)
        )));

        // VBox 混合 shift（非shift常显 + shift合并提示）
        root.add("minecraft:crying_obsidian", toJson(vbox(2,
            text("Crying Obsidian", "dark_purple", true, false, false, false),
            text("用于合成重生锚", "gray", false, false, false, false, true),
            text("可在遗迹中找到", "gray", false, false, false, false, true)
        )));

        // 复杂布局
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

        return DataProvider.saveStable(cache,
            JsonParser.parseString(new GsonBuilder().setPrettyPrinting().create().toJson(root)), path);
    }

    @Override
    public String getName() {
        return "Example Tooltips";
    }
}
