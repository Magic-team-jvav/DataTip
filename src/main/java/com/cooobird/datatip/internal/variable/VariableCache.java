package com.cooobird.datatip.internal.variable;

import com.cooobird.datatip.api.session.ItemStackFingerprint;
import com.cooobird.datatip.api.session.TooltipInvalidation;
import com.cooobird.datatip.api.session.TooltipSession;
import com.cooobird.datatip.api.session.TooltipSessionContext;

/**
 * 当前悬停会话内的物品静态变量缓存。
 */
public final class VariableCache {
    private VariableCache() {
    }

    public static String get(String text, ItemStackFingerprint item) {
        if (!VariableRegistry.isItemStatic(text)) return null;
        TooltipSession session = TooltipSessionContext.current();
        return session != null
            ? session.cached(
            TooltipInvalidation.VARIABLES,
            new CacheKey(text, item),
            String.class
        )
            : null;
    }

    public static void put(
        String text,
        ItemStackFingerprint item,
        String result
    ) {
        if (!VariableRegistry.isItemStatic(text)) return;
        TooltipSession session = TooltipSessionContext.current();
        if (session != null) {
            session.cache(
                TooltipInvalidation.VARIABLES,
                new CacheKey(text, item),
                result
            );
        }
    }

    public static void clear() {
        TooltipSession session = TooltipSessionContext.current();
        if (session != null) {
            session.invalidate(TooltipInvalidation.VARIABLES);
        }
    }

    private record CacheKey(String text, ItemStackFingerprint item) {
    }
}
