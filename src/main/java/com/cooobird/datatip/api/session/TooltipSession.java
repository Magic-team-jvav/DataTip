package com.cooobird.datatip.api.session;

import java.util.*;

/**
 * 单次稳定悬停期间持有的状态和缓存代次。
 */
public final class TooltipSession implements AutoCloseable {
    private final long sessionId;
    private final HoverIdentity identity;
    private final EnumMap<TooltipInvalidation, Long> generations =
        new EnumMap<>(TooltipInvalidation.class);
    private final EnumMap<TooltipInvalidation, Map<Object, Object>> caches =
        new EnumMap<>(TooltipInvalidation.class);
    private final List<OwnedResource> ownedResources = new ArrayList<>();
    private TooltipDependencies dependencies;
    private boolean closed;

    public TooltipSession(
        long sessionId,
        HoverIdentity identity,
        TooltipDependencies dependencies
    ) {
        this.sessionId = sessionId;
        this.identity = Objects.requireNonNull(identity, "identity");
        this.dependencies = Objects.requireNonNull(dependencies, "dependencies");
        for (TooltipInvalidation invalidation : TooltipInvalidation.values()) {
            generations.put(invalidation, 0L);
            caches.put(invalidation, new HashMap<>());
        }
    }

    public long sessionId() {
        return sessionId;
    }

    public HoverIdentity identity() {
        return identity;
    }

    public TooltipDependencies dependencies() {
        return dependencies;
    }

    public long generation(TooltipInvalidation invalidation) {
        return generations.get(Objects.requireNonNull(invalidation, "invalidation"));
    }

    public EnumSet<TooltipInvalidation> updateDependencies(
        TooltipDependencies replacement
    ) {
        ensureOpen();
        Objects.requireNonNull(replacement, "replacement");
        EnumSet<TooltipInvalidation> invalidated =
            EnumSet.noneOf(TooltipInvalidation.class);

        if (dependencies.resourceRevision() != replacement.resourceRevision()
            || dependencies.tagRevision() != replacement.tagRevision()
            || dependencies.runtimeContentRevision()
            != replacement.runtimeContentRevision()
            || dependencies.parserRevision() != replacement.parserRevision()) {
            invalidated.addAll(EnumSet.allOf(TooltipInvalidation.class));
        }
        if (dependencies.variableRevision() != replacement.variableRevision()
            || dependencies.componentReaderRevision()
            != replacement.componentReaderRevision()) {
            invalidated.addAll(EnumSet.of(
                TooltipInvalidation.VARIABLES,
                TooltipInvalidation.LAYOUT,
                TooltipInvalidation.RENDER
            ));
        }
        if (dependencies.conditionRevision() != replacement.conditionRevision()
            || dependencies.componentReaderRevision()
            != replacement.componentReaderRevision()) {
            invalidated.addAll(EnumSet.of(
                TooltipInvalidation.CONDITIONS,
                TooltipInvalidation.LAYOUT,
                TooltipInvalidation.RENDER
            ));
        }
        if (!dependencies.itemFingerprint().equals(replacement.itemFingerprint())) {
            invalidated.addAll(EnumSet.of(
                TooltipInvalidation.VARIABLES,
                TooltipInvalidation.LAYOUT,
                TooltipInvalidation.VIEWPORT,
                TooltipInvalidation.RENDER
            ));
        }
        if (!dependencies.languageCode().equals(replacement.languageCode())
            || dependencies.languageRevision() != replacement.languageRevision()
            || dependencies.fontIdentity() != replacement.fontIdentity()
            || dependencies.fontRevision() != replacement.fontRevision()
            || dependencies.guiScale() != replacement.guiScale()
            || dependencies.viewportWidth() != replacement.viewportWidth()
            || dependencies.viewportHeight() != replacement.viewportHeight()
            || !dependencies.config().equals(replacement.config())
            || dependencies.shiftDown() != replacement.shiftDown()) {
            invalidated.add(TooltipInvalidation.LAYOUT);
            invalidated.add(TooltipInvalidation.RENDER);
        }
        if (dependencies.worldIdentity() != replacement.worldIdentity()) {
            invalidated.addAll(EnumSet.of(
                TooltipInvalidation.ENTITY,
                TooltipInvalidation.VARIABLES,
                TooltipInvalidation.LAYOUT,
                TooltipInvalidation.VIEWPORT,
                TooltipInvalidation.RENDER
            ));
        }

        RuntimeException failure = null;
        for (TooltipInvalidation invalidation : invalidated) {
            generations.compute(invalidation, (ignored, generation) -> generation + 1);
            caches.get(invalidation).clear();
            failure = closeScope(invalidation, failure);
        }
        dependencies = replacement;
        if (failure != null) throw failure;
        return invalidated;
    }

