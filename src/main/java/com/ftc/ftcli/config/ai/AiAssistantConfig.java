package com.ftc.ftcli.config.ai;

import com.ftc.ftcli.ai.assistant.WebAiService;
import com.ftc.ftcli.infra.RedisChatMemoryStore;
import com.ftc.ftcli.properties.ChatMemoryProperties;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:39:05
 * @describe 智能助手配置类
 */
@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ChatMemoryProperties.class)
public class AiAssistantConfig {

    private final ChatModel model;

    private final ChatMemoryProperties chatMemoryProperties;

    private final RedisChatMemoryStore redisChatMemoryStore;

    private final ToolProvider toolProvider;

    private final QueryRouter webAiQueryRouter;

    private final QueryTransformer queryTransformer;

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
                .toolProvider(toolProvider)
                .retrievalAugmentor(DefaultRetrievalAugmentor.builder()
                        .queryTransformer(queryTransformer)
                        .queryRouter(webAiQueryRouter)
                        .build())
                .build();
    }
}
