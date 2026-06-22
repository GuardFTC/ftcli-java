package com.ftc.ftcli.common.enums.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 19:27:54
 * @describe 文档切分器类型枚举
 */
@Getter
@AllArgsConstructor
public enum DocIngestorTypeEnum {

    /**
     * Markdown文档切分器
     */
    MD("markdown"),

    /**
     * 书签文档切分器
     */
    BOOKMARK("bookmark"),

    /**
     * 通用文档切分器
     */
    UNIVERSAL("universal"),
    ;

    /**
     * 切分器类型
     */
    private final String type;
}
