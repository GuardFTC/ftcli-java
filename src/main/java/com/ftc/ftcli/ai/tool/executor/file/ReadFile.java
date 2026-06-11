package com.ftc.ftcli.ai.tool.executor.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.ftc.ftcli.ai.tool.executor.IToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-11 15:47:50
 * @describe 文件读取
 */
@Slf4j
@Component
public class ReadFile implements IToolExecutor {

    @Override
    public String getName() {
        return "readFile";
    }

    @Override
    public ToolExecutor getToolExecutor() {
        return (toolExecutionRequest, memoryId) -> {

            //1.从request中解析LLM传入的参数
            Map<String, Object> arguments = JSON.parseObject(toolExecutionRequest.arguments());

            //2.获取文件路径参数
            String filePath = arguments.get("filePath").toString();

            //3.获取起始行参数
            Object startLineParam = arguments.get("startLine");
            Long startLine = ObjectUtil.isNull(startLineParam) ? null : Long.parseLong(startLineParam.toString());

            //4.获取结束行参数
            Object endLineParam = arguments.get("endLine");
            Long endLine = ObjectUtil.isNull(endLineParam) ? null : Long.parseLong(endLineParam.toString());

            //5.判定文件是否存在
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return "未找到文件";
            }

            //6.读取文件，返回
            return readFile(filePath, startLine, endLine);
        };
    }

    /**
     * 读取文件
     *
     * @param filePath  文件路径
     * @param startLine 起始行号
     * @param endLine   结束行号
     * @return 文件内容
     */
    private static String readFile(String filePath, Long startLine, Long endLine) {

        //1.判断起止行数是否为完全缺省状态
        boolean isStartDefault = (startLine == null || startLine <= 0);
        boolean isEndDefault = (endLine == null || endLine <= 0);

        //2.根据缺省状态，设置起止行数
        long start = isStartDefault ? 1 : startLine;
        long end = isEndDefault ? Long.MAX_VALUE : endLine;
        try {

            //3.完全缺省 -> 直接使用 Hutool 读取全部
            if (isStartDefault && isEndDefault) {

                //4.读取全部
                List<String> lines = FileUtil.readUtf8Lines(filePath);

                //5.拼接回车符串，返回
                return StrUtil.join(System.lineSeparator(), lines);
            }

            //6.判断起始行是否大于结束行
            if (start > end) {
                log.error("[AI工具]-读取文件:[{}] [{}-{}] 无效的行号范围：起始行不能大于结束行", filePath, start, end);
                return "无效的行号范围：起始行不能大于结束行";
            }

            //7.局部读取，使用 Stream 保证不把无用的行加载进内存
            try (Stream<String> linesStream = Files.lines(Paths.get(filePath))) {
                List<String> lines = linesStream
                        .skip(start - 1)
                        .limit(end - start + 1)
                        .toList();

                //8.拼接回车符串，返回
                return StrUtil.join(System.lineSeparator(), lines);
            }
        } catch (IOException e) {
            log.error("[AI工具]-读取文件:[{}] [{}-{}] 异常", filePath, start, end, e);
            return "读取文件异常: " + e.getMessage();
        }
    }
}
