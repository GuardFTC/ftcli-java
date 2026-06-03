package com.ftc.ftcli.entity.embedding;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import dev.langchain4j.data.document.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 20:00:00
 * @describe Embedding文档记录实体
 */
@Data
@Builder
public class EmbeddingRecordEntity {

    /**
     * 文件路径分隔符（用于拼接完整路径并参与文件名MD5计算，两处必须保持一致）
     */
    private static final String PATH_SEPARATOR = File.separator;

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
     * 获取文件名MD5并设置文档元数据
     *
     * @param doc 文档
     * @return 文件名MD5
     */
    public static String getFileNameMD5AndSetDocMetaData(Document doc) {

        //1.获取文件路径
        String absoluteDirectoryPath = doc.metadata().getString("absolute_directory_path");
        absoluteDirectoryPath = StrUtil.isBlank(absoluteDirectoryPath) ? "" : absoluteDirectoryPath.trim();

        //2.获取文件名
        String fileName = doc.metadata().getString("file_name");
        fileName = StrUtil.isBlank(fileName) ? "" : fileName.trim();

        //3.文档设置相关元数据
        doc.metadata().put("absolute_directory_path", absoluteDirectoryPath);
        doc.metadata().put("file_name", fileName);

        //4.拼接文件完整路径作为文件名，防止不同文件夹下存在同名文件
        fileName = absoluteDirectoryPath + PATH_SEPARATOR + fileName;

        //5.生成MD5
        String fileNameMD5 = DigestUtil.md5Hex(fileName);

        //6.文档设置相关元数据
        doc.metadata().put("file_name_md5", fileNameMD5);

        //7.生成MD5,返回
        return fileNameMD5;
    }

    /**
     * 文档转换为EmbeddingRecordEntity
     *
     * @param entry 文件名MD5-文档Map.Entry
     * @return EmbeddingRecordEntity
     */
    public static EmbeddingRecordEntity doc2Record(Map.Entry<String, Document> entry) {

        //1.获取文档
        Document doc = entry.getValue();

        //2.获取文件路径
        String absoluteDirectoryPath = doc.metadata().getString("absolute_directory_path");
        absoluteDirectoryPath = StrUtil.isBlank(absoluteDirectoryPath) ? "" : absoluteDirectoryPath.trim();

        //3.获取文件名
        String fileName = doc.metadata().getString("file_name");
        fileName = StrUtil.isBlank(fileName) ? "" : fileName.trim();

        //4.获取文件名MD5
        String fileNameMD5 = entry.getKey();

        //5.获取文件内容MD5
        String fileContentMD5 = getFileContentMD5(doc);

        //6.构建EmbeddingRecordEntity，返回
        return EmbeddingRecordEntity.builder()
                .fileName(fileName)
                .filePath(absoluteDirectoryPath)
                .fileNameMd5(fileNameMD5)
                .fileContentMd5(fileContentMD5)
                .build();
    }

    /**
     * 判断文档内容是否发生更新
     *
     * @param entry              文件名MD5-文档Map.Entry
     * @param existDocRecordsMap 已写入文档记录Map
     * @return 是否发生更新
     */
    public static boolean isDocContentChange(Map.Entry<String, Document> entry, Map<String, EmbeddingRecordEntity> existDocRecordsMap) {

        //1.获取文件名MD5
        String fileNameMD5 = entry.getKey();

        //2.获取本次导入的文档
        Document doc = entry.getValue();

        //3.获取导入文档内容MD5
        String fileContentMD5 = EmbeddingRecordEntity.getFileContentMD5(doc);

        //4.获取已写入的文档记录
        EmbeddingRecordEntity existDocRecord = existDocRecordsMap.get(fileNameMD5);

        //5.获取已写入文档内容MD5
        String existFileContentMD5 = existDocRecord.getFileContentMd5();

        //6.判定文档内容是否发生更新
        if (!fileContentMD5.equals(existFileContentMD5)) {

            //7.设置新增的文档内容MD5
            existDocRecord.setFileContentMd5(fileContentMD5);

            //8.返回
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取文件内容MD5
     *
     * @param doc 文档
     * @return 文件内容MD5
     */
    public static String getFileContentMD5(Document doc) {

        //1.获取文件内容
        String text = doc.text();
        text = StrUtil.isBlank(text) ? "" : text.trim();

        //2.获取文件内容MD5，返回
        return DigestUtil.md5Hex(text);
    }

    /**
     * 获取文件完整路径（文件路径 + 分隔符 + 文件名）
     *
     * @return 文件完整路径
     */
    public String getFullPath() {
        return filePath + PATH_SEPARATOR + fileName;
    }
}