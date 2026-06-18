package com.ftc.ftcli.common.util.doc.ingestor.impl;

import com.ftc.ftcli.common.enums.doc.DocIngestorTypeEnum;
import com.ftc.ftcli.common.util.doc.ingestor.IIngestor;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-17 19:26:42
 * @describe Markdown文档切分器
 */
@Component
public class MarkdownIngestor implements IIngestor {

    /**
     * Markdown解析器
     */
    private static final Parser MD_PARSER = Parser.builder().build();

    /**
     * Markdown最大标题等级
     */
    private static final Integer MAX_MD_HEADING_LEVEL = 6;

    @Override
    public String getDocIngestorType() {
        return DocIngestorTypeEnum.MD.getType();
    }

    @Override
    public List<TextSegment> split(Document document) {

        //1.定义结果集
        List<TextSegment> segments = new ArrayList<>();

        //2.解析文档为Markdown根节点
        Node rootNode = MD_PARSER.parse(document.text());

        //3.用于追踪当前的各级标题上下文路径
        Map<Integer, String> currentHeaders = new HashMap<>();

        //4.缓存当前最底层标题下的实际内容块
        StringBuilder currentContent = new StringBuilder();

        //5.遍历根节点下的所有子节点
        Node node = rootNode.getFirstChild();
        while (node != null) {

            //6.判断是否为标题
            //如果是标题节点，添加标题树，判定是否标题结束，如果结束则创建一个TextSegment
            //如果不是标题节点，则添加正文内容到缓冲区
            if (node instanceof Heading heading) {

                //7.获取标题等级
                int level = heading.getLevel();

                //8.如果当前缓冲区有内容，则创建一个TextSegment
                //当遇到新标题时，如果之前的标题下已经积累了“实际内容”，
                //说明上一个底层结构结束了，此时再收割上一个 Chunk
                if (hasActualContent(currentContent)) {

                    //9.创建TextSegment
                    TextSegment segment = createSegment(currentHeaders, currentContent.toString(), document.metadata());

                    //10.写入结果集
                    segments.add(segment);

                    //11.清空缓冲区
                    currentContent.setLength(0);
                }

                //12.添加标题到标题树
                currentHeaders.put(level, heading.getText().toString());

                //13.更新层级树树干，清空层级更高的标题，因为新的标题会覆盖它
                for (int l = level + 1; l <= MAX_MD_HEADING_LEVEL; l++) {
                    currentHeaders.remove(l);
                }
            } else {
                currentContent.append(node.getChars()).append(System.lineSeparator());
            }

            //14.移动到下一个节点
            node = node.getNext();
        }

        //15.处理最后一个节点，如果有实际内容，则创建一个TextSegment
        if (hasActualContent(currentContent)) {
            TextSegment segment = createSegment(currentHeaders, currentContent.toString(), document.metadata());
            segments.add(segment);
        }

        //16.返回结果集
        return segments;
    }

    /**
     * 判断当前缓冲区是否包含真正的有效内容（排除只有换行或空格的空壳）
     *
     * @param currentContent 实际内容缓冲区
     * @return 是否包含有效内容
     */
    private boolean hasActualContent(StringBuilder currentContent) {
        return !currentContent.isEmpty() && !currentContent.toString().trim().isEmpty();
    }


    /**
     * 创建TextSegment
     *
     * @param headers     标题树
     * @param bodyContent 内容
     * @param docMetadata 文档元数据
     * @return TextSegment
     */
    private TextSegment createSegment(Map<Integer, String> headers, String bodyContent, Metadata docMetadata) {

        //1.定义当前TextSegment文本Builder
        StringBuilder segmentTextBuilder = new StringBuilder();

        //2.拼接标题文字 示例：一级标题 > 二级标题 > 三级标题
        List<String> activeHeaders = new ArrayList<>();
        for (int level = 1; level <= MAX_MD_HEADING_LEVEL; level++) {

            //3.如果包含对应等级标题,则添加
            if (headers.containsKey(level)) {
                activeHeaders.add(headers.get(level).trim());
            }
        }

        //4.拼接格式化标题文本
        segmentTextBuilder
                .append("- 归属: ")
                .append(String.join(" > ", activeHeaders))
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        //5.拼接具体内容文本
        segmentTextBuilder.append(bodyContent.trim());

        //6.写入原文档元数据
        Metadata segmentMetadata = new Metadata();
        if (docMetadata != null) {
            docMetadata.toMap().forEach((key, value) -> segmentMetadata.put(key, value.toString()));
        }

        //7.写入标题元数据
        headers.forEach((level, text) -> segmentMetadata.put("header_l" + level, text));

        //8.生成TextSegment，返回
        return TextSegment.from(segmentTextBuilder.toString().trim(), segmentMetadata);
    }
}
