package com.ftc.ftcli.command;

import com.ftc.ftcli.command.ai.AICommand;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 10:28:56
 * @describe 根命令
 */
@Component
@CommandLine.Command(
        name = "ftcli",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "ftc自己的命令行工具",
        subcommands = {AICommand.class}
)
public class RootCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println("请输入子命令，使用 --help 查看帮助");
        return 0;
    }
}
