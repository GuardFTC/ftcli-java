package com.ftc.ftcli.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.ftc.ftcli.entity.embedding.EmbeddingFileUploadPayload;
import com.ftc.ftcli.entity.embedding.EmbeddingFileUploadResult;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.infra.sqlite.EmbeddingRecordRepository;
import com.ftc.ftcli.service.AIEmbeddingService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocumentsRecursively;
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

    @Override
    @Transactional
    public EmbeddingFileUploadResult upload(EmbeddingFileUploadPayload payload) {

        //1.获取文档路径
        String path = payload.getPath();

        //2.加载上传文档
        Map<String, Document> docsMap = loadDocs(path);
        if (CollUtil.isEmpty(docsMap)) {
            log.error("[AI] 新增文档 文档不存在:[{}]", path);
            return new EmbeddingFileUploadResult();
        } else {
            log.info("[AI] 新增文档 加载文档数量:[{}]", docsMap.size());
        }

        //3.获取已存在文档记录Map
        Map<String, EmbeddingRecordEntity> existDocRecordsMap = getExistDocRecordsMap(docsMap);

        //4.按是否已存在分组文档
        Map<Boolean, Map<String, Document>> partitionedDocsMap = partitionedDocsMap(existDocRecordsMap, docsMap);

        //5.获取新增文档
        Map<String, Document> newDocsMap = partitionedDocsMap.getOrDefault(false, Map.of());
        log.info("[AI] 新增文档 新增文档数量:[{}]", newDocsMap.size());

        //6.新增文档，写入文档记录
        List<String> newFiles = addNewDocs(newDocsMap);

        //7.获取已存在文档
        Map<String, Document> existDocsMap = partitionedDocsMap.getOrDefault(true, Map.of());
        log.info("[AI] 新增文档 已存在文档数量:[{}]", existDocsMap.size());

        //8.过滤出文档内容发生更新的文档名称MD5
        Set<String> updateDocsNameSet = existDocsMap.entrySet()
                .stream()
                .filter(entry -> EmbeddingRecordEntity.isDocContentChange(entry, existDocRecordsMap))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        log.info("[AI] 新增文档 文档内容更新数量:[{}]", updateDocsNameSet.size());

        //9.已存在文档，如果文档内容发生更新，写入文档记录
        List<String> updateFiles = updateChangeDocs(updateDocsNameSet, existDocsMap, existDocRecordsMap);

        //10.构建结果返回
        return new EmbeddingFileUploadResult(newFiles, updateFiles);
    }

    /**
     * 加载文档
     *
     * @param filePath 文档路径
     * @return 文档名MD5-文档Map
     */
    private static Map<String, Document> loadDocs(String filePath) {

        //1.获取文档路径
        Path path = Paths.get(filePath);
        File uploadFile = path.toFile();

        //2.判定路径文档是否存在
        if (!uploadFile.exists()) {
            return new HashMap<>();
        }

        //3.判定文档是目录还是文档
        //如果是目录,递归获取目录下全部文档
        //如果是文档,则直接获取文档
        List<Document> documents;
        if (uploadFile.isDirectory()) {
            documents = loadDocumentsRecursively(path, new TextDocumentParser());
        } else {
            documents = List.of(FileSystemDocumentLoader.loadDocument(path, new TextDocumentParser()));
        }

        //4.解析为文档名MD5-文档Map，返回
        return documents.stream().collect(Collectors.toMap(
                EmbeddingRecordEntity::getFileNameMD5AndSetDocMetaData,
                doc -> doc,
                (existing, replacement) -> replacement
        ));
    }

    /**
     * 获取已存在文档记录Map
     *
     * @param docsMap 文档名MD5-文档Map
     * @return 文档名MD5-文档记录Map
     */
    private Map<String, EmbeddingRecordEntity> getExistDocRecordsMap(Map<String, Document> docsMap) {

        //1.获取上传文档名MD5 Set
        Set<String> docNameMD5Map = docsMap.keySet();

        //2.根据上传文档名MD5 Set，查询已写入的文档记录
        Set<EmbeddingRecordEntity> existDocsRecord = embeddingRecordRepository.findAllByMd5(docNameMD5Map);

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
            return new ArrayList<>();
        }

        //2.转换为EmbeddingRecordEntity
        List<EmbeddingRecordEntity> newRecords = newDocsMap.entrySet().stream()
                .map(EmbeddingRecordEntity::doc2Record)
                .toList();

        //3.写入文档记录
        embeddingRecordRepository.saveBatch(newRecords);

        //4.将新文档写入向量数据库
        ingestor.ingest(newDocsMap.values().stream().toList());

        //5.解析出新增文件列表，返回
        return newRecords.stream()
                .map(EmbeddingRecordEntity::getFullPath)
                .toList();
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
            return new ArrayList<>();
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

        //4.更新文档记录
        embeddingRecordRepository.updateBatch(updateDocRecords);

        //5.根据元数据file_name_md5 删除向量数据库中的元数据
        for (String fileNameMd5 : updateDocsNameSet) {
            Filter filter = metadataKey("file_name_md5").isEqualTo(fileNameMd5);
            embeddingStore.removeAll(filter);
        }

        //6.将更新后的文档写入向量数据库
        ingestor.ingest(updateDocs);

        //7.解析出更新文件列表，返回
        return updateDocRecords.stream()
                .map(EmbeddingRecordEntity::getFullPath)
                .toList();
    }
}
