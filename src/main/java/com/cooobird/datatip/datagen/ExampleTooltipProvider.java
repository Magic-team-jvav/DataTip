package com.cooobird.datatip.datagen;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

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
        ExampleTextTooltips.addTo(root);
        ExampleBasicContentTooltips.addTo(root);
        ExampleVisualTooltips.addTo(root);
        ExampleDynamicTooltips.addTo(root);
        ExampleLayoutTooltips.addTo(root);

        return DataProvider.saveStable(cache,
            JsonParser.parseString(new GsonBuilder().setPrettyPrinting().create().toJson(root)), path);
    }

    @Override
    public String getName() {
        return "Example Tooltips";
    }
}
