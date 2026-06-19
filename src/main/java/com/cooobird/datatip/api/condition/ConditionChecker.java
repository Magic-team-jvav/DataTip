package com.cooobird.datatip.api.condition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 条件检查器。
 * <p>
 * 根据游戏状态决定是否显示 tooltip。
 * 支持缓存检查结果以提升性能（缓存有效期 1 秒）。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // JSON 中的条件配置
 * {
 *   "type": "vbox",
 *   "conditions": {
 *     "dimension": "minecraft:the_nether",
 *     "health": "50%"
 *   },
 *   "children": [...]
 * }
 * }</pre>
 *
 * <h3>支持的条件类型</h3>
 * <table border="1">
 *   <tr><th>类型</th><th>说明</th><th>值类型</th><th>示例</th></tr>
 *   <tr><td>dimension</td><td>维度</td><td>String</td><td>"minecraft:the_nether"</td></tr>
 *   <tr><td>biome</td><td>生物群系</td><td>String/Array</td><td>"minecraft:desert"</td></tr>
 *   <tr><td>holding</td><td>手持物品</td><td>String/Array</td><td>"minecraft:diamond_sword"</td></tr>
 *   <tr><td>sneaking</td><td>是否潜行</td><td>boolean</td><td>true</td></tr>
 *   <tr><td>creative</td><td>是否创造模式</td><td>boolean</td><td>true</td></tr>
 *   <tr><td>survival</td><td>是否生存模式</td><td>boolean</td><td>true</td></tr>
 *   <tr><td>health</td><td>生命值</td><td>Number/String</td><td>50, "50%"</td></tr>
 *   <tr><td>hunger</td><td>饥饿值</td><td>Number</td><td>10</td></tr>
 *   <tr><td>experience</td><td>经验等级</td><td>Number</td><td>30</td></tr>
 *   <tr><td>time</td><td>时间</td><td>Number/String</td><td>6000, "day", "night"</td></tr>
 *   <tr><td>weather</td><td>天气</td><td>String</td><td>"clear", "rain", "thunder"</td></tr>
 *   <tr><td>light</td><td>光照等级</td><td>Number/String</td><td>8, "dark", "bright"</td></tr>
 *   <tr><td>altitude</td><td>海拔高度</td><td>Number/String</td><td>64, ">=64"</td></tr>
 *   <tr><td>enchanted</td><td>是否附魔</td><td>boolean</td><td>true</td></tr>
 *   <tr><td>damage</td><td>损坏值</td><td>Number</td><td>100</td></tr>
 *   <tr><td>count</td><td>物品数量</td><td>Number</td><td>16</td></tr>
 * </table>
 *
 * @author cooobird
 * @see Condition 条件记录
 * @since 1.2.0
 */
public class ConditionChecker {

    /**
     * 条件检查结果缓存
     */
    private static final Map<String, CachedResult> CACHE = new ConcurrentHashMap<>();
    /**
     * 缓存过期时间（毫秒）
     */
    private static final long CACHE_EXPIRY_MS = 1000; // 1 秒

    /**
     * 自定义条件注册表
     */
    private static final Map<String, CustomCondition> CUSTOM_CONDITIONS = new ConcurrentHashMap<>();

    /**
     * 自定义条件接口。
     * 其他 mod 可以实现此接口来注册自定义条件。
     */
    @FunctionalInterface
    public interface CustomCondition {
        /**
         * 检查条件是否满足。
         *
         * @param value  条件值
         * @param stack  物品栈
         * @param player 玩家
         * @param level  世界
         * @return true 如果条件满足
         */
        boolean check(Object value, ItemStack stack, Player player, Level level);
    }

    /**
     * 注册自定义条件。
     *
     * @param type      条件类型名
     * @param condition 条件检查实现
     */
    public static void registerCondition(String type, CustomCondition condition) {
        CUSTOM_CONDITIONS.put(type, condition);
    }

