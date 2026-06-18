package com.ftc.ftcli.common.enums.doc;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 11:38:03
 * @describe 文档元数据Key枚举
 */
@Getter
@AllArgsConstructor
public enum DocMetaDataKeyEnum {

    /**
     * 文档绝对路径
     */
    ABSOLUTE_DIRECTORY_PATH("absolute_directory_path"),

    /**
     * 文档文件名
     */
    FILE_NAME("file_name"),

    /**
     * 文档完整路径
     */
    FULL_PATH("full_path"),

    /**
     * 文档文件名MD5
     */
    FILE_NAME_MD5("file_name_md5"),

    /**
     * 文档切分器类型 (用于标识需要特殊切分规则的文档)
     */
    INGESTOR_TYPE("ingestor_type");

    /**
     * 文档元数据Key
     */
    private final String key;
}
