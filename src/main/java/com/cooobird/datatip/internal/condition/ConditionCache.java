package com.cooobird.datatip.internal.condition;

import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.internal.util.PerformanceOptimizer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 条件检查结果缓存。
 */
public final class ConditionCache {
    private static final int MAX_CACHE_SIZE = 1024;
    private static final Map<String, CachedResult> CACHE = Collections.synchronizedMap(
        new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CachedResult> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        });
    private static final long CACHE_EXPIRY_MS = 1000; // 1 秒
    private static final Set<String> CACHEABLE_ITEM_CONDITIONS = Set.of(
        "enchanted", "damage", "count", "component", "custom_data", "item_tag"
    );

    private ConditionCache() {
    }

    public static Boolean get(ConditionChecker.Condition condition, ItemStack stack, Player player, Level level) {
        if (!isCacheable(condition)) return null;
        String cacheKey = buildCacheKey(condition, stack, player, level);
        synchronized (CACHE) {
            CachedResult cached = CACHE.get(cacheKey);
            if (cached != null && !cached.isExpired()) return cached.result();
            if (cached != null) {
                CACHE.remove(cacheKey);
            }
        }
        return null;
    }

    public static void put(ConditionChecker.Condition condition, ItemStack stack, Player player, Level level, boolean result) {
        if (!isCacheable(condition)) return;
        synchronized (CACHE) {
            trimExpired();
            CACHE.put(buildCacheKey(condition, stack, player, level),
                new CachedResult(result, System.currentTimeMillis()));
        }
    }

    public static void clear() {
        synchronized (CACHE) {
            CACHE.clear();
        }
    }

    private static void trimExpired() {
        CACHE.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private static String buildCacheKey(
        ConditionChecker.Condition condition,
        ItemStack stack,
        Player player,
        Level level
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(condition.type()).append(':').append(condition.value());

        sb.append(':').append(PerformanceOptimizer.getItemSignature(stack));

        return sb.toString();
    }

    private static boolean isCacheable(ConditionChecker.Condition condition) {
        return condition != null && CACHEABLE_ITEM_CONDITIONS.contains(condition.type());
    }

    private record CachedResult(boolean result, long timestamp) {
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
    }
}
