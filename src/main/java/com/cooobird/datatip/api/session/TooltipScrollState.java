package com.cooobird.datatip.api.session;

/**
 * 单个悬停会话拥有的纵向视口滚动状态。
 */
public final class TooltipScrollState {
    private int offset;
    private int maximum;

    public int offset() {
        return offset;
    }

    public int maximum() {
        return maximum;
    }

    public boolean scrollable() {
        return maximum > 0;
    }

    public void update(int contentHeight, int viewportHeight) {
        maximum = Math.max(0, contentHeight - viewportHeight);
        offset = Math.min(offset, maximum);
    }

    public boolean scrollBy(double delta) {
        if (!scrollable() || delta == 0) return false;
        int step = Math.max(1, (int) Math.round(Math.abs(delta) * 18.0));
        int next = delta > 0
            ? Math.max(0, offset - step)
            : Math.min(maximum, offset + step);
        if (next == offset) return false;
        offset = next;
        return true;
    }
}
