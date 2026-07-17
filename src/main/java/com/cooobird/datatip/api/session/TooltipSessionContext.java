package com.cooobird.datatip.api.session;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 将当前 TooltipSession 沿同步准备和绘制调用链传递给缓存。
 */
public final class TooltipSessionContext {
    private static final ThreadLocal<TooltipSession> CURRENT = new ThreadLocal<>();

    private TooltipSessionContext() {
    }

    @Nullable
    public static TooltipSession current() {
        return CURRENT.get();
    }

    public static <T> T call(TooltipSession session, Supplier<T> action) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(action, "action");
        TooltipSession previous = CURRENT.get();
        CURRENT.set(session);
        try {
            return action.get();
        } finally {
            restore(previous);
        }
    }

    public static void run(TooltipSession session, Runnable action) {
        call(session, () -> {
            action.run();
            return null;
        });
    }

    private static void restore(@Nullable TooltipSession previous) {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }
}
