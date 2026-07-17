package com.cooobird.datatip.internal.variable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量注册表。
 * 存放内置变量和外部注册的自定义变量。
 */
public final class VariableRegistry {
    private static final Pattern VARIABLE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Map<String, Function<ItemStack, String>> VARIABLES = new ConcurrentHashMap<>();
    private static final Map<String, VariableDependency> DEPENDENCIES =
        new ConcurrentHashMap<>();
    private static final AtomicLong REVISION = new AtomicLong();

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
        register(name, VariableDependency.EVENT, resolver);
    }

    public static void register(
        String name,
        VariableDependency dependency,
        Function<ItemStack, String> resolver
    ) {
        if (name == null || !VARIABLE_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid variable name: " + name);
        }
        Function<ItemStack, String> checkedResolver =
            java.util.Objects.requireNonNull(resolver, "resolver");
        VariableDependency checkedDependency =
            java.util.Objects.requireNonNull(dependency, "dependency");
        Function<ItemStack, String> previousResolver = VARIABLES.put(
            name,
            checkedResolver
        );
        VariableDependency previousDependency = DEPENDENCIES.put(
            name,
            checkedDependency
        );
        if (previousResolver != checkedResolver
            || previousDependency != checkedDependency) {
            REVISION.incrementAndGet();
            VariableCache.clear();
        }
    }

    public static long getRevision() {
        return REVISION.get();
    }

    public static VariableDependency dependency(String name) {
        return DEPENDENCIES.getOrDefault(name, VariableDependency.EVENT);
    }

    public static boolean isItemStatic(String text) {
        if (text == null || !text.contains("{")) return true;
        int cursor = 0;
        while (cursor < text.length()) {
            int start = text.indexOf('{', cursor);
            if (start < 0) return true;
            int end = VariableTextScanner.findClosingBrace(text, start);
            if (end <= start + 1) return false;
            String body = text.substring(start + 1, end);
            if (body.startsWith("component:") || body.startsWith("custom_data:")) {
                cursor = end + 1;
                continue;
            }
            if (VARIABLES.containsKey(body)) {
                if (dependency(body) != VariableDependency.ITEM) return false;
                cursor = end + 1;
                continue;
            }
            if (VariableTextScanner.isExpression(body)) {
                Matcher matcher = IDENTIFIER.matcher(body);
                while (matcher.find()) {
                    String identifier = matcher.group();
                    if (VARIABLES.containsKey(identifier)
                        && dependency(identifier) != VariableDependency.ITEM) {
                        return false;
                    }
                }
                cursor = end + 1;
                continue;
            }
            return false;
        }
        return true;
    }

    private static void registerItemVariables() {
        register("durability", VariableDependency.ITEM, stack ->
            String.valueOf(stack.getMaxDamage() - stack.getDamageValue()));
        register("max_durability", VariableDependency.ITEM, stack ->
            String.valueOf(stack.getMaxDamage()));
        register("damage", VariableDependency.ITEM, stack ->
            String.valueOf(stack.getDamageValue()));
        register("count", VariableDependency.ITEM, stack ->
            String.valueOf(stack.getCount()));
        register("item_name", VariableDependency.ITEM, stack ->
            stack.getHoverName().getString());
        register("item_id", VariableDependency.ITEM, stack ->
            stack.getItem().toString());
        register("durability_percent", VariableDependency.ITEM, stack -> {
            if (!stack.isDamageableItem()) return "100";
            int max = stack.getMaxDamage();
            int current = max - stack.getDamageValue();
            return String.valueOf((int) ((current * 100.0) / max));
        });
        register("enchantment_count", VariableDependency.ITEM, stack -> {
            ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            return String.valueOf(enchants.size());
        });
        register("is_enchanted", VariableDependency.ITEM, stack ->
            String.valueOf(stack.isEnchanted()));
        register("rarity", VariableDependency.ITEM, stack ->
            stack.getRarity().name().toLowerCase(Locale.ROOT));
        register("max_stack_size", VariableDependency.ITEM, stack ->
            String.valueOf(stack.getMaxStackSize()));
        register("is_stackable", VariableDependency.ITEM, stack ->
            String.valueOf(stack.getMaxStackSize() > 1));
        register("is_damageable", VariableDependency.ITEM, stack ->
            String.valueOf(stack.isDamageableItem()));
    }

    private static void registerPlayerVariables() {
        register("player_health", VariableDependency.PLAYER, stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getHealth()) : "0";
        });
        register("player_max_health", VariableDependency.PLAYER, stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getMaxHealth()) : "0";
        });
        register("player_hunger", VariableDependency.PLAYER, stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf(player.getFoodData().getFoodLevel()) : "0";
        });
        register("player_experience", VariableDependency.PLAYER, stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf(player.experienceLevel) : "0";
        });
        register("player_x", VariableDependency.PLAYER, stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getX()) : "0";
        });
        register("player_y", VariableDependency.PLAYER, stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getY()) : "0";
        });
        register("player_z", VariableDependency.PLAYER, stack -> {
            var player = Minecraft.getInstance().player;
            return player != null ? String.valueOf((int) player.getZ()) : "0";
        });
    }

    private static void registerWorldVariables() {
        register("game_time", VariableDependency.TICK, stack -> {
            var level = Minecraft.getInstance().level;
            return level != null ? String.valueOf(level.getDayTime()) : "0";
        });
        register("is_day", VariableDependency.TICK, stack -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return "false";
            long time = level.getDayTime() % 24000;
            return String.valueOf(time >= 0 && time < 12000);
        });
        register("is_raining", VariableDependency.WORLD, stack -> {
            var level = Minecraft.getInstance().level;
            return level != null ? String.valueOf(level.isRaining()) : "false";
        });
        register("is_thundering", VariableDependency.WORLD, stack -> {
            var level = Minecraft.getInstance().level;
            return level != null ? String.valueOf(level.isThundering()) : "false";
        });
    }

    private static void registerFormattedVariables() {
        register("durability_bar", VariableDependency.ITEM, stack -> {
            if (!stack.isDamageableItem()) return "████████████";
            int max = stack.getMaxDamage();
            int current = max - stack.getDamageValue();
            int bars = (int) ((current * 12.0) / max);
            return "█".repeat(bars) + "░".repeat(12 - bars);
        });
        register("health_bar", VariableDependency.PLAYER, stack -> {
            var player = Minecraft.getInstance().player;
            if (player == null) return "░░░░░░░░░░░░";
            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            int bars = (int) ((health / maxHealth) * 12);
            return "❤".repeat(bars) + "♡".repeat(12 - bars);
        });
    }
}
