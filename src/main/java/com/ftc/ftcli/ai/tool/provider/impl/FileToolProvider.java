package com.ftc.ftcli.ai.tool.provider.impl;

import com.ftc.ftcli.ai.tool.ToolTypeEnum;
import com.ftc.ftcli.ai.tool.provider.IToolProvider;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProviderRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-14 10:32:59
 * @describe 文件工具提供者
 * <p>
 * 当前阶段：工具数量少,始终提供时间工具,由LLM自行判断是否调用。
 * 后续工具增多时,可恢复isMatch()中的意图识别逻辑,避免过多工具干扰模型决策。
 */
@Component
public class FileToolProvider implements IToolProvider {

    @Override
    public boolean isMatch(ToolProviderRequest request) {
        // 当前工具少（仅时间类2个），token开销可忽略，始终提供给模型，由LLM自行决定是否调用
        // 后续工具扩展到10+时，可在此处恢复意图过滤逻辑，按需提供工具以节省token
        return true;
    }

    @Override
    public Map<ToolSpecification, ToolExecutor> getTools(Map<String, Map<ToolSpecification, ToolExecutor>> typeToolSpecToolExecutorMap) {
        return typeToolSpecToolExecutorMap.get(ToolTypeEnum.FILE.getType());
    }
}
