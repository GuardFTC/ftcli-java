package com.ftc.ftcli.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ftc.ftcli.ai.assistant.WebAiService;
import com.ftc.ftcli.entity.payload.ChatPayload;
import com.ftc.ftcli.service.AiChatService;
import dev.langchain4j.service.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:23:55
 * @describe AI问答Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatServiceImpl implements AiChatService {

    private final WebAiService webAiService;

    @Override
    public String getChatId() {
        return IdUtil.fastSimpleUUID();
    }

    @Override
    public String chat(ChatPayload payload) {
        if (payload.isLocal()) {
            return "暂不支持本地问答";
        } else {
            return chatByWebAi(payload);
        }
    }

    /**
     * 通过网络问答
     *
     * @param payload 聊天参数
     * @return 响应结果
     */
    private String chatByWebAi(ChatPayload payload) {

        //1.进行聊天
        Result<String> aiResult = webAiService.chat(payload.getChatId(), payload.getUserMessage());

        //2.打印相关日志，后续有需要的再补充
        log.info("[AI] Token使用情况:[{}]", aiResult.tokenUsage());

        //3.返回
        return aiResult.content();
    }
}
