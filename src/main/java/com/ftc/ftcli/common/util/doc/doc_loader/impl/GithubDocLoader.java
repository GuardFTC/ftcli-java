package com.ftc.ftcli.common.util.doc.doc_loader.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.crypto.digest.DigestUtil;
import com.ftc.ftcli.common.enums.doc.DocLoaderEnum;
import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.util.doc.DocUtil;
import com.ftc.ftcli.common.util.doc.doc_loader.IDocLoader;
import com.ftc.ftcli.common.util.doc.doc_parser.DocParserFactory;
import com.ftc.ftcli.common.util.github.GitHubUrlInfo;
import com.ftc.ftcli.common.util.github.GitHubUrlParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.github.GitHubDocumentLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 15:43:52
 * @describe Github文档加载器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GithubDocLoader implements IDocLoader {

    private final GitHubDocumentLoader githubDocumentLoader;

    @Override
    public DocLoaderEnum getType() {
        return DocLoaderEnum.GITHUB;
    }

    @Override
    public Map<String, Document> loadDocs(String url) {
        try {

            //1.进行解码
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);

            //2.解析github信息
            GitHubUrlInfo urlInfo = GitHubUrlParser.parse(decodedUrl);
            if (urlInfo == null) {
                log.error("[Github文档加载器] 解析Github信息失败");
                return Map.of();
            }

            //3.获取文档后缀
            String fileType = FileUtil.extName(urlInfo.getFilePath());

            //4.获取DocParser
            DocumentParser parser = DocParserFactory.getDocParser(fileType);
            if(null == parser){
                log.error("[Github文档加载器] 文档解析器不存在:[{}] [{}]", decodedUrl, fileType);
                return Map.of();
            }

            //5.加载文档
            Document doc = githubDocumentLoader.loadDocument(
                    urlInfo.getOwner(),
                    urlInfo.getRepo(),
                    urlInfo.getBranchOrTag(),
                    urlInfo.getFilePath(),
                    parser
            );

            //6.生成MD5
            String fileNameMD5 = DigestUtil.md5Hex(decodedUrl);

            //7.文档设置相关元数据
            doc.metadata().put(DocMetaDataKeyEnum.ABSOLUTE_DIRECTORY_PATH.getKey(), decodedUrl);
            doc.metadata().put(DocMetaDataKeyEnum.FILE_NAME.getKey(), DocUtil.getStringFromMetadata(doc, "github_file_name"));
            doc.metadata().put(DocMetaDataKeyEnum.FULL_PATH.getKey(), decodedUrl);
            doc.metadata().put(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey(), fileNameMD5);

            //8.写入Map返回
            return Map.of(fileNameMD5, doc);
        } catch (Exception e) {
            log.error("[Github文档加载器] 加载文档:[{}] 异常", url, e);
            return Map.of();
        }
    }
}
