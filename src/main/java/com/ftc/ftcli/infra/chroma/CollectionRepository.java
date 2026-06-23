package com.ftc.ftcli.infra.chroma;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ftc.ftcli.properties.embedding.StoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-22 17:05:15
 * @describe 集合操作
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CollectionRepository {

    private final StoreProperties chromaProperties;

    private final ChromaUrlBuilder urlBuilder;

    /**
     * 集合ID缓存（运行期间不变，懒加载后复用）
     */
    private volatile String cachedCollectionId;

    /**
     * 获取Chroma集合ID（带缓存，首次调用时查询并缓存，后续直接返回）
     *
     * @return 集合ID，未找到返回null
     */
    public String getCollectionId() {

        //1.如果缓存已有值，直接返回
        if (StrUtil.isNotBlank(cachedCollectionId)) {
            return cachedCollectionId;
        }

        //2.查询集合ID
        String collectionId = fetchCollectionId();

        //3.缓存结果（仅在查询成功时缓存）
        if (StrUtil.isNotBlank(collectionId)) {
            cachedCollectionId = collectionId;
        }

        //4.返回
        return collectionId;
    }

    /**
     * 获取向量记录数
     *
     * @param collectionId 集合ID
     * @return 向量记录数
     */
    public int getVectorCount(String collectionId) {

        //1.查询集合记录数URL
        String recordsCountUrl = urlBuilder.buildRecordsCountUrl(collectionId);
        try {

            //2.查询集合记录数
            String countResp = HttpUtil.get(recordsCountUrl);

            //3.解析返回
            return Integer.parseInt(countResp.trim());
        } catch (Exception e) {
            log.error("[Chroma] 查询向量记录数 失败", e);
            return 0;
        }
    }

    /**
     * 从Chroma查询集合ID
     *
     * @return 集合ID，未找到返回null
     */
    private String fetchCollectionId() {

        //1.获取查询集合列表URL
        String collectionsUrl = urlBuilder.buildCollectionsUrl();
        try {

            //2.查询集合列表
            String collectionsResp = HttpUtil.get(collectionsUrl);

            //3.解析集合列表
            JSONArray collections = JSON.parseArray(collectionsResp);

            //4.查找目标集合ID
            for (int i = 0; i < collections.size(); i++) {

                //5.获取集合
                JSONObject coll = collections.getJSONObject(i);

                //6.判断集合名称是否与配置一致，如果一致，返回集合ID
                if (chromaProperties.getCollection().equals(coll.getString("name"))) {
                    return coll.getString("id");
                }
            }

            //7.未找到集合
            log.warn("[Chroma] 查询集合ID 未找到集合:[{}]", chromaProperties.getCollection());
            return null;
        } catch (Exception e) {
            log.error("[Chroma] 查询集合ID 失败", e);
            return null;
        }
    }
}