    public void invalidate(TooltipInvalidation... invalidations) {
        ensureOpen();
        RuntimeException failure = null;
        for (TooltipInvalidation invalidation : invalidations) {
            TooltipInvalidation checked = Objects.requireNonNull(
                invalidation,
                "invalidation"
            );
            generations.compute(checked, (ignored, generation) -> generation + 1);
            caches.get(checked).clear();
            failure = closeScope(checked, failure);
        }
        if (failure != null) throw failure;
    }

    public void own(AutoCloseable resource) {
        ensureOpen();
        ownedResources.add(new OwnedResource(
            null,
            Objects.requireNonNull(resource, "resource")
        ));
    }

    public void own(
        TooltipInvalidation invalidation,
        AutoCloseable resource
    ) {
        ensureOpen();
        ownedResources.add(new OwnedResource(
            Objects.requireNonNull(invalidation, "invalidation"),
            Objects.requireNonNull(resource, "resource")
        ));
    }

    public <T> T cached(
        TooltipInvalidation invalidation,
        Object key,
        Class<T> type
    ) {
        ensureOpen();
        Object value = caches.get(
            Objects.requireNonNull(invalidation, "invalidation")
        ).get(Objects.requireNonNull(key, "key"));
        return value != null ? type.cast(value) : null;
    }

    public void cache(
        TooltipInvalidation invalidation,
        Object key,
        Object value
    ) {
        ensureOpen();
        caches.get(Objects.requireNonNull(invalidation, "invalidation"))
            .put(
                Objects.requireNonNull(key, "key"),
                Objects.requireNonNull(value, "value")
            );
    }

    /**
     * 获取当前悬停会话中的滚动状态。
     */
    public TooltipScrollState scrollState(Object key) {
        TooltipScrollState state = cached(
            TooltipInvalidation.VIEWPORT,
            key,
            TooltipScrollState.class
        );
        if (state != null) return state;
        TooltipScrollState created = new TooltipScrollState();
        cache(TooltipInvalidation.VIEWPORT, key, created);
        return created;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        for (TooltipInvalidation invalidation : TooltipInvalidation.values()) {
            caches.get(invalidation).clear();
        }
        for (int index = ownedResources.size() - 1; index >= 0; index--) {
            failure = closeOne(ownedResources.get(index), failure);
        }
        ownedResources.clear();
        if (failure != null) throw failure;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Tooltip session is closed");
        }
    }

    private RuntimeException closeScope(
        TooltipInvalidation invalidation,
        RuntimeException failure
    ) {
        for (int index = ownedResources.size() - 1; index >= 0; index--) {
            OwnedResource resource = ownedResources.get(index);
            if (resource.scope == invalidation) {
                failure = closeOne(resource, failure);
            }
        }
        return failure;
    }

    private static RuntimeException closeOne(
        OwnedResource owned,
        RuntimeException failure
    ) {
        if (owned.closed) return failure;
        owned.closed = true;
        try {
            owned.resource.close();
        } catch (Exception exception) {
            return mergeFailure(failure, exception);
        }
        return failure;
    }

    private static RuntimeException mergeFailure(
        RuntimeException current,
        Exception exception
    ) {
        if (current == null) {
            return new IllegalStateException(
                "Failed to close tooltip session resource",
                exception
            );
        }
        current.addSuppressed(exception);
        return current;
    }

    private static final class OwnedResource {
        private final TooltipInvalidation scope;
        private final AutoCloseable resource;
        private boolean closed;

        private OwnedResource(
            TooltipInvalidation scope,
            AutoCloseable resource
        ) {
            this.scope = scope;
            this.resource = resource;
        }
    }
}
