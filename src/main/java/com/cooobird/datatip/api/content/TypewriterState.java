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
    private boolean completed;
    private boolean wasShiftDown;
    private int lastTickPc;

    void tick(TypewriterContent content, int tickPc) {
        boolean shiftDown = BaseTextContent.isShowTipDown();

        if (content.shift && shiftDown && !wasShiftDown && content.loop) {
            reset();
        }
        wasShiftDown = shiftDown;

        if (content.shift && !shiftDown) return;

        if (completed && content.loop && tickPc - lastTickPc > 2) {
            reset();
        }
        lastTickPc = tickPc;

        List<String> currentLines = TypewriterTextSource.currentLines(content);
        if (currentLines.isEmpty() || completed) return;

        tickCount++;
        if (pauseCounter > 0) {
            pauseCounter--;
            return;
        }

        int ticksPerChar = Math.max(1, 20 / content.charsPerSecond);
        if (tickCount % ticksPerChar == 0) {
            advance(currentLines, content.pauseSeconds);
        }
    }

    void reset() {
        currentLine = 0;
        currentChar = 0;
        tickCount = 0;
        pauseCounter = 0;
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
            currentChar++;
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
