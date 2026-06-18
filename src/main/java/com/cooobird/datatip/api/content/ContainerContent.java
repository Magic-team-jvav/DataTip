package com.cooobird.datatip.api.content;

import com.cooobird.datatip.api.TipContent;

import java.util.List;

/**
 * 容器内容接口。
 * 可以包含子元素的内容类型。
 *
 * @author cooobird
 * @since 1.2.0
 */
public interface ContainerContent extends TipContent {

    /**
     * 获取子元素列表（只读）。
     *
     * @return 子元素列表
     */
    List<TipContent> children();

    /**
     * 添加子元素。
     *
     * @param child 要添加的子元素
     */
    void addChild(TipContent child);
}
