package com.cooobird.datatip.api.session;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 只保存当前悬停会话，不使用固定容量 LRU。
 */
public final class TooltipSessionManager implements AutoCloseable {
    private static final AtomicLong NEXT_SESSION_ID = new AtomicLong();

    @Nullable
    private TooltipSession active;
    private final ItemFingerprintFactory fingerprintFactory;
    private boolean touchedThisFrame;

    public TooltipSessionManager() {
        this(ItemStackFingerprint::capture);
    }

    public TooltipSessionManager(ItemFingerprintFactory fingerprintFactory) {
        this.fingerprintFactory = Objects.requireNonNull(
            fingerprintFactory,
            "fingerprintFactory"
        );
    }

    public synchronized TooltipSession acquire(
        HoverIdentity identity,
        TooltipDependencies dependencies
    ) {
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(dependencies, "dependencies");
        touchedThisFrame = true;
        if (active != null && active.identity().equals(identity)) {
            active.updateDependencies(dependencies);
            return active;
        }
        closeActive();
        active = new TooltipSession(
            NEXT_SESSION_ID.incrementAndGet(),
            identity,
            dependencies
        );
        return active;
    }

    public synchronized TooltipHit acquireHit(
        HoverIdentity identity,
        net.minecraft.world.item.ItemStack stack,
        Function<ItemStackFingerprint, TooltipDependencies> dependenciesFactory
    ) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(dependenciesFactory, "dependenciesFactory");
        ItemStackFingerprint fingerprint = fingerprintFactory.capture(stack);
        TooltipDependencies dependencies = dependenciesFactory.apply(fingerprint);
        if (!fingerprint.equals(dependencies.itemFingerprint())) {
            throw new IllegalArgumentException(
                "Tooltip dependencies must use the captured item fingerprint"
            );
        }
        TooltipSession session = acquire(identity, dependencies);
        return new TooltipHit(session, stack.copy(), fingerprint);
    }

    @Nullable
    public synchronized TooltipSession active() {
        return active;
    }

    public synchronized void endHover() {
        closeActive();
    }

    public synchronized void beginFrame() {
        touchedThisFrame = false;
    }

    public synchronized void endFrame() {
        if (!touchedThisFrame) {
            closeActive();
        }
    }

    public synchronized void onWorldChanged() {
        closeActive();
    }

    public synchronized void onResourceReload(TooltipDependencies dependencies) {
        if (active != null) {
            active.updateDependencies(Objects.requireNonNull(dependencies, "dependencies"));
        }
    }

    @Override
    public synchronized void close() {
        closeActive();
    }

    private void closeActive() {
        if (active == null) return;
        TooltipSession previous = active;
        active = null;
        previous.close();
    }
}
