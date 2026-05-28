package com.ftc.ftcli.command.ai;

import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.ai.service.WebAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 10:30:26
 * @describe AI命令
 */
@Component
@RequiredArgsConstructor
@CommandLine.Command(name = "ai", description = "基于DeepSeek进行AI问答", mixinStandardHelpOptions = true)
public class AICommand implements Callable<Integer> {

    //-----------------------------------------命令行参数相关-----------------------------------//
    @CommandLine.Option(names = {"-l", "--local"}, description = "基于本地文库进行回答")
    private String localUserMessage;

    @CommandLine.Option(names = {"-w", "--web"}, description = "基于web内容进行回答")
    private String webUserMessage;

    @CommandLine.Option(names = {"-u", "--user"}, description = "用户ID", defaultValue = "ftc")
    private String userId;

    @CommandLine.Option(names = {"-uf", "--update-file"}, description = "更新文库文件路径")
    private String updateFilePath;

    //-----------------------------------------Spring Bean相关----------------------------------//
    private final WebAiService webAiService;

    //-----------------------------------------具体方法相关--------------------------------------//
    @Override
    public Integer call() {

        //1.如果基于Web进行回答
        if (StrUtil.isNotBlank(webUserMessage)) {
            chatByWeb(webUserMessage);
        }

        return 0;
    }

    /**
     * 基于Web进行问答
     *
     * @param userMessage 用户消息
     */
    private void chatByWeb(String userMessage) {

        //1.打印用户信息
        System.out.println("user: " + userMessage);

        //2.调用AI服务获取回答
        String aiResponse = webAiService.chat(userId, userMessage);

        //3.打印AI回答
        System.out.println("ai: " + aiResponse);
    }
}
