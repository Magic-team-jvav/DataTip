package com.cooobird.datatip.internal.loader;

import com.cooobird.datatip.api.ParseContext;
import com.cooobird.datatip.api.TipContent;
import com.cooobird.datatip.api.TipContentEntry;
import com.cooobird.datatip.api.TipContentSource;
import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.api.util.VariableResolver;
import com.cooobird.datatip.internal.legacy.LegacyFormatConverter;
import com.cooobird.datatip.internal.util.ReloadOptimizer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.*;

/**
 * 资源包 Tooltip 内容加载器。
 * <p>
 * 从资源包的 datatip/ 目录加载 JSON，并维护一个可增量更新的内容索引。
 * </p>
 */
public class TipContentLoader extends SimpleJsonResourceReloadListener implements TipContentSource {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private final TipContentIndex contentIndex = new TipContentIndex();
    private final TipContentEntryParser entryParser = new TipContentEntryParser();
    private final ReloadOptimizer reloadOptimizer = new ReloadOptimizer();
    private final Map<ResourceLocation, LoadedDatatipFile> loadedFiles = new HashMap<>();

    public TipContentLoader() {
        super(GSON, "datatip");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elements, ResourceManager resourceManager, ProfilerFiller profiler) {
        ReloadOptimizer.ReloadPlan reloadPlan = reloadOptimizer.createPlan(elements);
        if (!reloadPlan.requiresReload()) {
            LOGGER.info("Skipped datatip reload: {} unchanged files", reloadPlan.unchangedFiles());
            return;
        }

        ConditionChecker.clearCache();
        VariableResolver.clearCache();

        ParseContext context = new ParseContext();
        for (ResourceLocation deletedFile : reloadPlan.deletedFiles()) {
            loadedFiles.remove(deletedFile);
        }

        for (ReloadOptimizer.ChangedFile changedFile : reloadPlan.changedFiles()) {
            ResourceLocation location = changedFile.location();
            JsonElement element = elements.get(location);
            loadedFiles.put(location, parseFile(location, element, context));
        }

        rebuildIndex();
        reloadOptimizer.commit(reloadPlan, elements);
        logReloadResult(reloadPlan, context);
    }

    private LoadedDatatipFile parseFile(ResourceLocation location, JsonElement element, ParseContext context) {
        if (element == null || !element.isJsonObject()) {
            LOGGER.warn("Invalid datatip at {}: expected object", location);
            return LoadedDatatipFile.empty();
        }

        JsonObject json = element.getAsJsonObject();
        boolean legacyConverted = false;
        if (LegacyFormatConverter.isLegacyFormat(json)) {
            LOGGER.info("Converting legacy format at {}", location);
            json = LegacyFormatConverter.convert(json, location);
            legacyConverted = true;
        }

        Map<String, List<TipContentEntry>> entriesByItemKey = new LinkedHashMap<>();
        int totalEntries = 0;

        for (var itemEntry : json.entrySet()) {
            String itemKey = itemEntry.getKey();
            JsonElement itemElement = itemEntry.getValue();

            if (itemKey.startsWith("_") || itemKey.equals("$schema")) {
                continue;
            }

            if (!TipContentIndex.isValidItemKey(itemKey)) {
                LOGGER.warn("Invalid key format: '{}'. Expected item ID, tag (#), or wildcard (*, ?)", itemKey);
                continue;
            }

            List<ConditionChecker.Condition> conditions = entryParser.parseConditions(itemElement);
            boolean shift = entryParser.parseBoolean(itemElement, "shift", false);
            boolean prepend = entryParser.parseBoolean(itemElement, "prepend", false);

            List<TipContent> contents = entryParser.parseItemContent(itemKey, itemElement, context);
            if (contents.isEmpty()) {
                continue;
            }

            List<TipContentEntry> entries = contents.stream()
                .map(c -> new TipContentEntry(c, conditions, shift, prepend))
                .toList();

            entriesByItemKey.put(itemKey, entries);
            totalEntries++;
        }

        return new LoadedDatatipFile(entriesByItemKey, totalEntries, legacyConverted);
    }

    private void rebuildIndex() {
        contentIndex.clear();
        for (LoadedDatatipFile loadedFile : loadedFiles.values()) {
            for (var entry : loadedFile.entriesByItemKey().entrySet()) {
                contentIndex.add(entry.getKey(), entry.getValue());
            }
        }
    }

    private void logReloadResult(ReloadOptimizer.ReloadPlan reloadPlan, ParseContext context) {
        LOGGER.info("Loaded {} datatip entries (exact: {}, tag: {}, wildcard: {}, legacy converted: {})",
            totalEntries(), contentIndex.exactSize(), contentIndex.tagSize(), contentIndex.wildcardSize(),
            legacyConvertedFiles());

        LOGGER.info("Datatip reload plan: added {}, updated {}, deleted {}, unchanged {}",
            reloadPlan.addedFiles(), reloadPlan.updatedFiles(), reloadPlan.deletedFiles().size(),
            reloadPlan.unchangedFiles());

        if (context.hasWarnings()) {
            LOGGER.warn("Datatip parse warnings ({}):", context.getWarnings().size());
            for (String warning : context.getWarnings()) {
                LOGGER.warn("  - {}", warning);
            }
        }
    }

    private int totalEntries() {
        int total = 0;
        for (LoadedDatatipFile loadedFile : loadedFiles.values()) {
            total += loadedFile.totalEntries();
        }
        return total;
    }

    private int legacyConvertedFiles() {
        int total = 0;
        for (LoadedDatatipFile loadedFile : loadedFiles.values()) {
            if (loadedFile.legacyConverted()) {
                total++;
            }
        }
        return total;
    }

    @Override
    public List<TipContentEntry> find(ItemStack stack) {
        return contentIndex.find(stack);
    }

    public List<TipContentEntry> getEntries(String itemId, ItemStack stack) {
        return contentIndex.getEntries(itemId, stack);
    }

    public List<TipContentEntry> getEntriesByTag(String tag, ItemStack stack) {
        return contentIndex.getEntriesByTag(tag, stack);
    }

    public Set<String> getExactItemIds() {
        return contentIndex.getExactItemIds();
    }

    private record LoadedDatatipFile(
        Map<String, List<TipContentEntry>> entriesByItemKey,
        int totalEntries,
        boolean legacyConverted
    ) {
        static LoadedDatatipFile empty() {
            return new LoadedDatatipFile(Map.of(), 0, false);
        }
    }
}
