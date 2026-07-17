package com.cooobird.datatip.api.node;

import com.cooobird.datatip.api.condition.ConditionChecker;
import com.cooobird.datatip.api.layout.OverflowPolicy;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * 所有 Tooltip 内容节点共享的修饰符。
 */
public record TipModifiers(
    boolean shift,
    List<ConditionChecker.Condition> conditions,
    long offsetX,
    long offsetY,
    long offsetZ,
    SelfAlignment selfAlignX,
    VerticalAlignment selfAlignY,
    Margins margins,
    SizeConstraints sizeConstraints,
    double scaleX,
    double scaleY,
    double rotation,
    double pivotX,
    double pivotY,
    double opacity,
    boolean visible,
    OverflowPolicy overflow
) {
    public static final TipModifiers DEFAULT = new TipModifiers(
        false,
        List.of(),
        0,
        0,
        0,
        SelfAlignment.INHERIT,
        VerticalAlignment.INHERIT,
        Margins.ZERO,
        SizeConstraints.NONE,
        1.0,
        1.0,
        0.0,
        0.5,
        0.5,
        1.0,
        true,
        OverflowPolicy.NONE
    );

    public TipModifiers(
        boolean shift,
        List<ConditionChecker.Condition> conditions,
        long offsetX,
        long offsetY,
        long offsetZ,
        SelfAlignment selfAlignX,
        VerticalAlignment selfAlignY,
        Margins margins,
        SizeConstraints sizeConstraints,
        double scaleX,
        double scaleY,
        double rotation,
        double pivotX,
        double pivotY,
        double opacity,
        boolean visible,
        OverflowPolicy overflow
    ) {
        requirePositiveFinite(scaleX, "scaleX");
        requirePositiveFinite(scaleY, "scaleY");
        requireFinite(rotation, "rotation");
        requireUnitInterval(pivotX, "pivotX");
        requireUnitInterval(pivotY, "pivotY");
        requireUnitInterval(opacity, "opacity");
        this.shift = shift;
        this.conditions = List.copyOf(Objects.requireNonNull(
            conditions,
            "conditions"
        ));
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.selfAlignX = Objects.requireNonNull(selfAlignX, "selfAlignX");
        this.selfAlignY = Objects.requireNonNull(selfAlignY, "selfAlignY");
        this.margins = Objects.requireNonNull(margins, "margins");
        this.sizeConstraints = Objects.requireNonNull(
            sizeConstraints,
            "sizeConstraints"
        );
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.rotation = rotation;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.opacity = opacity;
        this.visible = visible;
        this.overflow = Objects.requireNonNull(overflow, "overflow");
    }

    public TipModifiers(
        boolean shift,
        long offsetZ,
        SelfAlignment selfAlignment
    ) {
        this(
            shift,
            List.of(),
            0,
            0,
            offsetZ,
            selfAlignment,
            VerticalAlignment.INHERIT,
            Margins.ZERO,
            SizeConstraints.NONE,
            1.0,
            1.0,
            0.0,
            0.5,
            0.5,
            1.0,
            true,
            OverflowPolicy.NONE
        );
    }

    public TipModifiers(boolean shift, long offsetZ) {
        this(shift, offsetZ, SelfAlignment.INHERIT);
    }

    public TipModifiers withConditions(
        List<ConditionChecker.Condition> newConditions
    ) {
        return new TipModifiers(
            shift,
            newConditions,
            offsetX,
            offsetY,
            offsetZ,
            selfAlignX,
            selfAlignY,
            margins,
            sizeConstraints,
            scaleX,
            scaleY,
            rotation,
            pivotX,
            pivotY,
            opacity,
            visible,
            overflow
        );
    }

    public TipModifiers withOffset(long newOffsetX, long newOffsetY) {
        return new TipModifiers(
            shift,
            conditions,
            newOffsetX,
            newOffsetY,
            offsetZ,
            selfAlignX,
            selfAlignY,
            margins,
            sizeConstraints,
            scaleX,
            scaleY,
            rotation,
            pivotX,
            pivotY,
            opacity,
            visible,
            overflow
        );
    }

    public TipModifiers withMargins(Margins newMargins) {
        return new TipModifiers(
            shift,
            conditions,
            offsetX,
            offsetY,
            offsetZ,
            selfAlignX,
            selfAlignY,
            newMargins,
            sizeConstraints,
            scaleX,
            scaleY,
            rotation,
            pivotX,
            pivotY,
            opacity,
            visible,
            overflow
        );
    }

    /**
     * 保留旧 API 名称，等价于水平自对齐。
     */
    public SelfAlignment selfAlignment() {
        return selfAlignX;
    }

    private static void requirePositiveFinite(double value, String property) {
        requireFinite(value, property);
        if (value <= 0.0) {
            throw new IllegalArgumentException(property + " must be greater than 0");
        }
    }

    private static void requireUnitInterval(double value, String property) {
        requireFinite(value, property);
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(property + " must be between 0 and 1");
        }
    }

    private static void requireFinite(double value, String property) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(property + " must be finite");
        }
    }

    /**
     * 节点自身的水平对齐方式。
     */
    public enum SelfAlignment {
        INHERIT,
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * 节点自身的垂直对齐方式。
     */
    public enum VerticalAlignment {
        INHERIT,
        TOP,
        CENTER,
        BOTTOM
    }

    /**
     * 节点四边的外边距，允许使用负值。
     */
    public record Margins(long top, long right, long bottom, long left) {
        public static final Margins ZERO = new Margins(0, 0, 0, 0);
    }

    /**
     * 节点的可选固定、最小与最大尺寸约束。
     */
    public record SizeConstraints(
        @Nullable Long width,
        @Nullable Long height,
        @Nullable Long minWidth,
        @Nullable Long minHeight,
        @Nullable Long maxWidth,
        @Nullable Long maxHeight
    ) {
        public static final SizeConstraints NONE = new SizeConstraints(
            null,
            null,
            null,
            null,
            null,
            null
        );

        public SizeConstraints {
            requireNonNegative(width, "width");
            requireNonNegative(height, "height");
            requireNonNegative(minWidth, "minWidth");
            requireNonNegative(minHeight, "minHeight");
            requireNonNegative(maxWidth, "maxWidth");
            requireNonNegative(maxHeight, "maxHeight");
            requireOrdered(minWidth, maxWidth, "minWidth", "maxWidth");
            requireOrdered(minHeight, maxHeight, "minHeight", "maxHeight");
        }

        private static void requireNonNegative(
            @Nullable Long value,
            String property
        ) {
            if (value != null && value < 0) {
                throw new IllegalArgumentException(
                    property + " must be greater than or equal to 0"
                );
            }
        }

        private static void requireOrdered(
            @Nullable Long minimum,
            @Nullable Long maximum,
            String minimumProperty,
            String maximumProperty
        ) {
            if (minimum != null && maximum != null && minimum > maximum) {
                throw new IllegalArgumentException(
                    minimumProperty + " must be less than or equal to "
                        + maximumProperty
                );
            }
        }
    }
}
