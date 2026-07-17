package com.cooobird.datatip.api.session;

import java.util.Objects;

/**
 * 影响会话求值和布局的独立依赖快照。
 */
public record TooltipDependencies(
    long resourceRevision,
    long tagRevision,
    long runtimeContentRevision,
    long parserRevision,
    long variableRevision,
    long conditionRevision,
    long componentReaderRevision,
    ItemStackFingerprint itemFingerprint,
    String languageCode,
    long languageRevision,
    Object fontIdentity,
    long fontRevision,
    int guiScale,
    int viewportWidth,
    int viewportHeight,
    TooltipConfigSnapshot config,
    boolean shiftDown,
    Object worldIdentity
) {
    public TooltipDependencies(
        long resourceRevision,
        long tagRevision,
        long runtimeContentRevision,
        long parserRevision,
        long variableRevision,
        long conditionRevision,
        long componentReaderRevision,
        ItemStackFingerprint itemFingerprint,
        String languageCode,
        long languageRevision,
        Object fontIdentity,
        long fontRevision,
        int guiScale,
        int viewportWidth,
        int viewportHeight,
        TooltipConfigSnapshot config,
        boolean shiftDown,
        Object worldIdentity
    ) {
        if (guiScale < 1 || viewportWidth < 0 || viewportHeight < 0) {
            throw new IllegalArgumentException(
                "GUI scale must be positive and viewport size must be non-negative"
            );
        }
        this.resourceRevision = resourceRevision;
        this.tagRevision = tagRevision;
        this.runtimeContentRevision = runtimeContentRevision;
        this.parserRevision = parserRevision;
        this.variableRevision = variableRevision;
        this.conditionRevision = conditionRevision;
        this.componentReaderRevision = componentReaderRevision;
        this.itemFingerprint = Objects.requireNonNull(
            itemFingerprint,
            "itemFingerprint"
        );
        this.languageCode = Objects.requireNonNull(
            languageCode,
            "languageCode"
        );
        this.languageRevision = languageRevision;
        this.fontIdentity = Objects.requireNonNull(
            fontIdentity,
            "fontIdentity"
        );
        this.fontRevision = fontRevision;
        this.guiScale = guiScale;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.config = Objects.requireNonNull(config, "config");
        this.shiftDown = shiftDown;
        this.worldIdentity = Objects.requireNonNull(
            worldIdentity,
            "worldIdentity"
        );
    }

    public TooltipDependencies withItemFingerprint(ItemStackFingerprint value) {
        return new TooltipDependencies(
            resourceRevision, tagRevision, runtimeContentRevision,
            parserRevision, variableRevision, conditionRevision,
            componentReaderRevision, value, languageCode,
            languageRevision, fontIdentity, fontRevision,
            guiScale, viewportWidth, viewportHeight,
            config, shiftDown, worldIdentity
        );
    }

    public TooltipDependencies withResourceRevision(long value) {
        return new TooltipDependencies(
            value, tagRevision, runtimeContentRevision,
            parserRevision, variableRevision, conditionRevision,
            componentReaderRevision, itemFingerprint, languageCode,
            languageRevision, fontIdentity, fontRevision,
            guiScale, viewportWidth, viewportHeight,
            config, shiftDown, worldIdentity
        );
    }

    public TooltipDependencies withLanguageRevision(long value) {
        return new TooltipDependencies(
            resourceRevision, tagRevision, runtimeContentRevision,
            parserRevision, variableRevision, conditionRevision,
            componentReaderRevision, itemFingerprint, languageCode,
            value, fontIdentity, fontRevision,
            guiScale, viewportWidth, viewportHeight,
            config, shiftDown, worldIdentity
        );
    }

    public TooltipDependencies withFontIdentity(Object value) {
        return new TooltipDependencies(
            resourceRevision, tagRevision, runtimeContentRevision,
            parserRevision, variableRevision, conditionRevision,
            componentReaderRevision, itemFingerprint, languageCode,
            languageRevision, value, fontRevision, guiScale, viewportWidth, viewportHeight,
            config, shiftDown, worldIdentity
        );
    }

    public TooltipDependencies withShiftDown(boolean value) {
        return new TooltipDependencies(
            resourceRevision, tagRevision, runtimeContentRevision,
            parserRevision, variableRevision, conditionRevision,
            componentReaderRevision, itemFingerprint, languageCode,
            languageRevision, fontIdentity, fontRevision, guiScale, viewportWidth, viewportHeight,
            config, value, worldIdentity
        );
    }

    public TooltipDependencies withWorldIdentity(Object value) {
        return new TooltipDependencies(
            resourceRevision, tagRevision, runtimeContentRevision,
            parserRevision, variableRevision, conditionRevision,
            componentReaderRevision, itemFingerprint, languageCode,
            languageRevision, fontIdentity, fontRevision, guiScale, viewportWidth, viewportHeight,
            config, shiftDown, value
        );
    }
}
