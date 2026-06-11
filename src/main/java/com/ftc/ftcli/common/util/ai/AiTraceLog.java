package com.ftc.ftcli.common.util.ai;

import cn.hutool.core.date.DateUtil;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-10 20:00:00
 * @describe AI链路追踪日志工具类
 */
@Slf4j
public class AiTraceLog {

    /**
     * 日志前缀
     */
    private static final String PREFIX = "[AI-Trace]";

    /**
     * 默认最大长度
     */
    private static final int DEFAULT_MAX_LENGTH = 80;

    /**
     * 打印查询压缩日志
     *
     * @param query              原始查询
     * @param transformedQueries 压缩查询集合
     */
    public static void logQueryTransform(Query query, Collection<Query> transformedQueries) {
        for (Query transformed : transformedQueries) {
            log.info("{} 查询压缩: [{}] -> [{}]", PREFIX, query.text(), transformed.text());
        }
    }

    /**
     * 打印检索查询日志
     *
     * @param queryText 检索查询文本
     */
    public static void logRetrievalQuery(String queryText) {
        log.info("{} 检索查询: [{}]", PREFIX, queryText);
    }

    /**
     * 打印检索命中日志
     *
     * @param contents 检索结果
     */
    public static void logRetrievalResults(List<Content> contents) {

        //1.打印检索条数
        log.info("{} 检索命中: [{}]条", PREFIX, contents.size());

        //2.遍历检索结果
        for (Content content : contents) {

            //3.获取文件名
            String source = content.textSegment().metadata().getString("file_name");

            //4.获取文件内容，如果内容过长，省略内容，并压缩为一行
            String preview = compressString(content.textSegment().text(), DEFAULT_MAX_LENGTH);

            //5.打印日志
            log.info("{}  -来源=[{}], 内容=[{}]", PREFIX, source, preview);
        }
    }

    /**
     * 打印工具调用日志
     *
     * @param toolName  工具名称
     * @param arguments 调用参数
     */
    public static void logToolCall(String toolName, String arguments) {
        log.info("{} 工具调用: name=[{}], args=[{}]", PREFIX, toolName, arguments);
    }

    /**
     * 打印工具返回日志
     *
     * @param toolName 工具名称
     * @param result   返回结果
     */
    public static void logToolResult(String toolName, String result) {

        //1.压缩工具返回结果
        result = compressString(result, DEFAULT_MAX_LENGTH);

        //2.打印日志
        log.info("{} 工具返回: name=[{}], result=[{}]", PREFIX, toolName, result);
    }

    /**
     * 打印Token使用日志
     *
     * @param usage Token使用情况
     */
    public static void logTokenUsage(TokenUsage usage) {

        //1.判空
        if (usage == null) {
            return;
        }

        //2.日志打印
        log.info(
                "{} Token使用: input=[{}], output=[{}], total=[{}]",
                PREFIX,
                usage.inputTokenCount(),
                usage.outputTokenCount(),
                usage.totalTokenCount()
        );
    }

    /**
     * 打印总耗时日志
     *
     * @param startMillis 开始时间戳（毫秒）
     */
    public static void logTotalTime(long startMillis) {

        //1.计算总耗时
        long elapsed = System.currentTimeMillis() - startMillis;

        //2.使用 Hutool 格式化时间
        String timeStr = DateUtil.formatBetween(elapsed);

        //3.打印日志
        log.info("{} 总耗时: [{}]", PREFIX, timeStr);
    }

    /**
     * 打印异常日志
     *
     * @param message 异常信息
     */
    public static void logError(String message) {
        log.error("{} 异常: [{}]", PREFIX, message);
    }

    /**
     * 压缩字符串
     *
     * @param content 待压缩字符串
     * @return 压缩后的字符串
     */
    private static String compressString(String content, int maxLength) {

        //1.删除换行符
        String text = content.replaceAll("[\\r\\n]+", " ");

        //2.如果内容过长，则进行压缩，返回
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
