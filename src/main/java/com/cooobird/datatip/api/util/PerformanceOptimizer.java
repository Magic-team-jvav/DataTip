package com.cooobird.datatip.api.util;

import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能优化器。
 * 提供物品 ID 缓存等基础优化。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class PerformanceOptimizer {

    // 物品 ID 缓存
    private static final Map<ItemStack, String> itemIdCache = new ConcurrentHashMap<>();

    /**
     * 获取物品 ID（带缓存）。
     */
    public static String getItemId(ItemStack stack) {
        return itemIdCache.computeIfAbsent(stack, s -> {
            var key = s.getItemHolder().getKey();
            return key != null ? key.location().toString() : "minecraft:air";
        });
    }

    /**
     * 清除所有缓存（在资源重载时调用）。
     */
    public static void clearAllCaches() {
        itemIdCache.clear();
    }
}
