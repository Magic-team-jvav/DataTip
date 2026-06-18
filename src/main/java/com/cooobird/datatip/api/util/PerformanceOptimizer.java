package com.cooobird.datatip.api.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能优化器。
 * 提供缓存优化、静态内容缓存、条件检查缓存等功能。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class PerformanceOptimizer {

    private static final Logger LOGGER = LogUtils.getLogger();

    // 物品 ID 缓存（避免重复查询注册表）
    private static final Map<ItemStack, String> itemIdCache = new ConcurrentHashMap<>();

    // 条件检查缓存
    private static final Map<String, Boolean> conditionCache = new ConcurrentHashMap<>();
    private static final long CONDITION_CACHE_EXPIRY = 1000; // 1秒
    private static final Map<String, Long> conditionCacheTimestamps = new ConcurrentHashMap<>();

    // 静态内容缓存（渲染结果）
    private static final Map<String, CachedRenderResult> renderCache = new ConcurrentHashMap<>();
    private static final long RENDER_CACHE_EXPIRY = 100; // 100ms

    /**
     * Cached render result.
     */
    public record CachedRenderResult(
        int width,
        int height,
        long timestamp
    ) {
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > RENDER_CACHE_EXPIRY;
        }
    }

    /**
     * 获取物品 ID（带缓存）。
     */
    public static String getItemId(ItemStack stack) {
        return itemIdCache.computeIfAbsent(stack, s -> 
            BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
    }

    /**
     * 检查条件（带缓存）。
     */
    public static boolean checkConditionCached(String conditionType, Object value, ItemStack stack) {
        String cacheKey = conditionType + ":" + value + ":" + stack.hashCode();

        // 检查缓存
        Boolean cached = conditionCache.get(cacheKey);
        Long timestamp = conditionCacheTimestamps.get(cacheKey);

        if (cached != null && timestamp != null) {
            if (System.currentTimeMillis() - timestamp < CONDITION_CACHE_EXPIRY) {
                return cached;
            }
        }

        // 计算结果（这里需要实际的条件检查逻辑）
        boolean result = false; // 实际实现会调用 ConditionChecker

        // 缓存结果
        conditionCache.put(cacheKey, result);
        conditionCacheTimestamps.put(cacheKey, System.currentTimeMillis());

        return result;
    }

    /**
     * 获取缓存的渲染结果。
     */
    public static CachedRenderResult getCachedRenderResult(String key) {
        CachedRenderResult result = renderCache.get(key);
        if (result != null && !result.isExpired()) {
            return result;
        }
        return null;
    }

    /**
     * 缓存渲染结果。
     */
    public static void cacheRenderResult(String key, int width, int height) {
        renderCache.put(key, new CachedRenderResult(width, height, System.currentTimeMillis()));
    }

    /**
     * 清除所有缓存。
     */
    public static void clearAllCaches() {
        itemIdCache.clear();
        conditionCache.clear();
        conditionCacheTimestamps.clear();
        renderCache.clear();
        LOGGER.info("All performance caches cleared");
    }

    /**
     * 清除过期缓存。
     */
    public static void clearExpiredCaches() {
        long now = System.currentTimeMillis();

        // 清除过期的条件缓存
        conditionCacheTimestamps.entrySet().removeIf(entry ->
            now - entry.getValue() > CONDITION_CACHE_EXPIRY
        );
        conditionCache.keySet().removeIf(key -> !conditionCacheTimestamps.containsKey(key));

        // 清除过期的渲染缓存
        renderCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * 获取缓存统计。
     */
    public static Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("itemIdCache", itemIdCache.size());
        stats.put("conditionCache", conditionCache.size());
        stats.put("renderCache", renderCache.size());
        return stats;
    }

    /**
     * 预热缓存（加载常用物品）。
     */
    public static void warmupCache(List<ItemStack> commonItems) {
        LOGGER.info("Warming up cache with {} items...", commonItems.size());
        for (ItemStack stack : commonItems) {
            getItemId(stack);
        }
        LOGGER.info("Cache warmup complete");
    }
}
