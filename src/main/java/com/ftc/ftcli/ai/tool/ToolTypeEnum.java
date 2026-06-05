package com.ftc.ftcli.ai.tool;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-13 20:25:41
 * @describe 工具类型枚举
 */
@Getter
@AllArgsConstructor
public enum ToolTypeEnum {

    /**
     * 日期工具
     */
    DATE("date"),

    /**
     * 方法内部区分用户工具
     */
    MEMORY_ID("memory_id"),

    /**
     * 系统工具
     */
    SYSTEM("system"),
    ;

    /**
     * 工具类型
     */
    private final String type;
}
