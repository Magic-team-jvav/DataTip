package com.cooobird.datatip.api;

import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.internal.loader.TipContentIndex;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 运行时 Tooltip 内容注册表。
 * <p>
 * 用于让其他代码在资源包 JSON 之外注册 DataTip 内容。注册后的内容会和资源包内容一起参与 tooltip
 * 查找，但不会写回资源包，也不会跨游戏进程持久化。
 * </p>
 */
public final class TipRuntimeContentRegistry {
    private static final Map<String, List<TipContentEntry>> REGISTERED_BY_KEY = new HashMap<>();
    private static final TipContentIndex CONTENT_INDEX = new TipContentIndex();
    private static final TipContentSource SOURCE = TipRuntimeContentRegistry::find;

    private TipRuntimeContentRegistry() {
    }

    public static synchronized void register(String itemKey, TipContent content) {
        register(itemKey, TipContentEntry.of(content));
    }

    public static synchronized void register(String itemKey, List<TipContent> contents) {
        validateItemKey(itemKey);
        Objects.requireNonNull(contents, "contents");
        List<TipContentEntry> entries = contents.stream()
            .map(TipContentEntry::of)
            .toList();
        REGISTERED_BY_KEY.computeIfAbsent(itemKey, key -> new ArrayList<>()).addAll(entries);
        CONTENT_INDEX.add(itemKey, entries);
    }

    public static synchronized void register(
        String itemKey,
        TipContent content,
        List<ConditionChecker.Condition> conditions,
        boolean shift,
        boolean prepend
    ) {
        register(itemKey, new TipContentEntry(content, conditions, shift, prepend));
    }

    public static synchronized void register(String itemKey, TipContentEntry entry) {
        validateItemKey(itemKey);
        Objects.requireNonNull(entry, "entry");
        REGISTERED_BY_KEY.computeIfAbsent(itemKey, key -> new ArrayList<>()).add(entry);
        CONTENT_INDEX.add(itemKey, List.of(entry));
    }

    public static synchronized void clear() {
        REGISTERED_BY_KEY.clear();
        CONTENT_INDEX.clear();
    }

    public static synchronized void clearNamespace(String namespace) {
        REGISTERED_BY_KEY.keySet().removeIf(key -> belongsToNamespace(key, namespace));
        rebuildIndex();
    }

    public static synchronized List<TipContentEntry> find(ItemStack stack) {
        return CONTENT_INDEX.find(stack);
    }

    public static TipContentSource source() {
        return SOURCE;
    }

    public static boolean isValidItemKey(String itemKey) {
        if (itemKey == null || itemKey.isEmpty()) {
            return false;
        }
        return TipContentIndex.isValidItemKey(itemKey);
    }

    private static void validateItemKey(String itemKey) {
        if (!isValidItemKey(itemKey)) {
            throw new IllegalArgumentException(
                "Invalid item key: " + itemKey + ". Expected item ID, tag (#), or wildcard (*, ?)");
        }
    }

    private static void rebuildIndex() {
        CONTENT_INDEX.clear();
        for (Map.Entry<String, List<TipContentEntry>> entry : REGISTERED_BY_KEY.entrySet()) {
            CONTENT_INDEX.add(entry.getKey(), entry.getValue());
        }
    }

    private static boolean belongsToNamespace(String itemKey, String namespace) {
        if (namespace == null || namespace.isBlank()) return false;
        String rawKey = itemKey.startsWith("#") ? itemKey.substring(1) : itemKey;
        int separator = rawKey.indexOf(':');
        return separator > 0 && rawKey.substring(0, separator).equals(namespace);
    }

}
