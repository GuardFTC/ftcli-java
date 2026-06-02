package com.ftc.ftcli.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ftc.ftcli.ai.assistant.WebAiService;
import com.ftc.ftcli.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:23:55
 * @describe AI问答Service实现类
 */
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final WebAiService webAiService;

    @Override
    public String getChatId() {
        return IdUtil.fastSimpleUUID();
    }
}
