package com.ftc.ftcli.config.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:39:05
 * @describe AI聊天记忆配置属性
 */
@Data
@ConfigurationProperties(prefix = "ai.chat-memory")
public class AiChatMemoryProperties {

    /**
     * 最大Token数
     */
    private int maxTokens = 95000;

    /**
     * Token计数估算器模型名称
     */
    private String tokenEstimatorModel = "gpt-4o";
}
