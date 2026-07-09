package com.cooobird.datatip.internal.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 性能优化工具。
 * 提供 tooltip 匹配时使用的轻量查询方法。
 *
 * @author cooobird
 * @since 1.2.0
 */
public final class PerformanceOptimizer {
    private static final ResourceLocation AIR_ID = ResourceLocation.parse("minecraft:air");

    private PerformanceOptimizer() {
    }

    /**
     * 获取物品 ID。
     */
    public static ResourceLocation getItemId(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null ? key : AIR_ID;
    }

    /**
     * 获取用于短期缓存的物品语义签名。
     */
    public static String getItemSignature(ItemStack stack) {
        return getItemId(stack) + "|count=" + stack.getCount()
            + "|damage=" + stack.getDamageValue()
            + "|tag=" + stack.getTag();
    }
}
