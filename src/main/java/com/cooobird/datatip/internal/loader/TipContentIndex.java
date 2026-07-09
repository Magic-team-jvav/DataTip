package com.cooobird.datatip.internal.loader;

import com.cooobird.datatip.api.TipContentEntry;
import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.internal.util.PerformanceOptimizer;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Tip 内容索引。
 * 负责精确物品、标签和通配符三种查询方式。
 */
public final class TipContentIndex {
    private final Map<String, List<TipContentEntry>> exactContents = new HashMap<>();
    private final Map<String, List<TipContentEntry>> tagContents = new HashMap<>();
    private final List<WildcardEntry> wildcardContents = new ArrayList<>();

    public void clear() {
        exactContents.clear();
        tagContents.clear();
        wildcardContents.clear();
    }

    public void add(String itemKey, List<TipContentEntry> entries) {
        if (itemKey.startsWith("#")) {
            String tag = itemKey.substring(1);
            tagContents.computeIfAbsent(tag, key -> new ArrayList<>()).addAll(entries);
        } else if (itemKey.contains("*") || itemKey.contains("?")) {
            wildcardContents.add(new WildcardEntry(compileWildcardPattern(itemKey), entries));
        } else {
            exactContents.computeIfAbsent(itemKey, key -> new ArrayList<>()).addAll(entries);
        }
    }

    public List<TipContentEntry> getEntries(String itemId, ItemStack stack) {
        List<TipContentEntry> result = new ArrayList<>();

        List<TipContentEntry> exact = exactContents.get(itemId);
        if (exact != null) {
            addMatchingEntries(result, exact, stack);
        }

        for (WildcardEntry wildcardEntry : wildcardContents) {
            if (wildcardEntry.matches(itemId)) {
                addMatchingEntries(result, wildcardEntry.entries(), stack);
            }
        }

        return result;
    }

    public List<TipContentEntry> getEntriesByTag(String tag, ItemStack stack) {
        List<TipContentEntry> result = new ArrayList<>();
        addMatchingEntries(result, tagContents.getOrDefault(tag, List.of()), stack);
        return result;
    }

    public List<TipContentEntry> find(ItemStack stack) {
        String itemId = PerformanceOptimizer.getItemId(stack).toString();
        List<TipContentEntry> result = getEntries(itemId, stack);
        stack.getTags().forEach(tag ->
            result.addAll(getEntriesByTag(tag.location().toString(), stack)));
        return result;
    }

    public Set<String> getExactItemIds() {
        return Set.copyOf(exactContents.keySet());
    }

    public int exactSize() {
        return exactContents.size();
    }

    public int tagSize() {
        return tagContents.size();
    }

    public int wildcardSize() {
        return wildcardContents.size();
    }

    public static boolean isValidItemKey(String key) {
        return !key.isEmpty() && key.contains(":");
    }

    private static void addMatchingEntries(
        List<TipContentEntry> result,
        List<TipContentEntry> entries,
        ItemStack stack
    ) {
        for (TipContentEntry entry : entries) {
            if (ConditionChecker.checkAll(entry.conditions(), stack)) {
                result.add(entry);
            }
        }
    }

    private static Pattern compileWildcardPattern(String wildcard) {
        StringBuilder regex = new StringBuilder("^");
        for (int i = 0; i < wildcard.length(); i++) {
            char ch = wildcard.charAt(i);
            if (ch == '*') {
                regex.append(".*");
            } else if (ch == '?') {
                regex.append('.');
            } else {
                regex.append(Pattern.quote(String.valueOf(ch)));
            }
        }
        regex.append('$');
        return Pattern.compile(regex.toString());
    }

    private record WildcardEntry(Pattern pattern, List<TipContentEntry> entries) {
        boolean matches(String id) {
            return pattern.matcher(id).matches();
        }
    }
}
