package com.ftc.ftcli.properties.rag;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 17:31:20
 * @describe 重排配置属性类
 */
@Data
@ConfigurationProperties(prefix = "ai.rag.rerank")
public class RerankProperties {

    /**
     * Jina API Key
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model;
}
