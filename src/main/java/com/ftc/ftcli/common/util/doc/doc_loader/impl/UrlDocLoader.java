package com.ftc.ftcli.common.util.doc.doc_loader.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.enums.doc.DocLoaderEnum;
import com.ftc.ftcli.common.util.doc.doc_loader.IDocLoader;
import com.ftc.ftcli.common.util.doc.doc_parser.DocParserFactory;
import com.ftc.ftcli.common.enums.doc.DocTypeEnum;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 14:27:37
 * @describe URL文档加载器
 */
@Slf4j
@Component
public class UrlDocLoader implements IDocLoader {

    @Override
    public DocLoaderEnum getType() {
        return DocLoaderEnum.URL;
    }

    @Override
    public Map<String, Document> loadDocs(String url) {
        try {

            //1.进行解码
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);

            //2.获取文件名
            String fileName = FileUtil.getName(decodedUrl);

            //3.获取文件类型
            String fileType = FileUtil.extName(fileName);

            //4.如果文件类型为空,设置为默认类型
            if (StrUtil.isBlank(fileType)) {

                //5.设置文件类型为默认类型,文件名拼接类型，url拼接类型
                fileType = DocTypeEnum.DEFAULT.getType();
                fileName = fileName + StrUtil.DOT + fileType;
                decodedUrl = decodedUrl + StrUtil.DOT + fileType;
            }

            //6.根据文件类型获取解析器
            DocumentParser parser = DocParserFactory.getDocParser(fileType);

            //7.加载文档
            Document doc = UrlDocumentLoader.load(url, parser);

            //8.生成MD5
            String fileNameMD5 = DigestUtil.md5Hex(decodedUrl);

            //9.文档设置相关元数据
            doc.metadata().put(DocMetaDataKeyEnum.ABSOLUTE_DIRECTORY_PATH.getKey(), decodedUrl);
            doc.metadata().put(DocMetaDataKeyEnum.FILE_NAME.getKey(), fileName);
            doc.metadata().put(DocMetaDataKeyEnum.FULL_PATH.getKey(), decodedUrl);
            doc.metadata().put(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey(), fileNameMD5);

            //10.写入Map返回
            return Map.of(fileNameMD5, doc);
        } catch (Exception e) {
            log.error("[URL文档加载器] 加载文档:[{}] 异常", url, e);
            return Map.of();
        }
    }
}
