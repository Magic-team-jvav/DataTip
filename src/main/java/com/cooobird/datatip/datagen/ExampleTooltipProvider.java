package com.cooobird.datatip.datagen;

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
        Path directory = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
            .resolve("minecraft/datatip");
        CompletableFuture<?> showcase = DataProvider.saveStable(
            cache, ExampleShowcaseTooltips.create(), directory.resolve("showcase.json"));
        CompletableFuture<?> conditions = DataProvider.saveStable(
            cache, ExampleConditionTooltips.create(), directory.resolve("all_conditions.json"));
        return CompletableFuture.allOf(showcase, conditions);
    }

    @Override
    public String getName() {
        return "Complete DataTip Examples";
    }
}
