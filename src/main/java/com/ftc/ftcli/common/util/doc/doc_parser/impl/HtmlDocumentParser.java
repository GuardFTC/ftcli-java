package com.ftc.ftcli.common.util.doc.doc_parser.impl;

import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.Metadata;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-10 10:43:39
 * @describe HTML文档解析器（支持Netscape Bookmark格式及通用HTML）
 */
public class HtmlDocumentParser implements DocumentParser {

    /**
     * 书签文档类型标识值
     */
    public static final String DOC_TYPE_BOOKMARK = "bookmark";

    @Override
    public Document parse(InputStream inputStream) {

        //1.解析HTML
        org.jsoup.nodes.Document htmlDoc;
        try {
            htmlDoc = Jsoup.parse(inputStream, "UTF-8", "");
        } catch (IOException e) {
            throw new RuntimeException("HTML文档解析失败", e);
        }

        //2.判断是否为书签文件（Netscape Bookmark格式）
        String title = htmlDoc.title();
        boolean isBookmark = "Bookmarks".equalsIgnoreCase(title) || "书签".equals(title);

        //3.书签文件：解析为结构化文本，并打上书签类型标记（供入库时按行切分）
        if (isBookmark) {
            String text = parseBookmark(htmlDoc);
            return Document.from(text, Metadata.from(DocMetaDataKeyEnum.DOC_TYPE.getKey(), DOC_TYPE_BOOKMARK));
        }

        //4.通用HTML：提取正文文本返回
        return Document.from(parseGenericHtml(htmlDoc));
    }

    /**
     * 解析Netscape Bookmark格式文件，提取文件夹层级和书签链接
     *
     * @param htmlDoc Jsoup文档
     * @return 结构化文本
     */
    private String parseBookmark(org.jsoup.nodes.Document htmlDoc) {

        //1.定义结果构建器
        StringBuilder result = new StringBuilder();

        //2.使用栈记录文件夹层级路径
        Deque<String> folderStack = new ArrayDeque<>();

        //3.获取所有DT元素（书签项或文件夹）
        Elements dtElements = htmlDoc.select("DT");

        //4.遍历所有DT元素
        for (Element dt : dtElements) {

            //5.判断是否为文件夹（包含H3子元素）
            Element h3 = dt.selectFirst("> H3");
            if (h3 != null) {

                //6.计算当前文件夹深度（通过嵌套DL层数）
                int depth = getDepth(dt);

                //7.调整栈深度到当前层级
                while (folderStack.size() >= depth) {
                    folderStack.pollLast();
                }

                //8.压入当前文件夹名称
                folderStack.addLast(h3.text());
                continue;
            }

            //9.判断是否为书签链接（包含A子元素）
            Element a = dt.selectFirst("> A");
            if (a != null) {

                //10.获取书签标题和URL
                String linkTitle = a.text();
                String url = a.attr("href");

                //11.构建文件夹路径
                String folderPath = String.join(" > ", folderStack);

                //12.写入结构化文本
                if (!folderPath.isEmpty()) {
                    result.append("[").append(folderPath).append("] ");
                }
                result.append(linkTitle).append(" - ").append(url).append("\n");
            }
        }

        //13.返回
        return result.toString();
    }

    /**
     * 解析通用HTML文件，提取正文文本
     *
     * @param htmlDoc Jsoup文档
     * @return 纯文本内容
     */
    private String parseGenericHtml(org.jsoup.nodes.Document htmlDoc) {

        //1.移除script和style标签
        htmlDoc.select("script, style, nav, footer, header").remove();

        //2.提取body文本内容
        Element body = htmlDoc.body();

        //3.返回文本
        return body.text();
    }

    /**
     * 计算DT元素的嵌套深度（通过父级DL数量）
     *
     * @param element 当前元素
     * @return 嵌套深度
     */
    private int getDepth(Element element) {

        //1.定义深度计数器
        int depth = 0;

        //2.向上遍历父级，统计DL数量
        Element parent = element.parent();
        while (parent != null) {
            if ("DL".equalsIgnoreCase(parent.tagName())) {
                depth++;
            }
            parent = parent.parent();
        }

        //3.返回深度
        return depth;
    }
}
