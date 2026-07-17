package com.cooobird.datatip.internal.util;

import com.cooobird.datatip.api.session.ItemStackFingerprint;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * 性能优化工具。
 * 提供 tooltip 匹配时使用的轻量查询方法。
 *
 * @author cooobird
 * @since 1.2.0
 */
public final class PerformanceOptimizer {
    private static final ResourceLocation AIR_ID = Objects.requireNonNull(
        ResourceLocation.tryParse("minecraft:air"),
        "Invalid built-in item id"
    );

    private PerformanceOptimizer() {
    }

    /**
     * 获取物品 ID。
     */
    public static ResourceLocation getItemId(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null ? id : AIR_ID;
    }

    /**
     * 获取用于缓存依赖比较的结构化物品指纹。
     */
    public static ItemStackFingerprint getItemFingerprint(ItemStack stack) {
        return ItemStackFingerprint.capture(stack);
    }
}
