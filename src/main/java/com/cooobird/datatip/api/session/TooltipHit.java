package com.cooobird.datatip.api.session;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * 一次 Tooltip 命中中所有内容共享的会话、物品快照和指纹。
 */
public record TooltipHit(
    TooltipSession session,
    ItemStack stackSnapshot,
    ItemStackFingerprint itemFingerprint
) {
    public TooltipHit(
        TooltipSession session,
        ItemStack stackSnapshot,
        ItemStackFingerprint itemFingerprint
    ) {
        this.session = Objects.requireNonNull(session, "session");
        this.stackSnapshot = Objects.requireNonNull(
            stackSnapshot,
            "stackSnapshot"
        );
        this.itemFingerprint = Objects.requireNonNull(
            itemFingerprint,
            "itemFingerprint"
        );
    }
}
