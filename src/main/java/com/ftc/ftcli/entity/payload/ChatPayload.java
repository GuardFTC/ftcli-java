package com.ftc.ftcli.entity.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:41:42
 * @describe 聊天参数
 */
@Data
public class ChatPayload {

    @Schema(description = "是否基于本地文库进行问答")
    private boolean isLocal;

    @Schema(description = "会话ID")
    private String chatId;

    @Schema(description = "用户消息")
    private String userMessage;
}
