package com.ftc.ftcli.config.ai;

import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.util.ai.AiTraceLog;
import com.ftc.ftcli.properties.rag.ContentRetrieverProperties;
import com.ftc.ftcli.properties.rag.RagIngestorProperties;
import com.ftc.ftcli.properties.rag.RerankProperties;
import com.ftc.ftcli.properties.rag.WebSearchProperties;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.jina.JinaScoringModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
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

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 17:31:20
 * @describe RAG配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({WebSearchProperties.class, RagIngestorProperties.class, RerankProperties.class, ContentRetrieverProperties.class})
public class RagConfig {

    private final ChatModel model;

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final RagIngestorProperties ingestorProperties;

    private final WebSearchProperties webSearchProperties;

    private final ContentRetrieverProperties contentRetrieverProperties;

    private final RerankProperties rerankProperties;

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

        //1.创建压缩查询转换器
        CompressingQueryTransformer compressingTransformer = new CompressingQueryTransformer(model);

        //2.包装为带追踪日志的转换器
        return query -> {

            //3.获取压缩后的查询
            var transformedQueries = compressingTransformer.transform(query);

            //4.打印转换日志
            AiTraceLog.logQueryTransform(query, transformedQueries);

            //5.返回压缩后的查询
            return transformedQueries;
        };
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
                .maxResults(webSearchProperties.getMaxResults())
                .build();

        //3.包装检索器，添加追踪日志
        ContentRetriever tracedRetriever = query -> {

            //4.检索
            List<Content> contents = webSearchContentRetriever.retrieve(query);

            //5.打印检索日志
            AiTraceLog.logRetrievalQuery(query.text());
            AiTraceLog.logRetrievalResults(contents);

            //6.返回文档
            return contents;
        };

        //7.使用LLM路由：由模型自行判断用户问题是否需要联网检索，替代正则匹配
        return new LanguageModelQueryRouter(model, Map.of(
                tracedRetriever, "用于查询实时信息、最新新闻、时事热点、技术框架最新版本、产品价格、赛事结果等需要联网获取的动态内容。不要用于回答编程概念、语法规则、设计模式等稳定的知识性问题。"
        ));
    }

    @Bean
    public QueryRouter localAiQueryRouter() {

        //1.创建文档检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(contentRetrieverProperties.getMaxResults())
                .minScore(contentRetrieverProperties.getMinScore())
                .build();

        //2.包装检索器，添加追踪日志
        ContentRetriever tracedRetriever = query -> {

            //3.检索
            List<Content> contents = contentRetriever.retrieve(query);

            //4.打印检索日志
            AiTraceLog.logRetrievalQuery(query.text());
            AiTraceLog.logRetrievalResults(contents);

            //5.返回文档
            return contents;
        };

        //7.创建自定义查询路由器：默认使用文档检索器
        return query -> {

            //8.返回检索器
            return List.of(tracedRetriever);
        };
    }

    @Bean
    public ContentInjector contentInjector() {
        return DefaultContentInjector.builder()
                .metadataKeysToInclude(asList(
                        DocMetaDataKeyEnum.ABSOLUTE_DIRECTORY_PATH.getKey(),
                        DocMetaDataKeyEnum.FILE_NAME.getKey(),
                        DocMetaDataKeyEnum.FULL_PATH.getKey()
                ))
                .build();
    }

    @Bean
    public ReRankingContentAggregator contentAggregator() {

        //1.创建Jina评分模型
        JinaScoringModel scoringModel = JinaScoringModel.builder()
                .apiKey(rerankProperties.getApiKey())
                .modelName(rerankProperties.getModel())
                .build();

        //2.包装评分模型，添加追踪日志
        ScoringModel tracedScoringModel = (segments, query) -> {

            //3.调用评分
            Response<List<Double>> response = scoringModel.scoreAll(segments, query);

            //4.打印重排日志
            AiTraceLog.logRerank(query, segments, response.content(), rerankProperties.getMaxResults());

            //5.返回
            return response;
        };

        //6.定义内容聚合器，基于重排模型进行文档二次过滤
        return ReRankingContentAggregator.builder()
                .scoringModel(tracedScoringModel)
                .maxResults(rerankProperties.getMaxResults())
                .build();
    }
}
