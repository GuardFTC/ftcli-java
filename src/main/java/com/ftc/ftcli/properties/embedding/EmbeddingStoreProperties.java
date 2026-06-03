package com.ftc.ftcli.properties.embedding;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-03 10:35:05
 * @describe 向量存储配置属性类
 */
@Data
@ConfigurationProperties(prefix = "ai.embedding.store.chroma")
public class EmbeddingStoreProperties {

    /**
     * Chroma服务URL
     */
    private String url;

    /**
     * 租户名称
     */
    private String tenant;

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 集合名称
     */
    private String collection;
}
