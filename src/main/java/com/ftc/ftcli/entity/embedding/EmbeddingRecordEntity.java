package com.ftc.ftcli.entity.embedding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.File;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 20:00:00
 * @describe Embedding文档记录实体
 */
@Data
@Builder
public class EmbeddingRecordEntity {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "文件名（如 api-guide.pdf）")
    private String fileName;

    @Schema(description = "文件源路径（如 /docs/api-guide.pdf）")
    private String filePath;

    @Schema(description = "文件名MD5（用于快速去重判断）")
    private String fileNameMd5;

    @Schema(description = "文件内容MD5（用于判断同名文件内容是否变更）")
    private String fileContentMd5;

    @Schema(description = "首次录入时间")
    private String createdAt;

    @Schema(description = "最近更新时间")
    private String updatedAt;

    /**
     * 获取文件完整路径（文件路径 + 分隔符 + 文件名）
     *
     * @return 文件完整路径
     */
    public String getFullPath() {

        //1.如果文件路径包含文件名称，直接返回路径，否则拼接返回
        if (filePath.contains(fileName)) {
            return filePath;
        } else {
            return filePath + File.separator + fileName;
        }
    }
}