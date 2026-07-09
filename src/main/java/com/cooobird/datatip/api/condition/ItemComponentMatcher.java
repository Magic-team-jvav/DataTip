package com.cooobird.datatip.api.condition;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * 物品数据组件读取门面。
 * <p>
 * Minecraft 1.21.1 的物品数据是 Data Components。这里保留常用入口，
 * 具体读取逻辑拆分到内置读取器、扩展读取器注册表和 custom_data 路径读取器。
 * </p>
 */
public final class ItemComponentMatcher {
    private ItemComponentMatcher() {
    }

    /**
     * @deprecated 请直接使用 {@link com.cooobird.datatip.api.condition.ComponentValueReader}。
     */
    @Deprecated(forRemoval = false)
    @FunctionalInterface
    public interface ComponentValueReader extends com.cooobird.datatip.api.condition.ComponentValueReader {
    }

    public static void registerComponentReader(String componentName, com.cooobird.datatip.api.condition.ComponentValueReader reader) {
        ComponentReaderRegistry.register(componentName, reader);
    }

    public static void registerComponentReader(DataComponentType<?> componentType, com.cooobird.datatip.api.condition.ComponentValueReader reader) {
        ComponentReaderRegistry.register(componentType, reader);
    }

    public static void registerOrReplaceComponentReader(String componentName, com.cooobird.datatip.api.condition.ComponentValueReader reader) {
        ComponentReaderRegistry.registerOrReplace(componentName, reader);
    }

    public static void registerOrReplaceComponentReader(DataComponentType<?> componentType, com.cooobird.datatip.api.condition.ComponentValueReader reader) {
        ComponentReaderRegistry.registerOrReplace(componentType, reader);
    }

    public static boolean unregisterComponentReader(String componentName) {
        return ComponentReaderRegistry.unregister(componentName);
    }

    public static boolean unregisterComponentReader(DataComponentType<?> componentType) {
        return ComponentReaderRegistry.unregister(componentType);
    }

    public static Set<String> registeredComponentReaders() {
        return ComponentReaderRegistry.registeredNames();
    }

    public static void clearComponentReaders() {
        ComponentReaderRegistry.clear();
    }

    public static boolean hasComponent(ItemStack stack, String componentName) {
        String normalizedName = ComponentReaderRegistry.normalizeName(componentName);
        Boolean builtInResult = BuiltInComponentReaders.hasComponent(stack, normalizedName);
        if (builtInResult != null) {
            return builtInResult;
        }
        return ComponentReaderRegistry.read(stack, normalizedName) != null;
    }

    public static String getComponentValue(ItemStack stack, String componentName) {
        String normalizedName = ComponentReaderRegistry.normalizeName(componentName);
        String builtInValue = BuiltInComponentReaders.getComponentValue(stack, normalizedName);
        if (builtInValue != null) {
            return builtInValue;
        }

        String value = ComponentReaderRegistry.read(stack, normalizedName);
        return value != null ? value : "";
    }

    public static boolean hasCustomData(ItemStack stack, String path) {
        return CustomDataPathReader.has(stack, path);
    }

    @Nullable
    public static String getCustomDataValue(ItemStack stack, String path) {
        return CustomDataPathReader.get(stack, path);
    }

    public static boolean matchesCustomData(ItemStack stack, @Nullable Map<String, Object> conditions) {
        return CustomDataPathReader.matches(stack, conditions);
    }
}
