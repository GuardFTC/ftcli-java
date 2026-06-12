package com.ftc.ftcli.entity.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-12 16:00:00
 * @describe 聊天记录实体
 */
@Data
@Builder
public class ChatMemoryEntity {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "会话ID")
    private String memoryId;

    @Schema(description = "聊天消息（JSON序列化）")
    private String chatMessage;
}
