package com.ftc.ftcli.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:30:35
 * @describe Restful风格接口统一响应举类
 */
@Getter
@AllArgsConstructor
public enum RestfulResultEnum {

    /**
     * 响应成功信息
     */
    SUCCESS_MESSAGE("响应成功"),

    /**
     * 响应失败信息
     */
    ERROR_MESSAGE("响应失败.失败原因:[{}]"),
    ;

    /**
     * 响应消息
     */
    private final String message;
}
