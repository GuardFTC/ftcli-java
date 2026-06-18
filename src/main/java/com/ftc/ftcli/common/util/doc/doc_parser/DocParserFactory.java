package com.ftc.ftcli.common.util.doc.doc_parser;

import com.ftc.ftcli.common.enums.doc.DocParserTypeEnum;
import com.ftc.ftcli.common.util.doc.doc_parser.impl.HtmlDocumentParser;
import com.ftc.ftcli.common.util.doc.doc_parser.impl.MarkdownDocumentParser;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
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
        DocParserTypeEnum docParserTypeEnum = DocParserTypeEnum.fromType(type);

        //2.根据文档类型获取文档解析器
        return switch (docParserTypeEnum) {
            case MARKDOWN -> new MarkdownDocumentParser();
            case HTML -> new HtmlDocumentParser();
            case PDF -> new ApachePdfBoxDocumentParser(true);
            case YAML, YML -> new YamlDocumentParser();
            case TXT -> new TextDocumentParser();
            default -> null;
        };
    }
}
