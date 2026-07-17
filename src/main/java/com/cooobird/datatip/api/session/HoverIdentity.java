package com.cooobird.datatip.api.session;

import java.util.Objects;

/**
 * 仅描述“当前仍悬停在同一目标”这一稳定身份。
 * <p>
 * 物品组件、语言、viewport 等变化不属于身份，必须放进独立依赖快照。
 * </p>
 */
public final class HoverIdentity {
    private final Object hoverOwner;
    private final Object contentOwner;

    public HoverIdentity(Object hoverOwner, Object contentOwner) {
        this.hoverOwner = Objects.requireNonNull(hoverOwner, "hoverOwner");
        this.contentOwner = Objects.requireNonNull(contentOwner, "contentOwner");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof HoverIdentity identity
            && hoverOwner == identity.hoverOwner
            && contentOwner == identity.contentOwner;
    }

    @Override
    public int hashCode() {
        return 31 * System.identityHashCode(hoverOwner)
            + System.identityHashCode(contentOwner);
    }
}
