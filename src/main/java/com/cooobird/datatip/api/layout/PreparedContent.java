package com.cooobird.datatip.api.layout;

import com.cooobird.datatip.api.TipContent;

/**
 * 原生支持一次性布局快照的 V2 内容协议。
 */
public interface PreparedContent extends TipContent {
    @Override
    PreparedLayout prepare(TipPrepareContext context);
}
