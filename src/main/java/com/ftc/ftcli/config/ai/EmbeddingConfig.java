package com.ftc.ftcli.config.ai;

import com.ftc.ftcli.properties.embedding.EmbeddingModelProperties;
import com.ftc.ftcli.properties.embedding.EmbeddingStoreProperties;
import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-03 10:35:05
 * @describe AI向量嵌入配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({EmbeddingModelProperties.class, EmbeddingStoreProperties.class})
public class EmbeddingConfig {

    private final EmbeddingModelProperties modelProperties;

    private final EmbeddingStoreProperties storeProperties;

    @Bean
    public EmbeddingModel embeddingModel() {
        return ZhipuAiEmbeddingModel.builder()
                .apiKey(modelProperties.getApiKey())
                .model(modelProperties.getModelName())
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(storeProperties.getUrl())
                .apiVersion(ChromaApiVersion.V2)
                .tenantName(storeProperties.getTenant())
                .databaseName(storeProperties.getDatabase())
                .collectionName(storeProperties.getCollection())
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
