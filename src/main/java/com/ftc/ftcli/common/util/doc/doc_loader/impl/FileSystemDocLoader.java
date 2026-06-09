package com.ftc.ftcli.common.util.doc.doc_loader.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.util.doc.DocUtil;
import com.ftc.ftcli.common.enums.doc.DocLoaderEnum;
import com.ftc.ftcli.common.util.doc.doc_loader.IDocLoader;
import com.ftc.ftcli.common.util.doc.doc_parser.DocParserFactory;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 14:11:16
 * @describe 文件系统文档加载器
 */
@Slf4j
@Component
public class FileSystemDocLoader implements IDocLoader {

    @Override
    public DocLoaderEnum getType() {
        return DocLoaderEnum.FILE_SYSTEM;
    }

    @Override
    public Map<String, Document> loadDocs(String path) {
        try {

            //1.获取文件列表
            List<File> files = getFiles(path);
            if (files.isEmpty()){
                return Map.of();
            }

            //2.获取文档列表
            List<Document> docs = getDocs(files);

            //3.文档设置元数据
            for (Document doc : docs) {
                setDocMetaData(doc);
            }

            //4.解析为文档名MD5-文档Map，返回
            return docs.stream().collect(Collectors.toMap(
                    DocUtil::getFileNameMD5,
                    doc -> doc,
                    (existing, replacement) -> replacement)
            );
        } catch (Exception e) {
            log.error("[系统文档加载器] 加载文档:[{}] 异常", path, e);
            return Map.of();
        }
    }

    /**
     * 获取文件列表
     *
     * @param path 文件路径
     * @return 文件列表
     */
    private static List<File> getFiles(String path) {

        //1.获取文档路径
        File uploadFile = Paths.get(path).toFile();

        //2.判定路径文档是否存在
        if (!uploadFile.exists()) {
            return List.of();
        }

        //3.根据文件类型是文件夹还是文件，获取文件列表，返回
        if (uploadFile.isDirectory()) {
            return FileUtil.loopFiles(uploadFile);
        } else {
            return List.of(uploadFile);
        }
    }

    /**
     * 获取文档列表
     *
     * @param files 文件列表
     * @return 文档列表
     */
    private static List<Document> getDocs(List<File> files) {

        //1.定义文档列表集合
        List<Document> documents = new ArrayList<>();

        //2.遍历文件列表
        for (File file : files) {

            //3.获取文件扩展名
            String ext = FileUtil.extName(file);

            //4.根据扩展名获取解析器
            DocumentParser parser = DocParserFactory.getDocParser(ext);

            //5.加载文档
            Document doc = FileSystemDocumentLoader.loadDocument(file.toPath(), parser);

            //6.添加文档
            documents.add(doc);
        }

        //7.返回
        return documents;
    }

    /**
     * 文档设置元数据
     *
     * @param doc 文档
     */
    private static void setDocMetaData(Document doc) {

        //1.获取文件路径
        String absoluteDirectoryPath = DocUtil.getAbsoluteDirectoryPath(doc);

        //2.获取文件名
        String fileName = DocUtil.getFileName(doc);

        //3.拼接文件完整路径作为文件名，防止不同文件夹下存在同名文件
        String fullPath = absoluteDirectoryPath + File.separator + fileName;

        //4.生成MD5
        String fileNameMD5 = DigestUtil.md5Hex(fullPath);

        //5.文档设置相关元数据
        doc.metadata().put(DocMetaDataKeyEnum.ABSOLUTE_DIRECTORY_PATH.getKey(), absoluteDirectoryPath);
        doc.metadata().put(DocMetaDataKeyEnum.FILE_NAME.getKey(), fileName);
        doc.metadata().put(DocMetaDataKeyEnum.FULL_PATH.getKey(), fullPath);
        doc.metadata().put(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey(), fileNameMD5);
    }
}
