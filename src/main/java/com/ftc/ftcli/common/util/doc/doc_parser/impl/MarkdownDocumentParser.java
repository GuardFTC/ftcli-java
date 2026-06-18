package com.ftc.ftcli.common.util.doc.doc_parser.impl;

import com.ftc.ftcli.common.enums.doc.DocIngestorTypeEnum;
import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;

import java.io.InputStream;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 19:17:28
 * @describe Markdown文档解析器
 */
public class MarkdownDocumentParser extends TextDocumentParser implements DocumentParser {

    @Override
    public Document parse(InputStream inputStream) {

        //1.调用text解析
        Document document = super.parse(inputStream);

        //2.写入元数据，表示为Markdown，便于获取特定类型的切分器
        document.metadata().put(DocMetaDataKeyEnum.INGESTOR_TYPE.getKey(), DocIngestorTypeEnum.MD.getType());

        //3.返回
        return document;
    }
}
