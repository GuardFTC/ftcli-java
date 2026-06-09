package com.ftc.ftcli.common.util.doc_loader;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.data.document.Document;

import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-09 11:58:21
 * @describe 文档加载器接口
 */
public interface IDocLoader {

    /**
     * 获取类型
     *
     * @return 文档加载器类型
     */
    DocLoaderEnum getType();

    /**
     * 加载文档
     *
     * @param path 文档路径/URL
     * @return 文档名称MD5-文档 Map
     */
    Map<String, Document> loadDocs(String path);

    /**
     * 通过path参数，获取对应的文档加载器类型
     *
     * @param path 文档路径/URL
     * @return 文档加载器类型
     */
    static DocLoaderEnum getTypeByPath(String path) {

        //1.防空校验
        if (StrUtil.isBlank(path)) {
            return null;
        }

        //2.清除前后空格并统一转为小写（用于特征码比对，防止大写 HTTPS 绕过）
        String cleanPath = path.trim().toLowerCase();

        //3.优先判定 GitHub（因为 GitHub 包含 https://，必须先于 WEB 拦截）
        if (cleanPath.startsWith("https://github.com/") || cleanPath.startsWith("http://github.com/")) {
            return DocLoaderEnum.GITHUB;
        }

        //4.判定普通 Web 链接
        if (cleanPath.startsWith("https://") || cleanPath.startsWith("http://")) {
            return DocLoaderEnum.URL;
        }

        //5.兜底逻辑：既不是网路链接，又不是空，说明是系统本地路径 (如 /user/data 或 C:\Users)
        return DocLoaderEnum.FILE_SYSTEM;
    }
}
