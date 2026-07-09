package com.cooobird.datatip.event;

import com.cooobird.datatip.api.TipContentEntry;
import com.cooobird.datatip.api.TipContentSource;
import com.cooobird.datatip.api.TipRuntimeContentRegistry;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Tooltip 内容查找工具。
 */
final class TipRenderContentLookup {
    private TipRenderContentLookup() {
    }

    static List<TipContentEntry> find(TipContentSource contentSource, ItemStack stack) {
        List<TipContentEntry> entries = new ArrayList<>();
        entries.addAll(TipRuntimeContentRegistry.source().find(stack));

        if (contentSource == null) {
            return entries;
        }

        entries.addAll(contentSource.find(stack));
        return entries;
    }
}
