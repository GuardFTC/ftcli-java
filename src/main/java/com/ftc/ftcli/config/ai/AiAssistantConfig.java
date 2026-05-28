package com.ftc.ftcli.config.ai;

import com.ftc.ftcli.ai.LocalAiService;
import com.ftc.ftcli.infra.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:39:05
 * @describe 智能助手配置类
 */
@Configuration
@RequiredArgsConstructor
public class AiAssistantConfig {

    /**
     * 聊天模型
     */
    private final ChatModel model;

    /**
     * Redis存储
     */
    private final RedisChatMemoryStore redisChatMemoryStore;

    /**
     * 创建本地问答服务
     *
     * @return LocalAiService
     */
    @Bean
    public LocalAiService localAiService() {
        return AiServices.builder(LocalAiService.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder()
                        .id(memoryId)
                        .maxTokens(95000, new OpenAiTokenCountEstimator("gpt-4o"))
                        .chatMemoryStore(redisChatMemoryStore)
                        .build())
                .build();
    }
}
