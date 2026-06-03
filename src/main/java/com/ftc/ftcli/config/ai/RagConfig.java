package com.ftc.ftcli.config.ai;

import com.ftc.ftcli.properties.rag.RagIngestorProperties;
import com.ftc.ftcli.properties.rag.WebSearchProperties;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 17:31:20
 * @describe RAG配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({WebSearchProperties.class, RagIngestorProperties.class})
public class RagConfig {

    private final ChatModel model;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final WebSearchProperties webSearchProperties;

    private final RagIngestorProperties ingestorProperties;

    @Bean
    public EmbeddingStoreIngestor ingestor() {

        //1.定义文档切分规则
        DocumentSplitter splitter = DocumentSplitters.recursive(
                ingestorProperties.getMaxSegmentSize(),
                ingestorProperties.getOverlap(),
                new OpenAiTokenCountEstimator(ingestorProperties.getTokenEstimatorModel())
        );

        //2.创建入库器，返回
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    @Bean
    public QueryTransformer queryTransformer() {
        return new CompressingQueryTransformer(model);
    }

    @Bean
    public QueryRouter webAiQueryRouter() {

        //1.创建 Web 搜索引擎
        WebSearchEngine webSearchEngine = TavilyWebSearchEngine.builder()
                .apiKey(webSearchProperties.getApiKey())
                .build();

        //2.创建网络内容检索器
        ContentRetriever webSearchContentRetriever = WebSearchContentRetriever.builder()
                .webSearchEngine(webSearchEngine)
                .maxResults(3)
                .build();

        //3.使用LLM路由：由模型自行判断用户问题是否需要联网检索，替代正则匹配
        return new LanguageModelQueryRouter(model, Map.of(
                webSearchContentRetriever, "用于查询实时信息、最新新闻、时事热点、技术框架最新版本、产品价格、赛事结果等需要联网获取的动态内容。不要用于回答编程概念、语法规则、设计模式等稳定的知识性问题。"
        ));
    }
}
