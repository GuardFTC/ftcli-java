package com.ftc.ftcli.common.util.doc.doc_parser;

import com.ftc.ftcli.common.enums.doc.DocTypeEnum;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.markdown.MarkdownDocumentParser;
import dev.langchain4j.data.document.parser.yaml.YamlDocumentParser;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-08 20:24:39
 * @describe 文档解析工厂
 */
public class DocParserFactory {

    /**
     * 获取文档解析器
     *
     * @param type 文档类型
     * @return 文档解析器
     */
    public static DocumentParser getDocParser(String type) {

        //1.获取文档类型
        DocTypeEnum docTypeEnum = DocTypeEnum.fromType(type);

        //2.根据文档类型获取文档解析器
        return switch (docTypeEnum) {
            case MARKDOWN -> new MarkdownDocumentParser();
            case PDF -> new ApachePdfBoxDocumentParser(true);
            case YAML, YML -> new YamlDocumentParser();
            case DEFAULT -> new TextDocumentParser();
            default -> null;
        };
    }
}
