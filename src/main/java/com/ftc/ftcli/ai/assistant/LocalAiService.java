package com.ftc.ftcli.ai.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:30:15
 * @describe 基于本地文档进行AI问答Service
 */
public interface LocalAiService {

    /**
     * 聊天
     *
     * @param userId      用户ID
     * @param userMessage 用户消息
     * @return 响应结果
     */
    String chat(@MemoryId String userId, @UserMessage String userMessage);
}
