package com.cooobird.datatip.api.session;

/**
 * 会话内部可独立失效的缓存层。
 */
public enum TooltipInvalidation {
    DEFINITION,
    VARIABLES,
    CONDITIONS,
    LAYOUT,
    VIEWPORT,
    RENDER,
    ANIMATION,
    ENTITY
}
