package com.ftc.ftcli.infra.chroma;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.infra.sqlite.EmbeddingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-22 17:21:50
 * @describe 向量记录操作
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RecordRepository {

    private final ChromaUrlBuilder urlBuilder;

    private final EmbeddingRecordRepository embeddingRecordRepository;

    private final CollectionRepository collectionRepository;

    /**
     * 获取文档片段列表
     *
     * @param id   文档ID
     * @param page 页码(从1开始)
     * @param size 每页条数
     * @return 文档片段分页结果
     */
    public Map<String, Object> getChunks(Long id, int page, int size) {

        //1.查询文档记录
        EmbeddingRecordEntity docRecord = embeddingRecordRepository.findById(id);
        if (null == docRecord) {
            log.error("[Chroma] 获取文档片段 文档不存在:[{}]", id);
            return Map.of("total", 0, "chunks", List.of());
        }

        //2.获取文件名MD5
        String fileNameMd5 = docRecord.getFileNameMd5();

        //3.获取集合ID
        String collectionId = collectionRepository.getCollectionId();

        //4.获取查询集合向量记录URL
        String getUrl = urlBuilder.getUrl(collectionId);
        try {

            //5.先查询该文档的总片段数
            int total = getChunkCount(getUrl, fileNameMd5);

            //6.计算分页偏移量
            int offset = (page - 1) * size;

            //7.构建Chroma查询请求体
            JSONObject requestBody = new JSONObject();
            JSONObject where = new JSONObject();
            where.put("file_name_md5", fileNameMd5);
            requestBody.put("where", where);
            requestBody.put("include", List.of("documents", "metadatas"));
            requestBody.put("limit", size);
            requestBody.put("offset", offset);

            //8.发起请求
            String resp = HttpUtil.post(getUrl, requestBody.toJSONString());
            JSONObject result = JSON.parseObject(resp);

            //9.解析片段列表
            JSONArray ids = result.getJSONArray("ids");
            JSONArray documents = result.getJSONArray("documents");
            JSONArray metadatas = result.getJSONArray("metadatas");

            //10.组装片段数据
            List<Map<String, Object>> chunks = new java.util.ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                Map<String, Object> chunk = new java.util.LinkedHashMap<>();
                chunk.put("id", ids.getString(i));
                chunk.put("document", documents.getString(i));
                chunk.put("metadata", metadatas.getJSONObject(i));
                chunks.add(chunk);
            }

            //11.返回分页结果
            return Map.of("total", total, "chunks", chunks);
        } catch (Exception e) {
            log.error("[Chroma] 获取文档片段 失败 文档ID:[{}]", id, e);
            return Map.of("total", 0, "chunks", List.of());
        }
    }

    /**
     * 获取指定文档的片段总数
     *
     * @param getUrl      查询集合向量记录URL
     * @param fileNameMd5 文件名MD5
     * @return 片段总数
     */
    private int getChunkCount(String getUrl, String fileNameMd5) {

        //1.构建计数请求体（不限制数量，只取ID用于计数）
        JSONObject requestBody = new JSONObject();
        requestBody.put("include", List.of());
        JSONObject where = new JSONObject();
        where.put("file_name_md5", fileNameMd5);
        requestBody.put("where", where);

        //2.发起请求
        String resp = HttpUtil.post(getUrl, requestBody.toJSONString());
        JSONObject result = JSON.parseObject(resp);

        //3.解析ID数组长度作为总数
        JSONArray ids = result.getJSONArray("ids");
        return ids != null ? ids.size() : 0;
    }
}
