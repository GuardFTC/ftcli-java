package com.ftc.ftcli.ai.tool.executor.impl.system;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.ftc.ftcli.ai.tool.executor.IToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProviderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.beans.Introspector;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-11 10:28:35
 * @describe 运行Shell
 */
@Slf4j
@Component
public class RunShell implements IToolExecutor {

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

            //2.获取命令参数
            String command = arguments.get("command").toString();

            //3.获取执行目录参数
            Object filePathParam = arguments.get("filePath");
            String filePath = ObjectUtil.isNull(filePathParam) ? System.getProperty("user.home") : filePathParam.toString();

            //4.获取超时时间参数
            Object timeoutParam = arguments.get("timeout");
            long timeout = ObjectUtil.isNull(timeoutParam) ? 10 : Long.parseLong(timeoutParam.toString());

            //5.判断操作系统是否为 Windows
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

            //6.定义命令 Windows 用 cmd  Linux/Mac 用 sh
            List<String> fullCommand = isWindows ? List.of("cmd.exe", "/c", command) : List.of("sh", "-c", command);

            //7.执行命令
            return run(fullCommand, filePath, timeout, isWindows);
        };
    }

    /**
     * 运行命令
     *
     * @param command   命令
     * @param filePath  文件路径
     * @param timeout   超时时间/秒
     * @param isWindows 是否为Windows系统
     * @return 命令执行结果
     */
    private static String run(List<String> command, String filePath, long timeout, boolean isWindows) {
        try {

            //1.构建子进程
            ProcessBuilder pb = new ProcessBuilder(command);

            //2.设置工作目录
            pb.directory(new File(filePath));

            //3.启动子进程
            Process process = pb.start();

            //4.根据系统获取编码
            String charset = isWindows ? "GBK" : "UTF-8";

            //5.创建线程读取标准输出
            StringBuilder stdout = new StringBuilder();
            Thread stdoutReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))) {
                    reader.lines().forEach(line -> stdout.append(line).append(System.lineSeparator()));
                } catch (IOException e) {
                    log.error("[AI工具]-执行Shell命令:[{}] 读取stdout异常", command, e);
                }
            });

            //6.创建线程读取标准错误输出
            StringBuilder stderr = new StringBuilder();
            Thread stderrReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), charset))) {
                    reader.lines().forEach(line -> stderr.append(line).append(System.lineSeparator()));
                } catch (IOException e) {
                    log.error("[AI工具]-执行Shell命令:[{}] 读取stderr异常", command, e);
                }
            });

            //7.设为守护线程：即使主线程意外退出，读取线程也不会阻止 JVM 关闭
            stdoutReader.setDaemon(true);
            stderrReader.setDaemon(true);

            //8.启动读取线程
            stdoutReader.start();
            stderrReader.start();

            //9.等待命令执行完成
            boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("[AI工具]-执行Shell命令:[{}] 命令执行超时（>{}s）", command, timeout);
                return "命令执行超时（>" + timeout + "s）: " + command;
            }

            //10.等待输出线程读完缓冲区剩余内容
            stdoutReader.join();
            stderrReader.join();

            //11.返回结果
            if (!stderr.isEmpty()) {
                return stderr.toString();
            } else {
                return stdout.toString();
            }
        } catch (Exception e) {
            log.error("[AI工具]-执行Shell命令:[{}] 执行异常", command, e);
            return "执行异常: " + e.getMessage();
        }
    }
}
