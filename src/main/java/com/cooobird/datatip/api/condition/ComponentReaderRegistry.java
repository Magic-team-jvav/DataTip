package com.cooobird.datatip.api.condition;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据组件读取器注册表。
 * <p>
 * 其他代码可以在这里注册 DataTip 不内置支持的数据组件读取逻辑。
 * </p>
 */
public final class ComponentReaderRegistry {
    private static final Map<String, ComponentValueReader> READERS = new ConcurrentHashMap<>();

    private ComponentReaderRegistry() {
    }

    public static void register(String componentName, ComponentValueReader reader) {
        String normalizedName = normalizeName(componentName);
        if (READERS.putIfAbsent(normalizedName, requireReader(reader)) != null) {
            throw new IllegalArgumentException("Component reader already registered: " + normalizedName);
        }
    }

    public static void register(DataComponentType<?> componentType, ComponentValueReader reader) {
        register(componentName(componentType), reader);
    }

    public static void registerOrReplace(String componentName, ComponentValueReader reader) {
        READERS.put(normalizeName(componentName), requireReader(reader));
    }

    public static void registerOrReplace(DataComponentType<?> componentType, ComponentValueReader reader) {
        registerOrReplace(componentName(componentType), reader);
    }

    public static boolean unregister(String componentName) {
        return READERS.remove(normalizeName(componentName)) != null;
    }

    public static boolean unregister(DataComponentType<?> componentType) {
        return unregister(componentName(componentType));
    }

    public static Set<String> registeredNames() {
        return Set.copyOf(READERS.keySet());
    }

    public static void clear() {
        READERS.clear();
    }

    @Nullable
    public static String read(ItemStack stack, String componentName) {
        ComponentValueReader reader = READERS.get(normalizeName(componentName));
        return reader != null ? reader.read(stack) : null;
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
}
