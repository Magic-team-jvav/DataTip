package com.cooobird.datatip.api;

import com.cooobird.datatip.api.util.VariableResolver;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Tooltip 渲染上下文。
 * 封装 GuiGraphics、字体、动画状态和当前物品栈，提供内容渲染时需要的常用绘制方法。
 *
 * @param graphics    GuiGraphics 实例
 * @param font        字体渲染器
 * @param tickCount   当前 tick 计数
 * @param partialTick 帧间插值
 * @param itemStack   当前物品栈，用于变量解析
 * @author cooobird
 * @since 1.2.0
 */
public record TipRenderContext(GuiGraphics graphics, Font font, int tickCount, float partialTick, ItemStack itemStack) {

    /**
     * 仅合成当前内容颜色的透明度，不修改全局 Shader 状态。
     */
    public static int applyAlpha(int color, float alpha) {
        float normalizedAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
        int sourceAlpha = color >>> 24;
        if (sourceAlpha == 0) sourceAlpha = 0xFF;
        int resultAlpha = normalizedAlpha > 0.0f
            ? Math.max(1, Math.round(sourceAlpha * normalizedAlpha))
            : 0;
        return (resultAlpha << 24) | (color & 0x00FFFFFF);
    }

    /**
     * 创建不绑定物品栈的渲染上下文。
     */
    public TipRenderContext(GuiGraphics graphics, Font font, int tickCount, float partialTick) {
        this(graphics, font, tickCount, partialTick, ItemStack.EMPTY);
    }

    public void drawString(String text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    public void drawStringWithVariables(String text, int x, int y, int color) {
        drawString(resolveVariables(text), x, y, color);
    }

    public void drawString(String text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }

    public void drawString(String text, int x, int y, int color, @Nullable ResourceLocation customFont) {
        if (customFont != null) {
            Component component = Component.literal(text).withStyle(Style.EMPTY.withFont(customFont));
            graphics.drawString(font, component, x, y, color, true);
        } else {
            drawString(text, x, y, color);
        }
    }

    public void drawCenteredString(String text, int x, int y, int color) {
        int width = font.width(text);
        graphics.drawString(font, text, x - width / 2, y, color, true);
    }

    public void drawRightAlignedString(String text, int x, int y, int color) {
        int width = font.width(text);
        graphics.drawString(font, text, x - width, y, color, true);
    }

    public void drawRightAlignedString(Component text, int x, int y, int color) {
        int width = font.width(text);
        graphics.drawString(font, text, x - width, y, color, true);
    }

    public void drawString(Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    public void drawString(Component text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }

    public void drawString(FormattedCharSequence text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    public void drawString(FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }

    public void drawCenteredString(Component text, int x, int y, int color) {
        graphics.drawCenteredString(font, text, x, y, color);
    }

    public int getStringWidth(String text) {
        return font.width(text);
    }

    public int getStringWidth(Component text) {
        return font.width(text);
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y2, color);
    }

    public void fillGradientV(int x1, int y1, int x2, int y2, int colorTop, int colorBottom) {
        graphics.fillGradient(x1, y1, x2, y2, colorTop, colorBottom);
    }

    public void hLine(int x1, int x2, int y, int color) {
        graphics.fill(x1, y, x2, y + 1, color);
    }

    public void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public void blit(ResourceLocation texture, int x, int y, int width, int height,
                     float u1, float v1, int u2, int v2, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, width, height, u1, v1, u2, v2, textureWidth, textureHeight);
    }

    public void blitSprite(ResourceLocation sprite, int x, int y, int width, int height) {
        graphics.blitSprite(sprite, x, y, width, height);
    }

    public void renderItem(ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
    }

    public void renderItemScaled(ItemStack stack, int x, int y, int size) {
        graphics.pose().pushPose();
        float scale = size / 16.0f;
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    public void renderItemDecorations(ItemStack stack, int x, int y) {
        graphics.renderItemDecorations(font, stack, x, y);
    }

    public void renderItemDecorations(ItemStack stack, int x, int y, String countText) {
        graphics.renderItemDecorations(font, stack, x, y, countText);
    }

    public void renderItemWithDecorations(ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
    }

    public PoseStack pose() {
        return graphics.pose();
    }

    /**
     * 解析文本中的变量。
     */
    public String resolveVariables(String text) {
        if (text == null || itemStack.isEmpty()) {
            return text;
        }
        return VariableResolver.resolve(text, itemStack);
    }
}
