package com.ftc.ftcli.common.util.doc;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import dev.langchain4j.data.document.Document;

import java.io.File;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-04 14:20:53
 * @describe 文档工具类
 */
public class DocUtil {

    /**
     * 获取文件名MD5并设置文档元数据
     *
     * @param doc 文档
     * @return 文件名MD5
     */
    public static String getFileNameMD5AndSetDocMetaData(Document doc) {

        //1.获取文件路径
        String absoluteDirectoryPath = getAbsoluteDirectoryPath(doc);

        //2.获取文件名
        String fileName = getFileName(doc);

        //3.拼接文件完整路径作为文件名，防止不同文件夹下存在同名文件
        String fullPath = absoluteDirectoryPath + File.separator + fileName;

        //4.生成MD5
        String fileNameMD5 = DigestUtil.md5Hex(fullPath);

        //5.文档设置相关元数据
        doc.metadata().put(DocMetaDataKeyEnum.ABSOLUTE_DIRECTORY_PATH.getKey(), absoluteDirectoryPath);
        doc.metadata().put(DocMetaDataKeyEnum.FILE_NAME.getKey(), fileName);
        doc.metadata().put(DocMetaDataKeyEnum.FULL_PATH.getKey(), fullPath);
        doc.metadata().put(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey(), fileNameMD5);

        //6.生成MD5,返回
        return fileNameMD5;
    }

    /**
     * 文档转换为EmbeddingRecordEntity
     *
     * @param docEntry 文件名MD5-文档Map.Entry
     * @return EmbeddingRecordEntity
     */
    public static EmbeddingRecordEntity doc2Record(Map.Entry<String, Document> docEntry) {

        //1.获取文档
        Document doc = docEntry.getValue();

        //2.获取文件路径
        String absoluteDirectoryPath = getAbsoluteDirectoryPath(doc);

        //3.获取文件名
        String fileName = getFileName(doc);

        //4.获取文件名MD5
        String fileNameMD5 = docEntry.getKey();

        //5.获取文件内容MD5
        String fileContentMD5 = getFileContentMD5(doc);

        //6.构建EmbeddingRecordEntity，返回
        return EmbeddingRecordEntity.builder()
                .filePath(absoluteDirectoryPath)
                .fileName(fileName)
                .fileNameMd5(fileNameMD5)
                .fileContentMd5(fileContentMD5)
                .build();
    }

    /**
     * 判断文档内容是否发生更新
     *
     * @param docEntry           文件名MD5-文档Map.Entry
     * @param existDocRecordsMap 已写入文档记录Map
     * @return 是否发生更新
     */
    public static boolean isDocContentChange(Map.Entry<String, Document> docEntry, Map<String, EmbeddingRecordEntity> existDocRecordsMap) {

        //1.获取文件名MD5
        String fileNameMD5 = docEntry.getKey();

        //2.获取本次导入的文档
        Document doc = docEntry.getValue();

        //3.获取导入文档内容MD5
        String fileContentMD5 = getFileContentMD5(doc);

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
        String text = StrUtil.isBlank(doc.text()) ? "" : doc.text().trim();

        //2.获取文件内容MD5，返回
        return DigestUtil.md5Hex(text);
    }

    /**
     * 获取文档绝对路径
     *
     * @param doc 文档
     * @return 文档绝对路径
     */
    public static String getAbsoluteDirectoryPath(Document doc) {
        return getStringFromMetadata(doc, DocMetaDataKeyEnum.ABSOLUTE_DIRECTORY_PATH.getKey());
    }

    /**
     * 获取文档文件名
     *
     * @param doc 文档
     * @return 文档文件名
     */
    public static String getFileName(Document doc) {
        return getStringFromMetadata(doc, DocMetaDataKeyEnum.FILE_NAME.getKey());
    }

    /**
     * 获取文档元数据字符串
     *
     * @param doc 文档
     * @param key 元数据键
     * @return 元数据字符串
     */
    public static String getStringFromMetadata(Document doc, String key) {

        //1.获取元数据
        String metadataString = doc.metadata().getString(key);

        //2.判空返回
        return StrUtil.isBlank(metadataString) ? StrUtil.EMPTY : metadataString.trim();
    }
}