    /**
     * 缓存的检查结果。
     */
    private record CachedResult(boolean result, long timestamp) {
        /**
         * 检查缓存是否已过期。
         *
         * @return true 如果缓存已过期
         */
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MS;
        }
    }

    /**
     * 检查所有条件是否满足。
     *
     * @param conditions 条件列表
     * @param stack      物品栈
     * @return true 如果所有条件都满足
     */
    public static boolean checkAll(List<Condition> conditions, ItemStack stack) {
        if (conditions.isEmpty()) return true;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) return false;

        for (Condition condition : conditions) {
            if (!check(condition, stack, player, level)) return false;
        }
        return true;
    }

    /**
     * 检查单个条件（带缓存）。
     *
     * @param condition 条件
     * @param stack     物品栈
     * @param player    玩家
     * @param level     世界
     * @return true 如果条件满足
     */
    private static boolean check(Condition condition, ItemStack stack, Player player, Level level) {
        String cacheKey = buildCacheKey(condition, stack, player, level);
        CachedResult cached = CACHE.get(cacheKey);
        if (cached != null && !cached.isExpired()) return cached.result();

        boolean result = checkCondition(condition, stack, player, level);
        CACHE.put(cacheKey, new CachedResult(result, System.currentTimeMillis()));
        return result;
    }

    /**
     * 根据条件类型分发到具体的检查方法。
     *
     * @param condition 条件
     * @param stack     物品栈
     * @param player    玩家
     * @param level     世界
     * @return true 如果条件满足
     */
    private static boolean checkCondition(Condition condition, ItemStack stack, Player player, Level level) {
        // 先检查自定义条件
        CustomCondition custom = CUSTOM_CONDITIONS.get(condition.type());
        if (custom != null) {
            return custom.check(condition.value(), stack, player, level);
        }

        // 内置条件
        return switch (condition.type()) {
            case "dimension" -> checkDimension(condition.value(), level);
            case "biome" -> checkBiome(condition.value(), player, level);
            case "holding" -> checkHolding(condition.value(), player);
            case "sneaking" -> player.isShiftKeyDown();
            case "creative" -> player.isCreative();
            case "survival" -> !player.isCreative() && !player.isSpectator();
            case "health" -> checkHealth(condition.value(), player);
            case "hunger" -> checkHunger(condition.value(), player);
            case "experience", "level" -> checkExperience(condition.value(), player);
            case "time" -> checkTime(condition.value(), level);
            case "weather" -> checkWeather(condition.value(), level);
            case "light" -> checkLight(condition.value(), player, level);
            case "altitude" -> checkAltitude(condition.value(), player);
            case "enchanted" -> checkEnchanted(condition.value(), stack);
            case "damage" -> checkDamage(condition.value(), stack);
            case "count" -> checkCount(condition.value(), stack);
            case "nbt" -> checkNbt(condition.value(), stack);
            case "item_tag" -> checkItemTag(condition.value(), stack);
            default -> true;
        };
    }

    /**
     * 构建缓存键。
     *
     * @param condition 条件
     * @param stack     物品栈
     * @param player    玩家
     * @param level     世界
     * @return 缓存键字符串
     */
    private static String buildCacheKey(Condition condition, ItemStack stack, Player player, Level level) {
        StringBuilder sb = new StringBuilder();
        sb.append(condition.type()).append(':').append(condition.value());

        switch (condition.type()) {
            case "dimension" -> sb.append(':').append(level.dimension().location());
            case "biome" -> sb.append(':').append(level.getBiome(player.blockPosition()).unwrapKey()
                .map(k -> k.location().toString()).orElse("unknown"));
            case "health" -> sb.append(':').append((int) player.getHealth());
            case "hunger" -> sb.append(':').append(player.getFoodData().getFoodLevel());
            case "time" -> sb.append(':').append(level.getDayTime() / 20);
            case "weather" -> sb.append(':').append(level.isRaining()).append(level.isThundering());
            case "light" -> sb.append(':').append(level.getMaxLocalRawBrightness(player.blockPosition()));
            case "altitude" -> sb.append(':').append(player.blockPosition().getY());
            default -> sb.append(':').append(stack.hashCode());
        }

        return sb.toString();
    }

    // ========== 条件检查方法 ==========

    /**
     * 检查维度条件。
     *
     * @param value 维度 ID（如 "minecraft:the_nether"）
     * @param level 世界
     * @return true 如果当前维度匹配
     */
    private static boolean checkDimension(Object value, Level level) {
        if (value instanceof String dimStr) {
            ResourceLocation dimId = ResourceLocation.tryParse(dimStr);
            return level.dimension().location().equals(dimId);
        }
        return false;
    }

    /**
     * 检查生物群系条件。
     *
     * @param value  生物群系 ID 或 ID 数组
     * @param player 玩家
     * @param level  世界
     * @return true 如果当前生物群系匹配
     */
    private static boolean checkBiome(Object value, Player player, Level level) {
        Holder<Biome> biome = level.getBiome(player.blockPosition());
        if (value instanceof String biomeStr) {
            ResourceLocation biomeId = ResourceLocation.tryParse(biomeStr);
            return biomeId != null && biome.is(biomeId);
        } else if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String str) {
                    ResourceLocation id = ResourceLocation.tryParse(str);
                    if (id != null && biome.is(id)) return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查手持物品条件。
     *
     * @param value  物品 ID 或 ID 数组
     * @param player 玩家
     * @return true 如果手持物品匹配
     */
    private static boolean checkHolding(Object value, Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (value instanceof String itemStr) {
            ResourceLocation itemId = ResourceLocation.tryParse(itemStr);
            if (itemId != null) {
                Item item = BuiltInRegistries.ITEM.get(itemId);
                return mainHand.is(item) || offHand.is(item);
            }
        } else if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof String str) {
                    ResourceLocation id = ResourceLocation.tryParse(str);
                    if (id != null) {
                        Item checkItem = BuiltInRegistries.ITEM.get(id);
                        if (mainHand.is(checkItem) || offHand.is(checkItem)) return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查生命值条件。
     *
     * @param value  生命值阈值（数字或百分比字符串如 "50%"）
     * @param player 玩家
     * @return true 如果生命值 >= 阈值
     */
    private static boolean checkHealth(Object value, Player player) {
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        if (value instanceof Number num) return health >= num.floatValue();
        if (value instanceof String str) {
            if (str.endsWith("%")) {
                float percent = Float.parseFloat(str.substring(0, str.length() - 1));
                return (health / maxHealth * 100) >= percent;
            }
            return health >= Float.parseFloat(str);
        }
        return false;
    }

    /**
     * 检查饥饿值条件。
     *
     * @param value  饥饿值阈值
     * @param player 玩家
     * @return true 如果饥饿值 >= 阈值
     */
    private static boolean checkHunger(Object value, Player player) {
        int foodLevel = player.getFoodData().getFoodLevel();
        if (value instanceof Number num) return foodLevel >= num.intValue();
        if (value instanceof String str) return foodLevel >= Integer.parseInt(str);
        return false;
    }

    /**
     * 检查经验等级条件。
     *
     * @param value  经验等级阈值
     * @param player 玩家
     * @return true 如果经验等级 >= 阈值
     */
    private static boolean checkExperience(Object value, Player player) {
        int level = player.experienceLevel;
        if (value instanceof Number num) return level >= num.intValue();
        if (value instanceof String str) return level >= Integer.parseInt(str);
        return false;
    }

    /**
     * 检查时间条件。
     *
     * @param value 时间值（数字或 "day"/"night"/"noon"/"midnight"）
     * @param level 世界
     * @return true 如果时间匹配
     */
    private static boolean checkTime(Object value, Level level) {
        long time = level.getDayTime() % 24000;
        if (value instanceof Number num) return time >= num.longValue();
        if (value instanceof String str) {
            return switch (str.toLowerCase()) {
                case "day" -> time >= 0 && time < 12000;
                case "night" -> time >= 12000;
                case "noon" -> time >= 5500 && time <= 6500;
                case "midnight" -> time >= 17500 || time <= 500;
                default -> true;
            };
        }
        return false;
    }

    /**
     * 检查天气条件。
     *
     * @param value 天气类型（"clear"/"rain"/"thunder"）
     * @param level 世界
     * @return true 如果天气匹配
     */
    private static boolean checkWeather(Object value, Level level) {
        if (!(value instanceof String str)) return false;
        return switch (str.toLowerCase()) {
            case "clear" -> !level.isRaining() && !level.isThundering();
            case "rain" -> level.isRaining();
            case "thunder" -> level.isThundering();
            default -> true;
        };
    }

    /**
     * 检查光照条件。
     *
     * @param value  光照阈值（数字或 "dark"/"dim"/"bright"/"full"）
     * @param player 玩家
     * @param level  世界
     * @return true 如果光照匹配
     */
    private static boolean checkLight(Object value, Player player, Level level) {
        int light = level.getMaxLocalRawBrightness(player.blockPosition());
        if (value instanceof Number num) return light >= num.intValue();
        if (value instanceof String str) {
            return switch (str.toLowerCase()) {
                case "dark" -> light < 4;
                case "dim" -> light < 8;
                case "bright" -> light >= 8;
                case "full" -> light >= 12;
                default -> true;
            };
        }
        return false;
    }

    /**
     * 检查海拔高度条件。
     *
     * @param value  高度阈值（数字或比较字符串如 ">=64"）
     * @param player 玩家
     * @return true 如果高度匹配
     */
    private static boolean checkAltitude(Object value, Player player) {
        int y = player.blockPosition().getY();
        if (value instanceof Number num) return y >= num.intValue();
        if (value instanceof String str) {
            if (str.startsWith(">=")) return y >= Integer.parseInt(str.substring(2));
            if (str.startsWith("<=")) return y <= Integer.parseInt(str.substring(2));
            if (str.startsWith(">")) return y > Integer.parseInt(str.substring(1));
            if (str.startsWith("<")) return y < Integer.parseInt(str.substring(1));
            return y >= Integer.parseInt(str);
        }
        return false;
    }

    /**
     * 检查附魔条件。
     *
     * @param value 是否附魔
     * @param stack 物品栈
     * @return true 如果附魔状态匹配
     */
    private static boolean checkEnchanted(Object value, ItemStack stack) {
        boolean enchanted = stack.isEnchanted();
        if (value instanceof Boolean bool) return enchanted == bool;
        return enchanted;
    }

    /**
     * 检查损坏值条件。
     *
     * @param value 损坏值阈值
     * @param stack 物品栈
     * @return true 如果损坏值 <= 阈值
     */
    private static boolean checkDamage(Object value, ItemStack stack) {
        if (value instanceof Number num) return stack.getDamageValue() <= num.intValue();
        return false;
    }

    /**
     * 检查物品数量条件。
     *
     * @param value 数量阈值
     * @param stack 物品栈
     * @return true 如果数量 >= 阈值
     */
    private static boolean checkCount(Object value, ItemStack stack) {
        if (value instanceof Number num) return stack.getCount() >= num.intValue();
        return false;
    }

    /**
     * 检查 NBT 条件。
     * Forge 1.20.1 使用 NBT 系统。
     */
    private static boolean checkNbt(Object value, ItemStack stack) {
        if (!(value instanceof String path)) return false;
        if (!stack.hasTag()) return false;

        CompoundTag tag = stack.getTag();
        String[] parts = path.split("\\.");

        for (String part : parts) {
            if (tag == null) return false;

            if (part.endsWith("]")) {
                int bracketIndex = part.indexOf('[');
                String arrayName = part.substring(0, bracketIndex);
                int index = Integer.parseInt(part.substring(bracketIndex + 1, part.length() - 1));

                if (tag.contains(arrayName) && tag.getTagType(arrayName) == 9) {
                    ListTag list = tag.getList(arrayName, 10);
                    if (index >= 0 && index < list.size()) {
                        tag = list.getCompound(index);
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (tag.contains(part)) {
                    if (tag.getTagType(part) == 10) {
                        tag = tag.getCompound(part);
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 检查物品标签条件。
     */
    private static boolean checkItemTag(Object value, ItemStack stack) {
        if (!(value instanceof String tagStr)) return false;

        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, Objects.requireNonNull(ResourceLocation.tryParse(tagStr)));

        return stack.is(tagKey);
    }

    /**
     * 条件记录。
     *
     * @param type  条件类型（如 "dimension", "health"）
     * @param value 条件值
     */
    public record Condition(String type, Object value) {
    }
}
