package com.cooobird.datatip.api;

import com.cooobird.datatip.api.util.VariableResolver;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

/**
 * Tooltip 渲染上下文。
 * 封装了 {@link GuiGraphics} 和动画状态，提供便捷的绘图方法。
 *
 * @param graphics    GuiGraphics 实例
 * @param font        字体渲染器
 * @param tickCount   当前 tick 计数
 * @param partialTick 帧间插值 (0.0-1.0)
 * @param itemStack   物品栈（用于变量解析）
 * @author cooobird
 * @since 1.2.0
 */
public record TipRenderContext(GuiGraphics graphics, Font font, int tickCount, float partialTick, ItemStack itemStack) {

    /**
     * 创建渲染上下文
     *
     * @param graphics    GuiGraphics 实例
     * @param font        字体渲染器
     * @param tickCount   当前 tick 计数
     * @param partialTick 帧间插值 (0.0-1.0)
     * @param itemStack   物品栈（用于变量解析）
     */
    public TipRenderContext {
    }

    /**
     * 创建渲染上下文（无物品栈）
     */
    public TipRenderContext(GuiGraphics graphics, Font font, int tickCount, float partialTick) {
        this(graphics, font, tickCount, partialTick, ItemStack.EMPTY);
    }

    /**
     * 绘制文字（带阴影）
     *
     * @param text  文字内容
     * @param x     X 坐标
     * @param y     Y 坐标
     * @param color 颜色
     */
    public void drawString(String text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    /**
     * 绘制文字（带变量替换）
     *
     * @param text  包含变量的文字
     * @param x     X 坐标
     * @param y     Y 坐标
     * @param color 颜色
     */
    public void drawStringWithVariables(String text, int x, int y, int color) {
        String resolved = resolveVariables(text);
        graphics.drawString(font, resolved, x, y, color, true);
    }

    /**
     * 绘制文字
     *
     * @param text   文字内容
     * @param x      X 坐标
     * @param y      Y 坐标
     * @param color  颜色
     * @param shadow 是否带阴影
     */
    public void drawString(String text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }

    /**
     * 绘制居中文字（带阴影）
     *
     * @param text  文字内容
     * @param x     中心 X 坐标
     * @param y     Y 坐标
     * @param color 颜色
     */
    public void drawCenteredString(String text, int x, int y, int color) {
        int width = font.width(text);
        graphics.drawString(font, text, x - width / 2, y, color, true);
    }

    /**
     * 绘制右对齐文字（带阴影）
     *
     * @param text  文字内容
     * @param x     右边界 X 坐标
     * @param y     Y 坐标
     * @param color 颜色
     */
    public void drawRightAlignedString(String text, int x, int y, int color) {
        int width = font.width(text);
        graphics.drawString(font, text, x - width, y, color, true);
    }

    /**
     * 绘制 Component（带阴影）
     */
    public void drawString(Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    /**
     * 绘制 Component（可选阴影）
     */
    public void drawString(Component text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }

    /**
     * 绘制 FormattedCharSequence（带阴影）
     */
    public void drawString(FormattedCharSequence text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    /**
     * 绘制 FormattedCharSequence（可选阴影）
     */
    public void drawString(FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        graphics.drawString(font, text, x, y, color, shadow);
    }

    /**
     * 绘制居中 Component（带阴影）
     */
    public void drawCenteredString(Component text, int x, int y, int color) {
        graphics.drawCenteredString(font, text, x, y, color);
    }

    /**
     * 绘制自动换行文字
     */
    public void drawWordWrap(FormattedText text, int x, int y, int maxWidth, int color) {
        graphics.drawWordWrap(font, text, x, y, maxWidth, color);
    }

    /**
     * 获取文字宽度
     *
     * @param text 文字内容
     * @return 宽度（像素）
     */
    public int getStringWidth(String text) {
        return font.width(text);
    }

    /**
     * 获取 Component 宽度
     */
    public int getStringWidth(Component text) {
        return font.width(text);
    }

    /**
     * 填充矩形
     */
    public void fill(int x1, int y1, int x2, int y2, int color) {
        graphics.fill(x1, y1, x2, y2, color);
    }

    /**
     * 填充渐变矩形（垂直）
     */
    public void fillGradientV(int x1, int y1, int x2, int y2, int colorTop, int colorBottom) {
        graphics.fillGradient(x1, y1, x2, y2, colorTop, colorBottom);
    }

    /**
     * 填充渐变矩形（水平）
     */
    public void fillGradientH(int x1, int y1, int x2, int y2, int colorLeft, int colorRight) {
        // GuiGraphics 没有水平渐变，需要手动实现
        int steps = x2 - x1;
        for (int i = 0; i < steps; i++) {
            float ratio = (float) i / steps;
            int color = lerpColor(colorLeft, colorRight, ratio);
            graphics.fill(x1 + i, y1, x1 + i + 1, y2, color);
        }
    }

    /**
     * 绘制水平线
     */
    public void hLine(int x1, int x2, int y, int color) {
        graphics.fill(x1, y, x2, y + 1, color);
    }

    /**
     * 绘制垂直线
     */
    public void vLine(int x, int y1, int y2, int color) {
        graphics.fill(x, y1, x + 1, y2, color);
    }

    /**
     * 绘制矩形边框
     */
    public void drawBorder(int x, int y, int width, int height, int color) {
        // 上边
        graphics.fill(x, y, x + width, y + 1, color);
        // 下边
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        // 左边
        graphics.fill(x, y, x + 1, y + height, color);
        // 右边
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    /**
     * 绘制纹理区域
     */
    public void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    /**
     * 绘制纹理（自动计算 UV）
     */
    public void blit(ResourceLocation texture, int x, int y, int width, int height,
                     float u1, float v1, int u2, int v2, int textureWidth, int textureHeight) {
        graphics.blit(texture, x, y, width, height, u1, v1, u2, v2, textureWidth, textureHeight);
    }

    /**
     * 绘制精灵（从 TextureAtlas）
     * 注意：1.20.1 没有 blitSprite，使用 blit 替代
     */
    public void blitSprite(ResourceLocation sprite, int x, int y, int width, int height) {
        // 1.20.1: 使用 blit 替代
        graphics.blit(sprite, x, y, 0, 0, width, height, width, height);
    }

    /**
     * 渲染物品图标（16x16）
     */
    public void renderItem(ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
    }

    /**
     * 渲染物品图标（指定尺寸，通过缩放）
     */
    public void renderItemScaled(ItemStack stack, int x, int y, int size) {
        graphics.pose().pushPose();
        float scale = size / 16.0f;
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    /**
     * 渲染物品装饰（数量、耐久条等）
     * 注意：必须在 renderItem 之后调用
     */
    public void renderItemDecorations(ItemStack stack, int x, int y) {
        graphics.renderItemDecorations(font, stack, x, y);
    }

    /**
     * 渲染物品装饰（数量、耐久条等，自定义数量文字）
     */
    public void renderItemDecorations(ItemStack stack, int x, int y, String countText) {
        graphics.renderItemDecorations(font, stack, x, y, countText);
    }

    /**
     * 渲染物品图标 + 装饰（数量、耐久条等）
     */
    public void renderItemWithDecorations(ItemStack stack, int x, int y) {
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(font, stack, x, y);
    }

    /**
     * 获取当前 tick 计数
     */
    @Override
    public int tickCount() {
        return tickCount;
    }

    /**
     * 获取帧间插值
     */
    @Override
    public float partialTick() {
        return partialTick;
    }

    /**
     * 获取底层 GuiGraphics
     */
    @Override
    public GuiGraphics graphics() {
        return graphics;
    }

    /**
     * 获取字体渲染器
     */
    @Override
    public Font font() {
        return font;
    }

    /**
     * 获取 PoseStack
     */
    public PoseStack pose() {
        return graphics.pose();
    }

    /**
     * 解析文本中的变量
     */
    public String resolveVariables(String text) {
        if (text == null || itemStack.isEmpty()) {
            return text;
        }
        return VariableResolver.resolve(text, itemStack);
    }

    /**
     * 检查文本是否包含变量
     */
    public boolean hasVariables(String text) {
        return VariableResolver.hasVariables(text);
    }

    /**
     * 线性插值颜色
     */
    public static int lerpColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * 设置透明度
     */
    public void setAlpha(float alpha) {
        // 通过颜色混合实现
        // 实际实现需要在渲染时处理
    }
}
