package com.ftc.ftcli.config.ai;

import com.ftc.ftcli.ai.properties.ChatMemoryProperties;
import com.ftc.ftcli.ai.assistant.WebAiService;
import com.ftc.ftcli.ai.infra.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:39:05
 * @describe 智能助手配置类
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ChatMemoryProperties.class)
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
     * 聊天记忆配置属性
     */
    private final ChatMemoryProperties chatMemoryProperties;

    /**
     * 构造方法
     *
     * @param model                聊天模型
     * @param redisChatMemoryStore Redis存储
     * @param chatMemoryProperties 聊天记忆配置属性
     */
    public AiAssistantConfig(ChatModel model, RedisChatMemoryStore redisChatMemoryStore, ChatMemoryProperties chatMemoryProperties) {
        this.model = model;
        this.redisChatMemoryStore = redisChatMemoryStore;
        this.chatMemoryProperties = chatMemoryProperties;
    }

    /**
     * 创建Web问答服务
     *
     * @return webAiService
     */
    @Bean
    public WebAiService webAiService() {
        return AiServices.builder(WebAiService.class)
                .chatModel(model)
                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder()
                        .id(memoryId)
                        .maxTokens(chatMemoryProperties.getMaxTokens(), new OpenAiTokenCountEstimator(chatMemoryProperties.getTokenEstimatorModel()))
                        .chatMemoryStore(redisChatMemoryStore)
                        .build())
                .build();
    }
}
