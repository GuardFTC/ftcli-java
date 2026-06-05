package com.ftc.ftcli.ai.tool.executor.system;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.ftc.ftcli.ai.tool.executor.IToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-05 15:31:33
 * @describe
 */
@Component
public class OpenFileOrDirectory implements IToolExecutor {

    @Override
    public String getName() {
        return "openFileOrDirectory";
    }

    @Override
    public ToolExecutor getToolExecutor() {
        return (toolExecutionRequest, memoryId) -> {

            //1.从request中解析LLM传入的参数
            Map<String, Object> arguments = JSON.parseObject(toolExecutionRequest.arguments());
            String path = arguments.get("path").toString();

            //2.验证文件存在
            if (!FileUtil.exist(path)) {
                return "文件/文件夹: " + path + " 不存在";
            }

            //3.获取绝对路径
            String absolutePath = FileUtil.getAbsolutePath(path);
            try {

                //4.获取系统名称
                String osName = System.getProperty("os.name").toLowerCase();

                //5.不同系统使用不同的方式打开
                if (osName.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", absolutePath});
                } else if (osName.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", absolutePath});
                } else {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", absolutePath});
                }

                return "已打开文件夹: " + absolutePath;
            } catch (Exception e) {
                return "打开文件夹: " + absolutePath + " 失败: " + e.getMessage();
            }
        };
    }
}
