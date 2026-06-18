package com.ftc.ftcli.common.util.doc.ingestor;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 19:24:54
 * @describe 切分器接口
 */
public interface IIngestor {

    /**
     * 获取切分器类型
     *
     * @return 文档类型
     */
    String getDocIngestorType();

    /**
     * 切分文档
     *
     * @param document 文档
     * @return 文档片段集合
     */
    List<TextSegment> split(Document document);
}
