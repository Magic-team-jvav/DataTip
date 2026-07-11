package com.cooobird.datatip.api.content;

import java.util.List;

/**
 * 打字机动画状态。
 */
final class TypewriterState {
    private int currentLine;
    private int currentChar;
    private int tickCount;
    private int pauseCounter;
    private double charAccumulator;
    private boolean completed;
    private boolean wasShiftDown;
    private int lastGameTick = Integer.MIN_VALUE;

    void tick(TypewriterContent content, int tickPc) {
        boolean shiftDown = BaseTextContent.isShowTipDown();

        if (content.shift && shiftDown && !wasShiftDown && content.loop) {
            resetAnimation();
        }
        wasShiftDown = shiftDown;

        if (content.shift && !shiftDown) return;

        if (tickPc == lastGameTick) return;

        boolean resumedAfterGap = lastGameTick != Integer.MIN_VALUE && tickPc - lastGameTick > 2;
        if (completed && content.loop && resumedAfterGap) {
            resetAnimation();
        }

        int elapsedTicks = lastGameTick == Integer.MIN_VALUE || resumedAfterGap
            ? 1
            : Math.max(1, tickPc - lastGameTick);
        lastGameTick = tickPc;

        List<String> currentLines = TypewriterTextSource.currentLines(content);
        if (currentLines.isEmpty() || completed) return;

        tickCount += elapsedTicks;
        if (pauseCounter > 0) {
            pauseCounter = Math.max(0, pauseCounter - elapsedTicks);
            if (pauseCounter > 0) return;
        }

        charAccumulator += content.charsPerSecond * elapsedTicks / 20.0;
        int charsToAdvance = (int) charAccumulator;
        charAccumulator -= charsToAdvance;
        while (charsToAdvance-- > 0 && !completed && pauseCounter == 0) {
            advance(currentLines, content.pauseSeconds);
        }
    }

    void reset() {
        resetAnimation();
        lastGameTick = Integer.MIN_VALUE;
    }

    private void resetAnimation() {
        currentLine = 0;
        currentChar = 0;
        tickCount = 0;
        pauseCounter = 0;
        charAccumulator = 0;
        completed = false;
    }

    int currentLine() {
        return currentLine;
    }

    int currentChar() {
        return currentChar;
    }

    int tickCount() {
        return tickCount;
    }

    boolean completed() {
        return completed;
    }

    private void advance(List<String> currentLines, int pauseSeconds) {
        if (currentLine >= currentLines.size()) {
            return;
        }

        String currentLineText = currentLines.get(currentLine);
        if (currentChar < currentLineText.length()) {
            currentChar = currentLineText.offsetByCodePoints(currentChar, 1);
            return;
        }

        currentLine++;
        currentChar = 0;
        if (currentLine >= currentLines.size()) {
            // loop:true 不持续循环，由重新悬停/展开触发重置。
            completed = true;
        } else {
            pauseCounter = pauseSeconds * 20;
        }
    }
}
