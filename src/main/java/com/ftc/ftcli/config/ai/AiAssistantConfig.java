package com.ftc.ftcli.config.ai;

import com.ftc.ftcli.ai.assistant.LocalAiService;
import com.ftc.ftcli.ai.assistant.WebAiService;
import com.ftc.ftcli.ai.store.SqliteChatMemoryStore;
import com.ftc.ftcli.properties.chat.ChatMemoryProperties;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.injector.ContentInjector;
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

    private final StreamingChatModel streamingModel;

    private final SqliteChatMemoryStore chatMemoryStore;

    private final ToolProvider toolProvider;

    private final QueryTransformer queryTransformer;

    private final QueryRouter webAiQueryRouter;

    private final QueryRouter localAiQueryRouter;

    public final ContentInjector contentInjector;

    private final ChatMemoryProperties chatMemoryProperties;

    @Bean
    public WebAiService webAiService() {
        return AiServices.builder(WebAiService.class)
                .chatModel(model)
                .streamingChatModel(streamingModel)
                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder()
                        .id(memoryId)
                        .maxTokens(chatMemoryProperties.getMaxTokens(), new OpenAiTokenCountEstimator(chatMemoryProperties.getTokenEstimatorModel()))
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .toolProvider(toolProvider)
                .retrievalAugmentor(DefaultRetrievalAugmentor.builder()
                        .queryTransformer(queryTransformer)
                        .queryRouter(webAiQueryRouter)
                        .build())
                .build();
    }

    @Bean
    public LocalAiService localAiService() {
        return AiServices.builder(LocalAiService.class)
                .chatModel(model)
                .streamingChatModel(streamingModel)
                .chatMemoryProvider(memoryId -> TokenWindowChatMemory.builder()
                        .id(memoryId)
                        .maxTokens(chatMemoryProperties.getMaxTokens(), new OpenAiTokenCountEstimator(chatMemoryProperties.getTokenEstimatorModel()))
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .toolProvider(toolProvider)
                .retrievalAugmentor(DefaultRetrievalAugmentor.builder()
                        .queryTransformer(queryTransformer)
                        .queryRouter(localAiQueryRouter)
                        .contentInjector(contentInjector)
                        .build())
                .build();
    }
}
