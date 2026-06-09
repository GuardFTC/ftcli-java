package com.ftc.ftcli.entity.embedding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-03 10:23:26
 * @describe 文件上传参数
 */
@Data
public class EmbeddingFileUploadPayload {

    @Schema(description = "文件路径/URL")
    private String path;
}
