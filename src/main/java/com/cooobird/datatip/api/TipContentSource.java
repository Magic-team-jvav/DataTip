package com.cooobird.datatip.api;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Tooltip 内容来源。
 * <p>
 * 资源包、运行时注册或其他扩展入口都可以实现这个接口，渲染侧只需要按物品查询可显示内容。
 * </p>
 */
@FunctionalInterface
public interface TipContentSource {
    List<TipContentEntry> find(ItemStack stack);
}
