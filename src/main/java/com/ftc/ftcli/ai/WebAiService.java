package com.ftc.ftcli.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-05-28 14:30:48
 * @describe 基于网络查询进行AI问答
 */
@AiService
public interface WebAiService {

    /**
     * 聊天
     *
     * @param memoryId    会话ID
     * @param userMessage 用户消息
     * @return 响应结果
     */
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
