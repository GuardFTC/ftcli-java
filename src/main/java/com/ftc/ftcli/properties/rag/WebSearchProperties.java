package com.ftc.ftcli.properties.rag;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 17:31:20
 * @describe Web搜索配置属性类
 */
@Data
@ConfigurationProperties(prefix = "ai.rag.web-search")
public class WebSearchProperties {

    /**
     * Tavily API Key
     */
    private String apiKey;
}
