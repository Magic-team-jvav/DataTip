package com.cooobird.datatip.internal.variable;

import com.cooobird.datatip.internal.util.PerformanceOptimizer;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 变量解析结果缓存。
 */
public final class VariableCache {
    private static final int MAX_CACHE_SIZE = 1024;
    private static final Map<String, CachedResult> CACHE = Collections.synchronizedMap(
        new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CachedResult> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        });
    private static final long CACHE_EXPIRY_ITEM = 200;      // 物品属性
    private static final long CACHE_EXPIRY_PLAYER = 50;     // 玩家状态
    private static final long CACHE_EXPIRY_POSITION = 100;  // 坐标/游戏状态

    private VariableCache() {
    }

    public static String get(String text, ItemStack stack) {
        String key = cacheKey(text, stack);
        synchronized (CACHE) {
            CachedResult cached = CACHE.get(key);
            if (cached != null && !cached.isExpired()) {
                return cached.result();
            }
            if (cached != null) {
                CACHE.remove(key);
            }
        }
        return null;
    }

    public static void put(String text, ItemStack stack, String result) {
        synchronized (CACHE) {
            trimExpired();
            CACHE.put(cacheKey(text, stack),
                new CachedResult(result, System.currentTimeMillis(), getCacheExpiryForText(text)));
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

    private static String cacheKey(String text, ItemStack stack) {
        return text + ":" + PerformanceOptimizer.getItemSignature(stack);
    }

    private static long getCacheExpiryForText(String text) {
        if (text.contains("player_") || text.contains("health_bar")) {
            return CACHE_EXPIRY_PLAYER;
        }
        if (text.contains("game_time") || text.contains("is_day") ||
            text.contains("is_raining") || text.contains("is_thundering")) {
            return CACHE_EXPIRY_POSITION;
        }
        return CACHE_EXPIRY_ITEM;
    }

    private record CachedResult(String result, long timestamp, long expiryMs) {
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > expiryMs;
        }
    }
}
