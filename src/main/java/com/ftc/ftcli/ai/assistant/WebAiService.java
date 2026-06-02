package com.ftc.ftcli.ai.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:30:48
 * @describe 基于网络查询进行AI问答
 */
public interface WebAiService {

    /**
     * 聊天
     *
     * @param chatId      会话ID
     * @param userMessage 用户消息
     * @return 响应结果
     */
    @SystemMessage(fromResource = "prompt/web-service.markdown")
    String chat(@MemoryId String chatId, @UserMessage String userMessage);
}
