package com.cooobird.datatip.internal.condition;

import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.api.session.ItemStackFingerprint;
import com.cooobird.datatip.api.session.TooltipInvalidation;
import com.cooobird.datatip.api.session.TooltipSession;
import com.cooobird.datatip.api.session.TooltipSessionContext;

import java.util.Set;

/**
 * 当前悬停会话内、只由物品指纹决定的条件缓存。
 */
public final class ConditionCache {
    private static final Set<String> CACHEABLE_ITEM_CONDITIONS = Set.of(
        "enchanted", "damage", "count", "component", "custom_data", "item_tag"
    );

    private ConditionCache() {
    }

    public static Boolean get(
        ConditionChecker.Condition condition,
        ItemStackFingerprint item
    ) {
        if (!isCacheable(condition)) return null;
        TooltipSession session = TooltipSessionContext.current();
        return session != null
            ? session.cached(
            TooltipInvalidation.CONDITIONS,
            new CacheKey(condition, item),
            Boolean.class
        )
            : null;
    }

    public static void put(
        ConditionChecker.Condition condition,
        ItemStackFingerprint item,
        boolean result
    ) {
        if (!isCacheable(condition)) return;
        TooltipSession session = TooltipSessionContext.current();
        if (session != null) {
            session.cache(
                TooltipInvalidation.CONDITIONS,
                new CacheKey(condition, item),
                result
            );
        }
    }

    public static void clear() {
        TooltipSession session = TooltipSessionContext.current();
        if (session != null) {
            session.invalidate(TooltipInvalidation.CONDITIONS);
        }
    }

    private static boolean isCacheable(ConditionChecker.Condition condition) {
        return condition != null && CACHEABLE_ITEM_CONDITIONS.contains(condition.type());
    }

    private record CacheKey(
        ConditionChecker.Condition condition,
        ItemStackFingerprint item
    ) {
    }
}
