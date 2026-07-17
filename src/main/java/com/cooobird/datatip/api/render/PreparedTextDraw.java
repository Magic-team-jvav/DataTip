package com.cooobird.datatip.api.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/**
 * 已完成换行、样式和坐标计算的原版文字阶段绘制动作。
 */
@FunctionalInterface
public interface PreparedTextDraw {
    void render(
        Font font,
        int x,
        int y,
        Matrix4f matrix,
        MultiBufferSource.BufferSource bufferSource,
        float alpha
    );
}
