package com.cooobird.datatip.api.condition;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 读取指定物品栈上的兼容数据值。
 */
@FunctionalInterface
public interface ComponentValueReader {
    @Nullable
    String read(ItemStack stack);
}
