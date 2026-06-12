package com.ftc.ftcli.ai.tool.executor.impl.file;

import com.alibaba.fastjson2.JSON;
import com.ftc.ftcli.ai.tool.executor.IToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProviderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.beans.Introspector;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-11 19:35:46
 * @describe 文件写入
 */
@Slf4j
@Component
public class WriteFile implements IToolExecutor {

    @Override
    public String getName() {
        return Introspector.decapitalize(this.getClass().getSimpleName());
    }

    @Override
    public boolean isMatch(ToolProviderRequest request) {
        return true;
    }

    @Override
    public ToolExecutor getToolExecutor() {
        return (toolExecutionRequest, memoryId) -> {

            //1.从request中解析LLM传入的参数
            Map<String, Object> arguments = JSON.parseObject(toolExecutionRequest.arguments());

            //2.获取文件路径参数
            String filePath = arguments.get("filePath").toString();

            //3.获取写入内容参数
            String content = arguments.get("content").toString();

            //4.获取是否追加参数
            boolean isAppend = Boolean.parseBoolean(arguments.get("isAppend").toString());

            //5.切分content
            List<String> lines = Arrays.asList(content.split(System.lineSeparator()));

            //6.写入文件，返回
            return writeFile(filePath, lines, isAppend);
        };
    }

    /**
     * 写文件
     *
     * @param filePath 文件路径
     * @param content  内容
     * @param isAppend 是否追加
     */
    private static String writeFile(String filePath, List<String> content, boolean isAppend) {

        //1.获取文件路径
        Path path = Paths.get(filePath);
        try {

            //2.获取父目录
            Path parent = path.getParent();

            //3.如果存在父目录，且该目录还不存在，则自动递归创建多级未存在的父文件夹
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }

            //3.根据参数决定是追加还是覆盖
            if (isAppend) {
                Files.write(path, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.write(path, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            //4.返回
            return "文件写入成功";
        } catch (IOException e) {
            log.error("[AI工具]-写入文件:[{}] 异常", filePath, e);
            return "文件写入失败：" + e.getMessage();
        }
    }
}
