package com.cooobird.datatip.internal.variable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * 变量注册表。
 * 存放内置变量和外部注册的自定义变量。
 */
public final class VariableRegistry {
    private static final Pattern VARIABLE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Map<String, Function<ItemStack, String>> VARIABLES = new ConcurrentHashMap<>();

    static {
        registerItemVariables();
        registerPlayerVariables();
        registerWorldVariables();
        registerFormattedVariables();
    }

    private VariableRegistry() {
    }

    public static Map<String, Function<ItemStack, String>> variables() {
        return Map.copyOf(VARIABLES);
    }

    public static void register(String name, Function<ItemStack, String> resolver) {
        if (name == null || !VARIABLE_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid variable name: " + name);
        }
        VARIABLES.put(name, java.util.Objects.requireNonNull(resolver, "resolver"));
        VariableCache.clear();
    }

    private static void registerItemVariables() {
        register("durability", stack ->
            String.valueOf(stack.getMaxDamage() - stack.getDamageValue()));
        register("max_durability", stack ->
            String.valueOf(stack.getMaxDamage()));
        register("damage", stack ->
            String.valueOf(stack.getDamageValue()));
        register("count", stack ->
            String.valueOf(stack.getCount()));
        register("item_name", stack ->
            stack.getHoverName().getString());
        register("item_id", stack ->
            stack.getItem().toString());
        register("durability_percent", stack -> {
            if (!stack.isDamageableItem()) return "100";
            int max = stack.getMaxDamage();
            int current = max - stack.getDamageValue();
            return String.valueOf((int) ((current * 100.0) / max));
        });
        register("enchantment_count", stack -> {
            ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            return String.valueOf(enchants.size());
        });
        register("is_enchanted", stack ->
            String.valueOf(stack.isEnchanted()));
        register("rarity", stack ->
            stack.getRarity().name().toLowerCase());
        register("max_stack_size", stack ->
            String.valueOf(stack.getMaxStackSize()));
        register("is_stackable", stack ->
            String.valueOf(stack.getMaxStackSize() > 1));
        register("is_damageable", stack ->
            String.valueOf(stack.isDamageableItem()));
    }

    private static void registerPlayerVariables() {
        register("player_health", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getHealth()) : "0";
        });
        register("player_max_health", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getMaxHealth()) : "0";
        });
        register("player_hunger", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf(player.getFoodData().getFoodLevel()) : "0";
        });
        register("player_experience", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf(player.experienceLevel) : "0";
        });
        register("player_x", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getX()) : "0";
        });
        register("player_y", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getY()) : "0";
        });
        register("player_z", stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getZ()) : "0";
        });
    }

    private static void registerWorldVariables() {
        register("game_time", stack -> {
            var level = Minecraft.getInstance().level;
            return level != null ? String.valueOf(level.getDayTime()) : "0";
        });
        register("is_day", stack -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return "false";
            long time = level.getDayTime() % 24000;
            return String.valueOf(time >= 0 && time < 12000);
        });
        register("is_raining", stack -> {
            var level = Minecraft.getInstance().level;
            return level != null ? String.valueOf(level.isRaining()) : "false";
        });
        register("is_thundering", stack -> {
            var level = Minecraft.getInstance().level;
            return level != null ? String.valueOf(level.isThundering()) : "false";
        });
    }

    private static void registerFormattedVariables() {
        register("durability_bar", stack -> {
            if (!stack.isDamageableItem()) return "████████████";
            int max = stack.getMaxDamage();
            int current = max - stack.getDamageValue();
            int bars = (int) ((current * 12.0) / max);
            return "█".repeat(bars) + "░".repeat(12 - bars);
        });
        register("health_bar", stack -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return "░░░░░░░░░░░░";
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            int bars = (int) ((health / maxHealth) * 12);
            return "❤".repeat(bars) + "♡".repeat(12 - bars);
        });
    }
}
