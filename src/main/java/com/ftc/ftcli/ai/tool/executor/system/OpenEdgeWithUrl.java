package com.ftc.ftcli.ai.tool.executor.system;

import com.alibaba.fastjson2.JSON;
import com.ftc.ftcli.ai.tool.executor.IToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-10 10:20:58
 * @describe 访问网页
 */
@Slf4j
@Component
public class OpenEdgeWithUrl implements IToolExecutor {

    @Override
    public String getName() {
        return "openEdgeWithUrl";
    }

    @Override
    public ToolExecutor getToolExecutor() {
        return (toolExecutionRequest, memoryId) -> {

            //1.从request中解析LLM传入的参数
            Map<String, Object> arguments = JSON.parseObject(toolExecutionRequest.arguments());
            String url = arguments.get("url").toString();

            //2.区分操作系统执行命令（Edge 常见于 Windows 和 Mac）
            String os = System.getProperty("os.name").toLowerCase();
            try {

                //3.根据不同系统执行命令
                if (os.contains("win")) {
                    new ProcessBuilder("cmd", "/c", "start msedge " + url).start();
                } else if (os.contains("mac")) {
                    new ProcessBuilder("open", "-a", "Microsoft Edge", url).start();
                } else {
                    log.error("[AI工具]-访问网站 暂不支持当前操作系统:[{}]自动打开 Edge", os);
                    return "暂不支持当前操作系统:[" + os + "]自动打开 Edge";
                }
            } catch (IOException e) {
                log.error("[AI工具]-访问网站 打开 Edge 浏览器失败:[{}]", url, e);
                return "打开 Edge 浏览器失败:[" + url + "]";
            }

            //4.成功返回
            return "已打开网站: " + url;
        };
    }
}
