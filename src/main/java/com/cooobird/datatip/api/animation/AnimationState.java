package com.cooobird.datatip.api.animation;

/**
 * 动画状态管理。
 * 用于管理轮播、打字机等动画效果。
 *
 * @author cooobird
 * @since 1.2.0
 */
public class AnimationState {

    private int tickCount;
    private int currentFrame;
    private float transitionProgress;
    private boolean active;
    private boolean paused;

    /**
     * 创建动画状态。
     */
    public AnimationState() {
        this.active = false;
        this.paused = false;
    }

    /**
     * 更新动画状态。
     *
     * @param intervalTicks 每帧间隔 tick 数
     * @param totalFrames   总帧数
     * @param loop          是否循环
     */
    public void tick(int intervalTicks, int totalFrames, boolean loop) {
        if (paused || !active) return;

        tickCount++;

        if (tickCount >= intervalTicks) {
            tickCount = 0;

            if (currentFrame < totalFrames - 1) {
                currentFrame++;
            } else if (loop) {
                currentFrame = 0;
            } else {
                active = false;
            }
        }

        // 计算过渡进度 (0.0 - 1.0)
        transitionProgress = (float) tickCount / intervalTicks;
    }

    /**
     * 重置动画状态。
     */
    public void reset() {
        tickCount = 0;
        currentFrame = 0;
        transitionProgress = 0;
        active = true;
        paused = false;
    }

    /**
     * 暂停动画。
     */
    public void pause() {
        paused = true;
    }

    /**
     * 恢复动画。
     */
    public void resume() {
        paused = false;
    }

    /**
     * 获取当前帧索引。
     */
    public int getCurrentFrame() {
        return currentFrame;
    }

    /**
     * 设置当前帧索引。
     */
    public void setCurrentFrame(int frame) {
        this.currentFrame = frame;
    }

    /**
     * 获取过渡进度 (0.0 - 1.0)。
     */
    public float getTransitionProgress() {
        return transitionProgress;
    }

    /**
     * 动画是否激活。
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 动画是否暂停。
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * 获取当前 tick 计数。
     */
    public int getTickCount() {
        return tickCount;
    }
}
