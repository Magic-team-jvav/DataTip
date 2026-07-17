package com.cooobird.datatip.api.condition;

import com.cooobird.datatip.api.util.VariableResolver;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 兼容数据读取器注册表。
 * <p>
 * 1.20.1 没有新版数据组件系统，其他代码可按名称注册等价读取逻辑。
 * </p>
 */
public final class ComponentReaderRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, ComponentValueReader> READERS = new ConcurrentHashMap<>();
    private static final Set<String> REPORTED_FAILURES = ConcurrentHashMap.newKeySet();
    private static final AtomicLong REVISION = new AtomicLong();

    private ComponentReaderRegistry() {
    }

    public static void register(String componentName, ComponentValueReader reader) {
        String normalizedName = normalizeName(componentName);
        if (READERS.putIfAbsent(normalizedName, requireReader(reader)) != null) {
            throw new IllegalArgumentException("Component reader already registered: " + normalizedName);
        }
        REVISION.incrementAndGet();
        invalidateCaches();
    }

    public static void registerOrReplace(String componentName, ComponentValueReader reader) {
        String normalizedName = normalizeName(componentName);
        ComponentValueReader replacement = requireReader(reader);
        ComponentValueReader previous = READERS.put(normalizedName, replacement);
        REPORTED_FAILURES.remove(normalizedName);
        if (previous != replacement) {
            REVISION.incrementAndGet();
            invalidateCaches();
        }
    }

    public static boolean unregister(String componentName) {
        String normalizedName = normalizeName(componentName);
        boolean removed = READERS.remove(normalizedName) != null;
        REPORTED_FAILURES.remove(normalizedName);
        if (removed) {
            REVISION.incrementAndGet();
            invalidateCaches();
        }
        return removed;
    }

    public static Set<String> registeredNames() {
        return Set.copyOf(READERS.keySet());
    }

    public static void clear() {
        if (READERS.isEmpty() && REPORTED_FAILURES.isEmpty()) return;
        READERS.clear();
        REPORTED_FAILURES.clear();
        REVISION.incrementAndGet();
        invalidateCaches();
    }

    public static long getRevision() {
        return REVISION.get();
    }

    @Nullable
    public static String read(ItemStack stack, String componentName) {
        String normalizedName = normalizeName(componentName);
        ComponentValueReader reader = READERS.get(normalizedName);
        if (reader == null) return null;
        try {
            String value = reader.read(stack);
            REPORTED_FAILURES.remove(normalizedName);
            return value;
        } catch (RuntimeException e) {
            if (REPORTED_FAILURES.add(normalizedName)) {
                LOGGER.warn("Failed to read DataTip component '{}'; repeated failures will be suppressed",
                    normalizedName, e);
            }
            return null;
        }
    }

    public static String normalizeName(String componentName) {
        if (componentName == null || componentName.isBlank()) {
            throw new IllegalArgumentException("Component name must not be blank");
        }
        String name = componentName.trim();
        if (name.startsWith("minecraft:")) {
            return name.substring("minecraft:".length());
        }
        return name;
    }

    private static ComponentValueReader requireReader(ComponentValueReader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("Component reader must not be null");
        }
        return reader;
    }

    private static void invalidateCaches() {
        ConditionChecker.clearCache();
        VariableResolver.clearCache();
    }
}
