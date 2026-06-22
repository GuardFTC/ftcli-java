package com.ftc.ftcli.common.util.doc.ingestor.impl;

import com.ftc.ftcli.common.enums.doc.DocIngestorTypeEnum;
import com.ftc.ftcli.common.util.doc.ingestor.IIngestor;
import com.ftc.ftcli.properties.embedding.IngestorProperties;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-22 14:22:32
 * @describe 通用文档切分器
 */
@Component
@RequiredArgsConstructor
public class UniversalIngestor implements IIngestor {

    private final IngestorProperties ingestorProperties;

    @Override
    public String getDocIngestorType() {
        return DocIngestorTypeEnum.UNIVERSAL.getType();
    }

    @Override
    public List<TextSegment> split(Document document) {

        //1.定义通用文档切分规则
        DocumentSplitter recursiveSplitter = DocumentSplitters.recursive(
                ingestorProperties.getMaxSegmentSize(),
                ingestorProperties.getOverlap(),
                new OpenAiTokenCountEstimator(ingestorProperties.getTokenEstimatorModel())
        );

        //2.切分文档返回
        return recursiveSplitter.split(document);
    }
}
