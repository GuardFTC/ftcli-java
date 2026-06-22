package com.ftc.ftcli.config.ai.embedding;

import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.util.doc.ingestor.DocIngestorFactory;
import com.ftc.ftcli.common.util.doc.ingestor.IIngestor;
import com.ftc.ftcli.properties.embedding.IngestorProperties;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 10:42:09
 * @describe 切分器配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({IngestorProperties.class})
public class IngestorConfig {

    private final EmbeddingModel embeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    @Bean
    public EmbeddingStoreIngestor ingestor() {

        //1.定义复合切分器: 专用切分器走专门的切分规则，其余文档走通用规则
        DocumentSplitter splitter = document -> {

            //2.获取文档切分器类型
            String ingestorType = document.metadata().getString(DocMetaDataKeyEnum.INGESTOR_TYPE.getKey());

            //3.获取文档切分器
            IIngestor docIngestor = DocIngestorFactory.getDocIngestor(ingestorType);

            //4.切分文档
            return docIngestor.split(document);
        };

        //5.创建入库器，返回
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }
}
