package com.ftc.ftcli.common.util.doc.ingestor.impl;

import com.ftc.ftcli.common.enums.doc.DocIngestorTypeEnum;
import com.ftc.ftcli.common.util.doc.ingestor.IIngestor;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 19:26:42
 * @describe 书签文档切分器
 */
@Component
public class BookmarkIngestor implements IIngestor {

    @Override
    public String getDocIngestorType() {
        return DocIngestorTypeEnum.BOOKMARK.getType();
    }

    @Override
    public List<TextSegment> split(Document document) {

        //1.定义片段集合
        List<TextSegment> segments = new ArrayList<>();

        //2.按换行符拆分
        String[] lines = document.text().split(System.lineSeparator());

        //3.遍历每行，非空行生成独立片段(携带文档元数据)
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                segments.add(TextSegment.from(trimmed, document.metadata().copy()));
            }
        }

        //4.返回
        return segments;
    }
}
