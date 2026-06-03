package com.ftc.ftcli.ai.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:30:15
 * @describe 基于本地文档进行AI问答Service
 */
public interface LocalAiService {

    /**
     * 聊天
     *
     * @param chatId      会话ID
     * @param userMessage 用户消息
     * @return 响应结果
     */
    @SystemMessage(fromResource = "prompt/local-service.markdown")
    Result<String> chat(@MemoryId String chatId, @UserMessage String userMessage);

    /**
     * 流式聊天
     *
     * @param chatId      会话ID
     * @param userMessage 用户消息
     * @return 响应结果
     */
    @SystemMessage(fromResource = "prompt/local-service.markdown")
    Flux<String> chatStream(@MemoryId String chatId, @UserMessage String userMessage);
}
