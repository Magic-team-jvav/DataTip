package com.cooobird.datatip.api.session;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemFingerprintFactory {
    ItemStackFingerprint capture(ItemStack stack);
}
