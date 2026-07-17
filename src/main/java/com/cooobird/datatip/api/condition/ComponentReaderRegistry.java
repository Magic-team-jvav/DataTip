package com.cooobird.datatip.api.condition;

import com.cooobird.datatip.api.util.VariableResolver;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据组件读取器注册表。
 * <p>
 * 其他代码可以在这里注册 DataTip 不内置支持的数据组件读取逻辑。
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

    public static void register(DataComponentType<?> componentType, ComponentValueReader reader) {
        register(componentName(componentType), reader);
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

    public static void registerOrReplace(DataComponentType<?> componentType, ComponentValueReader reader) {
        registerOrReplace(componentName(componentType), reader);
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

    public static boolean unregister(DataComponentType<?> componentType) {
        return unregister(componentName(componentType));
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

    public static String componentName(DataComponentType<?> componentType) {
        ResourceLocation id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType);
        if (id == null) {
            throw new IllegalArgumentException("Data component type is not registered");
        }
        return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
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
