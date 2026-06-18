package com.cooobird.datatip.datagen;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.cooobird.datatip.datagen.TipContentBuilder.*;

/**
 * 示例 Tooltip 数据生成器。
 * 展示如何使用新版本 API 生成 datatip.json。
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
    @NotNull
    public CompletableFuture<?> run(@NotNull CachedOutput cache) {
        Path path = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
            .resolve("minecraft/datatip/datatip.json");

        JsonObject root = new JsonObject();

        // 简单文本数组（老版本格式兼容）
        root.add("minecraft:diamond", toJson(vbox(2,
            text("A shiny diamond"),
            text("Worth a fortune")
        )));

        // 带颜色的文本
        root.add("minecraft:emerald", toJson(vbox(2,
            text("Emerald", "green"),
            text("Used for trading", "gray")
        )));

        // 多语言
        Map<String, String> netheriteLang = new LinkedHashMap<>();
        netheriteLang.put("zh_cn", "下界合金锭");
        netheriteLang.put("en_us", "Netherite Ingot");
        root.add("minecraft:netherite_ingot", toJson(vbox(2,
            langText(netheriteLang, "gold"),
            text("不会被熔岩烧毁 / Immune to lava", "dark_red")
        )));

        // 物品 + 文本
        root.add("minecraft:diamond_sword", toJson(hbox(8,
            item("minecraft:diamond_sword"),
            vbox(2,
                text("Diamond Sword", "aqua"),
                text("削铁如泥", "gold")
            )
        )));

        // 进度条
        root.add("minecraft:diamond_pickaxe", toJson(vbox(4,
            text("Diamond Pickaxe", "aqua"),
            progress(0.75f, 100, "75% 耐久")
        )));

        // 轮播容器
        root.add("minecraft:golden_apple", toJson(carousel(3,
            vbox(2, text("Golden Apple", "gold"), text("恢复生命", "red")),
            vbox(2, text("金苹果", "gold"), text("稀有食物", "light_purple"))
        )));

        // 打字机效果
        root.add("minecraft:nether_star", toJson(vbox(4,
            text("Nether Star", "light_purple"),
            typewriter("Boss 掉落", "用于合成信标")
        )));

        // 间距和分割线
        root.add("minecraft:stone", toJson(vbox(4,
            text("Stone", "gray"),
            divider("dark_gray"),
            text("基础方块", "white")
        )));

        return DataProvider.saveStable(cache,
            JsonParser.parseString(new GsonBuilder().setPrettyPrinting().create().toJson(root)), path);
    }

    @Override
    public String getName() {
        return "Example Tooltips";
    }
}
