package com.cooobird.datatip.tooltip;

import com.cooobird.datatip.data.TooltipEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * 鎵惧嚭鎵€鏈夊尮閰嶅綋鍓嶇墿鍝佺殑瑙勫垯銆?
 */
public class TooltipMatcher {

    public static List<TooltipEntry> matchAll(ItemStack stack, List<TooltipEntry> entries) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) return List.of();
        String fullId = id.toString();

        List<TooltipEntry> result = new ArrayList<>();
        for (TooltipEntry e : entries) {
            if (!idMatches(e, fullId, stack)) continue;
            if (!nbtMatches(e, stack)) continue;
            if (!checkConds(e.conditions())) continue;
            result.add(e);
        }
        return result;
    }

    private static boolean idMatches(TooltipEntry e, String fullId, ItemStack stack) {
        return switch (e.matchType()) {
            case EXACT -> e.match().equals(fullId);
            case TAG -> stack.is(TagKey.create(Registries.ITEM, ResourceLocation.parse(e.match())));
            case WILDCARD -> e.wildcardMatch(fullId, e.match());
        };
    }

    // NBT 鍖归厤
    private static boolean nbtMatches(TooltipEntry e, ItemStack stack) {
        if (e.nbt() == null) return true;
        CompoundTag tag = stack.getTag();
        for (var p : e.nbt().entrySet()) {
            Object want = p.getValue();
            if (want == null) continue;
            String wantStr = want.toString();

            String actual = switch (p.getKey()) {
                case "Damage" -> String.valueOf(stack.getDamageValue());
                case "Count" -> String.valueOf(stack.getCount());
                case "RepairCost" -> {
                    if (tag != null && tag.contains("RepairCost")) {
                        yield String.valueOf(tag.getInt("RepairCost"));
                    }
                    yield "0";
                }
                default -> {
                    if (tag != null && tag.contains(p.getKey())) {
                        yield tag.get(p.getKey()).getAsString();
                    }
                    yield null;
                }
            };
            if (actual == null || !actual.equals(wantStr)) return false;
        }
        return true;
    }

    private static boolean checkConds(List<TooltipEntry.Condition> conds) {
        if (conds.isEmpty()) return true;
        var p = Minecraft.getInstance().player;
        if (p == null || p.level() == null) return false;

        for (var c : conds) {
            Object raw = c.value();
            if (raw == null) continue;
            String val = raw.toString();

            switch (c.type()) {
                case "dimension" -> {
                    var dim = p.level().dimension();
                    if (dim == null || !dim.location().toString().equals(val)) return false;
                }
                case "biome" -> {
                    boolean ok = p.level().getBiome(p.blockPosition()).unwrapKey()
                        .map(k -> {
                            var loc = k.location();
                            if (loc == null) return false;
                            if (raw instanceof List<?> l) return l.contains(loc.toString());
                            return loc.toString().equals(val);
                        }).orElse(false);
                    if (!ok) return false;
                }
                case "holding" -> {
                    var mh = p.getMainHandItem();
                    var oh = p.getOffhandItem();
                    ResourceLocation mhKey = mh.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(mh.getItem());
                    ResourceLocation ohKey = oh.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(oh.getItem());
                    String mainId = mhKey != null ? mhKey.toString() : "";
                    String offId = ohKey != null ? ohKey.toString() : "";
                    if (!mainId.equals(val) && !offId.equals(val)) return false;
                }
                case "sneaking" -> {
                    if (!p.isShiftKeyDown()) return false;
                }
            }
        }
        return true;
    }
}
