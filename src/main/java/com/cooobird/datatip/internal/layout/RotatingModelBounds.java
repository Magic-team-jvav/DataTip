package com.cooobird.datatip.internal.layout;

/**
 * 旋转方块和实体的保守二维投影框。
 */
public final class RotatingModelBounds {
    private static final double DIAGONAL = Math.sqrt(2.0);
    private static final double BLOCK_PITCH = Math.toRadians(35.0);
    private static final double BLOCK_HEIGHT_FACTOR =
        Math.cos(BLOCK_PITCH) + DIAGONAL * Math.sin(BLOCK_PITCH);

    private RotatingModelBounds() {
    }

    public static int boxSize(int requestedSize) {
        return scaledEnvelope(requestedSize, DIAGONAL);
    }

    /**
     * 绕 Y 轴旋转只会改变水平投影，不会放大实体的垂直投影。
     */
    public static int entityHeight(int requestedSize) {
        return requestedSize;
    }

    public static int entityVerticalInset(int requestedSize) {
        return 0;
    }

    public static int blockBoxSize(int requestedSize) {
        return scaledEnvelope(
            requestedSize,
            Math.max(DIAGONAL, BLOCK_HEIGHT_FACTOR)
        );
    }

    public static int inset(int requestedSize) {
        return Math.max(0, (boxSize(requestedSize) - requestedSize) / 2);
    }

    public static int blockInset(int requestedSize) {
        return Math.max(0, (blockBoxSize(requestedSize) - requestedSize) / 2);
    }

    private static int scaledEnvelope(int requestedSize, double factor) {
        return (int) Math.min(
            Integer.MAX_VALUE,
            Math.ceil(requestedSize * factor)
        );
    }
}
