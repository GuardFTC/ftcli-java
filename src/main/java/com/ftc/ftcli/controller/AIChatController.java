package com.ftc.ftcli.controller;

import com.ftc.ftcli.entity.payload.ChatPayload;
import com.ftc.ftcli.entity.result.RestfulResult;
import com.ftc.ftcli.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:19:28
 * @describe AI聊天控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "AI聊天", description = "AI聊天控制器")
@RequestMapping("/api/rest/v1/ai")
public class AIChatController {

    private final AiChatService aiChatService;

    @GetMapping("/chatId")
    @Operation(summary = "获取会话ID")
    public RestfulResult<String> getChatId() {

        //1.获取会话ID
        String chatId = aiChatService.getChatId();
        log.info("[AI] 获取会话ID 出参:[{}]", chatId);

        //2.返回
        return RestfulResult.Success.getOrUpdateData(chatId);
    }

    @PostMapping("/chat")
    @Operation(summary = "AI聊天")
    public RestfulResult<String> chat(@RequestBody ChatPayload payload) {
        return null;
    }
}
