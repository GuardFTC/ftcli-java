package com.ftc.ftcli.common.enums.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 11:56:46
 * @describe 文档加载器枚举
 */
@Getter
@AllArgsConstructor
public enum DocLoaderEnum {

    /**
     * 文件系统文档加载器
     */
    FILE_SYSTEM("file_system"),

    /**
     * URL文档加载器
     */
    URL("url"),

    /**
     * GitHub文档加载器
     */
    GITHUB("github"),
    ;

    /**
     * 文档Loader类型
     */
    private final String type;
}
