package com.ftc.ftcli.properties.rag;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-03 10:35:05
 * @describe RAG文档入库器配置属性类
 */
@Data
@ConfigurationProperties(prefix = "ai.rag.ingestor")
public class RagIngestorProperties {

    /**
     * 文档切分最大段落大小(token)
     */
    private int maxSegmentSize = 1500;

    /**
     * 文档切分重叠大小(token)
     */
    private int overlap = 200;

    /**
     * Token计数估算器模型名称
     */
    private String tokenEstimatorModel = "gpt-4o";
}
