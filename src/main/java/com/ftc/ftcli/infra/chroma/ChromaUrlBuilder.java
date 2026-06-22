package com.ftc.ftcli.infra.chroma;

import com.ftc.ftcli.properties.embedding.StoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-22 17:12:10
 * @describe Chroma URL拼接器
 */
@Component
@RequiredArgsConstructor
public class ChromaUrlBuilder {

    private final StoreProperties chromaProperties;

    /**
     * 查询集合列表URL
     *
     * @return 查询集合列表URL
     */
    public String collectionsUrl() {
        return chromaProperties.getUrl()
                + "/api/v2/tenants/"
                + chromaProperties.getTenant()
                + "/databases/"
                + chromaProperties.getDatabase()
                + "/collections";
    }

    /**
     * 查询集合记录数URL
     *
     * @param collectionId 集合ID
     * @return 查询集合记录数URL
     */
    public String recordsCountUrl(String collectionId) {
        return chromaProperties.getUrl()
                + "/api/v2/tenants/"
                + chromaProperties.getTenant()
                + "/databases/"
                + chromaProperties.getDatabase()
                + "/collections/"
                + collectionId
                + "/count";
    }

    /**
     * 查询集合向量记录URL
     *
     * @param collectionId 集合ID
     * @return 查询集合向量记录URL
     */
    public String getUrl(String collectionId) {
        return chromaProperties.getUrl()
                + "/api/v2/tenants/"
                + chromaProperties.getTenant()
                + "/databases/"
                + chromaProperties.getDatabase()
                + "/collections/"
                + collectionId
                + "/get";
    }
}
