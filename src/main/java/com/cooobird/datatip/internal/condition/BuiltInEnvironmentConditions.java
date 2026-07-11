package com.cooobird.datatip.internal.condition;

import com.cooobird.datatip.api.condition.ConditionChecker;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Map;

/**
 * 环境相关内置条件。
 */
final class BuiltInEnvironmentConditions {
    private BuiltInEnvironmentConditions() {
    }

    static void registerTo(Map<String, ConditionChecker.CustomCondition> conditions) {
        conditions.put("dimension", (value, stack, player, level) -> checkDimension(value, level));
        conditions.put("biome", (value, stack, player, level) -> checkBiome(value, player, level));
        conditions.put("time", (value, stack, player, level) -> checkTime(value, level));
        conditions.put("weather", (value, stack, player, level) -> checkWeather(value, level));
        conditions.put("light", (value, stack, player, level) -> checkLight(value, player, level));
        conditions.put("altitude", (value, stack, player, level) -> checkAltitude(value, player));
    }

    static Boolean check(ConditionChecker.Condition condition, Player player, Level level) {
        return switch (condition.type()) {
            case "dimension" -> checkDimension(condition.value(), level);
            case "biome" -> checkBiome(condition.value(), player, level);
            case "time" -> checkTime(condition.value(), level);
            case "weather" -> checkWeather(condition.value(), level);
            case "light" -> checkLight(condition.value(), player, level);
            case "altitude" -> checkAltitude(condition.value(), player);
            default -> null;
        };
    }

    private static boolean checkDimension(Object value, Level level) {
        if (value instanceof String dimStr) {
            ResourceLocation dimId = ResourceLocation.tryParse(dimStr);
            return level.dimension().location().equals(dimId);
        }
        return false;
    }

    private static boolean checkBiome(Object value, Player player, Level level) {
        Holder<Biome> biome = level.getBiome(player.blockPosition());
        if (value instanceof String biomeStr) {
            ResourceLocation biomeId = ResourceLocation.tryParse(biomeStr);
            return biomeId != null && biome.is(biomeId);
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String str) {
                    ResourceLocation id = ResourceLocation.tryParse(str);
                    if (id != null && biome.is(id)) return true;
                }
            }
        }
        return false;
    }

    private static boolean checkTime(Object value, Level level) {
        long time = level.getDayTime() % 24000;
        if (value instanceof Number num) return time >= num.longValue();
        if (value instanceof String str) {
            return switch (str.toLowerCase()) {
                case "day" -> time >= 0 && time < 12000;
                case "night" -> time >= 12000;
                case "noon" -> time >= 5500 && time <= 6500;
                case "midnight" -> time >= 17500 || time <= 500;
                default -> false;
            };
        }
        return false;
    }

    private static boolean checkWeather(Object value, Level level) {
        if (!(value instanceof String str)) return false;
        return switch (str.toLowerCase()) {
            case "clear" -> !level.isRaining() && !level.isThundering();
            case "rain" -> level.isRaining();
            case "thunder" -> level.isThundering();
            default -> false;
        };
    }

    private static boolean checkLight(Object value, Player player, Level level) {
        int light = level.getMaxLocalRawBrightness(player.blockPosition());
        if (value instanceof Number num) return light >= num.intValue();
        if (value instanceof String str) {
            return switch (str.toLowerCase()) {
                case "dark" -> light < 4;
                case "dim" -> light < 8;
                case "bright" -> light >= 8;
                case "full" -> light >= 12;
                default -> false;
            };
        }
        return false;
    }

    private static boolean checkAltitude(Object value, Player player) {
        int y = player.blockPosition().getY();
        if (value instanceof Number num) return y >= num.intValue();
        if (value instanceof String str) {
            if (str.startsWith(">=")) {
                Integer threshold = ConditionNumbers.parseIntOrNull(str.substring(2));
                return threshold != null && y >= threshold;
            }
            if (str.startsWith("<=")) {
                Integer threshold = ConditionNumbers.parseIntOrNull(str.substring(2));
                return threshold != null && y <= threshold;
            }
            if (str.startsWith(">")) {
                Integer threshold = ConditionNumbers.parseIntOrNull(str.substring(1));
                return threshold != null && y > threshold;
            }
            if (str.startsWith("<")) {
                Integer threshold = ConditionNumbers.parseIntOrNull(str.substring(1));
                return threshold != null && y < threshold;
            }
            Integer threshold = ConditionNumbers.parseIntOrNull(str);
            return threshold != null && y >= threshold;
        }
        return false;
    }
}
