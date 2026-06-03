package com.ftc.ftcli.properties.embedding;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-03 10:35:05
 * @describe 向量嵌入模型配置属性类
 */
@Data
@ConfigurationProperties(prefix = "ai.embedding.model.zhipu")
public class EmbeddingModelProperties {

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String modelName;
}
