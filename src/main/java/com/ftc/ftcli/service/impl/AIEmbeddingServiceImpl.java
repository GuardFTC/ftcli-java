package com.ftc.ftcli.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ftc.ftcli.common.enums.doc.DocLoaderEnum;
import com.ftc.ftcli.common.enums.doc.DocMetaDataKeyEnum;
import com.ftc.ftcli.common.util.doc.DocUtil;
import com.ftc.ftcli.common.util.doc.doc_loader.DocLoaderFactory;
import com.ftc.ftcli.common.util.doc.doc_loader.IDocLoader;
import com.ftc.ftcli.entity.embedding.EmbeddingFileUploadPayload;
import com.ftc.ftcli.entity.embedding.EmbeddingFileUploadResult;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.infra.sqlite.EmbeddingRecordRepository;
import com.ftc.ftcli.properties.embedding.StoreProperties;
import com.ftc.ftcli.service.AIEmbeddingService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:23:55
 * @describe AI向量嵌入Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIEmbeddingServiceImpl implements AIEmbeddingService {

    private final EmbeddingStoreIngestor ingestor;

    private final EmbeddingStore<TextSegment> embeddingStore;

    private final EmbeddingRecordRepository embeddingRecordRepository;

    private final StoreProperties chromaProperties;

    @Override
    public List<EmbeddingRecordEntity> getDocs() {
        return embeddingRecordRepository.findAll();
    }

    @Override
    public EmbeddingFileUploadResult upload(EmbeddingFileUploadPayload payload) {

        //1.获取文档路径
        String path = payload.getPath();
        if (StrUtil.isBlank(path)) {
            log.error("[AI] 新增文档 文档路径不能为空");
            return new EmbeddingFileUploadResult();
        }

        //2.通过path获取文档加载类型
        DocLoaderEnum docLoadEnum = IDocLoader.getTypeByPath(path);

        //3.通过类型获取文档加载器
        IDocLoader docLoader = DocLoaderFactory.getDocLoader(docLoadEnum);

        //4.加载文档
        Map<String, Document> docsMap = docLoader.loadDocs(path);
        if (CollUtil.isEmpty(docsMap)) {
            log.error("[AI] 新增文档 文档不存在:[{}]", path);
            return new EmbeddingFileUploadResult();
        } else {
            log.info("[AI] 新增文档 加载文档数量:[{}]", docsMap.size());
        }

        //5.获取已存在文档记录Map
        Map<String, EmbeddingRecordEntity> existDocRecordsMap = getExistDocRecordsMap(docsMap);

        //6.按是否已存在分组文档
        Map<Boolean, Map<String, Document>> partitionedDocsMap = partitionedDocsMap(existDocRecordsMap, docsMap);

        //7.获取新增文档
        Map<String, Document> newDocsMap = partitionedDocsMap.getOrDefault(false, Map.of());
        log.info("[AI] 新增文档 新增文档数量:[{}]", newDocsMap.size());

        //8.新增文档，写入文档记录
        List<String> newFiles = addNewDocs(newDocsMap);

        //9.获取已存在文档
        Map<String, Document> existDocsMap = partitionedDocsMap.getOrDefault(true, Map.of());
        log.info("[AI] 新增文档 已存在文档数量:[{}]", existDocsMap.size());

        //10.过滤出文档内容发生更新的文档名称MD5
        Set<String> updateDocsNameSet = existDocsMap.entrySet()
                .stream()
                .filter(entry -> DocUtil.isDocContentChange(entry, existDocRecordsMap))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        log.info("[AI] 新增文档 文档内容更新数量:[{}]", updateDocsNameSet.size());

        //11.已存在文档，如果文档内容发生更新，写入文档记录
        List<String> updateFiles = updateChangeDocs(updateDocsNameSet, existDocsMap, existDocRecordsMap);

        //12.构建结果返回
        return new EmbeddingFileUploadResult(newFiles, updateFiles);
    }

    @Override
    public void remove(Long id) {

        //1.查询文档记录
        EmbeddingRecordEntity docRecord = embeddingRecordRepository.findById(id);
        if (null == docRecord) {
            log.error("[AI] 删除文档 文档不存在:[{}]", id);
            return;
        }

        //2.删除向量数据库向量
        Filter filter = metadataKey(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()).isEqualTo(docRecord.getFileNameMd5());
        embeddingStore.removeAll(filter);

        //3.删除文档记录
        embeddingRecordRepository.deleteById(id);
    }

    @Override
    public int getVectorCount() {

        //1.定义查询集合列表URL
        String collectionsUrl = chromaProperties.getUrl()
                + "/api/v2/tenants/"
                + chromaProperties.getTenant()
                + "/databases/"
                + chromaProperties.getDatabase()
                + "/collections";
        try {

            //2.查询集合列表
            String collectionsResp = HttpUtil.get(collectionsUrl);
            JSONArray collections = JSON.parseArray(collectionsResp);

            //3.查找目标集合ID
            String collectionId = null;
            for (int i = 0; i < collections.size(); i++) {
                JSONObject coll = collections.getJSONObject(i);
                if (chromaProperties.getCollection().equals(coll.getString("name"))) {
                    collectionId = coll.getString("id");
                    break;
                }
            }

            //4.如果未找到集合，返回0
            if (collectionId == null) {
                log.warn("[AI] 查询向量记录数 未找到集合:[{}]", chromaProperties.getCollection());
                return 0;
            }

            //5.定义查询集合记录数URL
            String countUrl = chromaProperties.getUrl()
                    + "/api/v2/tenants/"
                    + chromaProperties.getTenant()
                    + "/databases/"
                    + chromaProperties.getDatabase()
                    + "/collections/"
                    + collectionId
                    + "/count";

            //6.查询集合记录数
            String countResp = cn.hutool.http.HttpUtil.get(countUrl);

            //7.解析返回
            return Integer.parseInt(countResp.trim());
        } catch (Exception e) {
            log.error("[AI] 查询向量记录数失败", e);
            return 0;
        }
    }

    @Override
    public Map<String, Object> getChunks(Long id, int page, int size) {

        //1.查询文档记录
        EmbeddingRecordEntity docRecord = embeddingRecordRepository.findById(id);
        if (null == docRecord) {
            log.error("[AI] 查询文档片段 文档不存在:[{}]", id);
            return Map.of("total", 0, "chunks", List.of());
        }

        //2.获取文件名MD5
        String fileNameMd5 = docRecord.getFileNameMd5();

        //3.获取集合ID
        String collectionId = getCollectionId();
        if (StrUtil.isBlank(collectionId)) {
            return Map.of("total", 0, "chunks", List.of());
        }

        try {

            //4.先查询该文档的总片段数
            int total = getChunkCount(collectionId, fileNameMd5);

            //5.计算分页偏移量
            int offset = (page - 1) * size;

            //6.构建Chroma查询请求体
            JSONObject requestBody = new JSONObject();
            JSONObject where = new JSONObject();
            where.put("file_name_md5", fileNameMd5);
            requestBody.put("where", where);
            requestBody.put("include", List.of("documents", "metadatas"));
            requestBody.put("limit", size);
            requestBody.put("offset", offset);

            //7.定义查询URL
            String getUrl = chromaProperties.getUrl()
                    + "/api/v2/tenants/"
                    + chromaProperties.getTenant()
                    + "/databases/"
                    + chromaProperties.getDatabase()
                    + "/collections/"
                    + collectionId
                    + "/get";

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
            log.error("[AI] 查询文档片段失败 文档ID:[{}]", id, e);
            return Map.of("total", 0, "chunks", List.of());
        }
    }

    /**
     * 获取Chroma集合ID
     *
     * @return 集合ID，未找到返回null
     */
    private String getCollectionId() {

        //1.定义查询集合列表URL
        String collectionsUrl = chromaProperties.getUrl()
                + "/api/v2/tenants/"
                + chromaProperties.getTenant()
                + "/databases/"
                + chromaProperties.getDatabase()
                + "/collections";

        try {

            //2.查询集合列表
            String collectionsResp = HttpUtil.get(collectionsUrl);
            JSONArray collections = JSON.parseArray(collectionsResp);

            //3.查找目标集合ID
            for (int i = 0; i < collections.size(); i++) {
                JSONObject coll = collections.getJSONObject(i);
                if (chromaProperties.getCollection().equals(coll.getString("name"))) {
                    return coll.getString("id");
                }
            }

            //4.未找到集合
            log.warn("[AI] 未找到Chroma集合:[{}]", chromaProperties.getCollection());
            return null;
        } catch (Exception e) {
            log.error("[AI] 获取Chroma集合ID失败", e);
            return null;
        }
    }

    /**
     * 获取指定文档的片段总数
     *
     * @param collectionId 集合ID
     * @param fileNameMd5  文件名MD5
     * @return 片段总数
     */
    private int getChunkCount(String collectionId, String fileNameMd5) {

        //1.构建计数请求体（不限制数量，只取ID用于计数）
        JSONObject requestBody = new JSONObject();
        JSONObject where = new JSONObject();
        where.put("file_name_md5", fileNameMd5);
        requestBody.put("where", where);
        requestBody.put("include", List.of());

        //2.定义查询URL
        String getUrl = chromaProperties.getUrl()
                + "/api/v2/tenants/"
                + chromaProperties.getTenant()
                + "/databases/"
                + chromaProperties.getDatabase()
                + "/collections/"
                + collectionId
                + "/get";

        //3.发起请求
        String resp = HttpUtil.post(getUrl, requestBody.toJSONString());
        JSONObject result = JSON.parseObject(resp);

        //4.解析ID数组长度作为总数
        JSONArray ids = result.getJSONArray("ids");
        return ids != null ? ids.size() : 0;
    }

    /**
     * 获取已存在文档记录Map
     *
     * @param docsMap 文档名MD5-文档Map
     * @return 文档名MD5-文档记录Map
     */
    private Map<String, EmbeddingRecordEntity> getExistDocRecordsMap(Map<String, Document> docsMap) {

        //1.获取上传文档名MD5 Set
        Set<String> docNameMD5Set = docsMap.keySet();

        //2.根据上传文档名MD5 Set，查询已写入的文档记录
        Set<EmbeddingRecordEntity> existDocsRecord = embeddingRecordRepository.findAllByMd5(docNameMD5Set);

        //3.解析为已写入文档名MD5-文档记录 Map，返回
        return existDocsRecord.stream()
                .collect(Collectors.toMap(
                        EmbeddingRecordEntity::getFileNameMd5,
                        doc -> doc
                ));
    }

    /**
     * 按是否已存在分组文档
     *
     * @param existDocRecordsMap 已存在文档记录Map
     * @param docsMap            文档Map
     * @return 是否已存在-文档名称MD5-文档Map
     */
    private static Map<Boolean, Map<String, Document>> partitionedDocsMap(Map<String, EmbeddingRecordEntity> existDocRecordsMap, Map<String, Document> docsMap) {

        //1.获取已存在文档记录KeySet
        Set<String> existDocsNameMD5Set = existDocRecordsMap.keySet();

        //2.按是否已存在分组文档，返回
        return docsMap.entrySet().stream()
                .collect(Collectors.partitioningBy(
                        entry -> existDocsNameMD5Set.contains(entry.getKey()),
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                ));
    }

    /**
     * 将新增文档写入向量数据库，并保存文档记录
     *
     * @param newDocsMap 新文档
     * @return 新增文件列表
     */
    private List<String> addNewDocs(Map<String, Document> newDocsMap) {

        //1.为空直接返回
        if (CollUtil.isEmpty(newDocsMap)) {
            return List.of();
        }

        //2.转换为EmbeddingRecordEntity
        List<EmbeddingRecordEntity> newRecords = newDocsMap.entrySet().stream()
                .map(DocUtil::doc2Record)
                .toList();

        //3.解析出新增文件列表
        List<String> newFiles = newRecords.stream()
                .map(EmbeddingRecordEntity::getFullPath)
                .toList();

        //4.写入向量数据库前，先按file_name_md5清理可能残留的旧向量（保证ingest幂等，防止重试产生重复向量），再写入新向量
        try {
            Filter filter = metadataKey(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()).isIn(newDocsMap.keySet());
            embeddingStore.removeAll(filter);
            ingestor.ingest(newDocsMap.values().stream().toList());
        } catch (Exception e) {
            log.error("[AI] 新增文档 向量写入失败，本次不写入文档记录，可重新上传重试。文件:[{}]", newFiles, e);
            throw e;
        }

        //5.向量写入成功后，最后写入文档记录（SQLite作为唯一事实源，失败可靠下次上传自愈）
        embeddingRecordRepository.saveBatch(newRecords);

        //6.解析出新增文件列表，返回
        return newFiles;
    }

    /**
     * 更新内容发生变更的文档
     *
     * @param updateDocsNameSet  文档内容发生更新的文档名称MD5 Set
     * @param existDocsMap       已存在文档Map
     * @param existDocRecordsMap 已存在文档记录Map
     * @return 更新文件列表
     */
    private List<String> updateChangeDocs(Set<String> updateDocsNameSet, Map<String, Document> existDocsMap, Map<String, EmbeddingRecordEntity> existDocRecordsMap) {

        //1.为空直接返回
        if (CollUtil.isEmpty(updateDocsNameSet)) {
            return List.of();
        }

        //2.过滤出文档内容发生更新的文档记录
        List<EmbeddingRecordEntity> updateDocRecords = existDocRecordsMap.keySet().stream()
                .filter(updateDocsNameSet::contains)
                .map(existDocRecordsMap::get)
                .toList();

        //3.过滤出文档内容发生更新的文档
        List<Document> updateDocs = existDocsMap.keySet().stream()
                .filter(updateDocsNameSet::contains)
                .map(existDocsMap::get)
                .toList();

        //4.解析出更新文件列表，返回
        List<String> updateFiles = updateDocRecords.stream()
                .map(EmbeddingRecordEntity::getFullPath)
                .toList();

        //5.先按file_name_md5批量删除旧向量，再写入更新后的向量
        try {
            Filter filter = metadataKey(DocMetaDataKeyEnum.FILE_NAME_MD5.getKey()).isIn(updateDocsNameSet);
            embeddingStore.removeAll(filter);
            ingestor.ingest(updateDocs);
        } catch (Exception e) {
            log.error("[AI] 新增文档 向量更新失败，本次不更新文档记录，可重新上传重试。文件:[{}]", updateFiles, e);
            throw e;
        }

        //6.向量更新成功后，最后更新文档记录（SQLite作为唯一事实源，失败可靠下次上传自愈）
        embeddingRecordRepository.updateBatch(updateDocRecords);

        //7.解析出更新文件列表，返回
        return updateFiles;
    }
}
