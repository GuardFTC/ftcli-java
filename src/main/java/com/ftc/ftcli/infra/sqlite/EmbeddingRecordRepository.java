package com.ftc.ftcli.infra.sqlite;

import cn.hutool.core.collection.CollUtil;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 20:00:00
 * @describe Embedding文档记录数据访问层
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class EmbeddingRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 查询全部记录
     *
     * @return 全部embedding记录
     */
    public List<EmbeddingRecordEntity> findAll() {

        //1.定义SQL
        String sql = "SELECT id, file_name, file_path, file_name_md5, file_content_md5, created_at, updated_at FROM embedding_record order by updated_at desc,id asc";

        //2.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        //3.判空
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptyList();
        }

        //4.映射返回
        return rows.stream().map(this::mapRowToEntity).toList();
    }

    /**
     * 根据文件名MD5集合查询已存在的记录
     *
     * @param fileNameMd5List 文件名MD5集合
     * @return 已存在的记录列表
     */
    public Set<EmbeddingRecordEntity> findAllByMd5(Set<String> fileNameMd5List) {

        //1.判空
        if (CollUtil.isEmpty(fileNameMd5List)) {
            return Collections.emptySet();
        }

        //2.构建IN查询占位符
        String placeholders = fileNameMd5List.stream().map(s -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, file_name, file_path, file_name_md5, file_content_md5, created_at, updated_at FROM embedding_record WHERE file_name_md5 IN (" + placeholders + ")";

        //3.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, fileNameMd5List.toArray());

        //4.判空
        if (CollUtil.isEmpty(rows)) {
            return Collections.emptySet();
        }

        //5.映射返回
        return rows.stream().map(this::mapRowToEntity).collect(Collectors.toSet());
    }

    /**
     * 根据ID查询记录
     *
     * @param id 记录ID
     * @return embedding记录，不存在返回null
     */
    public EmbeddingRecordEntity findById(Long id) {

        //1.定义SQL
        String sql = "SELECT id, file_name, file_path, file_name_md5, file_content_md5, created_at, updated_at FROM embedding_record WHERE id = ?";

        //2.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);

        //3.判空
        if (CollUtil.isEmpty(rows)) {
            return null;
        }

        //4.映射返回
        return mapRowToEntity(rows.get(0));
    }

    /**
     * 批量保存新增的文档记录
     *
     * @param entities 待保存的记录集合
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveBatch(List<EmbeddingRecordEntity> entities) {

        //1.判空
        if (CollUtil.isEmpty(entities)) {
            return;
        }

        //2.构建多行VALUES的批量INSERT SQL
        StringBuilder sql = new StringBuilder("INSERT INTO embedding_record (file_name, file_path, file_name_md5, file_content_md5) VALUES ");
        List<Object> params = new ArrayList<>();

        //3.遍历拼接
        for (int i = 0; i < entities.size(); i++) {

            //4.获取待保存的记录
            EmbeddingRecordEntity entity = entities.get(i);

            //5.如果不是第一行,拼接,
            if (i > 0) {
                sql.append(",");
            }

            //6.拼接参数
            sql.append("(?, ?, ?, ?)");
            params.add(entity.getFileName());
            params.add(entity.getFilePath());
            params.add(entity.getFileNameMd5());
            params.add(entity.getFileContentMd5());
        }

        //7.执行
        jdbcTemplate.update(sql.toString(), params.toArray());
    }

    /**
     * 批量更新文档内容变更记录的file_content_md5和updated_at
     *
     * @param entities 待更新的记录集合（需包含fileNameMd5和新的fileContentMd5）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateBatch(List<EmbeddingRecordEntity> entities) {

        //1.判空
        if (CollUtil.isEmpty(entities)) {
            return;
        }

        //2.定义SQL
        String sql = "UPDATE embedding_record SET file_content_md5 = ?, updated_at = datetime('now', 'localtime') WHERE file_name_md5 = ?";

        //3.构建批量参数
        List<Object[]> batchArgs = entities.stream()
                .map(entity -> new Object[]{entity.getFileContentMd5(), entity.getFileNameMd5()})
                .toList();

        //4.批量执行
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * 根据ID删除记录
     *
     * @param id 记录ID
     * @return 是否删除成功
     */
    public boolean deleteById(Long id) {

        //1.定义SQL
        String sql = "DELETE FROM embedding_record WHERE id = ?";

        //2.执行
        int affected = jdbcTemplate.update(sql, id);

        //3.返回
        return affected > 0;
    }

    /**
     * 行数据映射为实体
     *
     * @param row 行数据
     * @return 实体
     */
    private EmbeddingRecordEntity mapRowToEntity(Map<String, Object> row) {
        return EmbeddingRecordEntity.builder()
                .id(((Number) row.get("id")).longValue())
                .fileName((String) row.get("file_name"))
                .filePath((String) row.get("file_path"))
                .fileNameMd5((String) row.get("file_name_md5"))
                .fileContentMd5((String) row.get("file_content_md5"))
                .createdAt((String) row.get("created_at"))
                .updatedAt((String) row.get("updated_at"))
                .build();
    }
}