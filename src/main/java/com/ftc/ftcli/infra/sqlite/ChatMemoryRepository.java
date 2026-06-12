package com.ftc.ftcli.infra.sqlite;

import cn.hutool.core.collection.CollUtil;
import com.ftc.ftcli.entity.chat.ChatMemoryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-12 16:00:00
 * @describe 聊天记录数据访问层
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ChatMemoryRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 根据会话ID查询聊天记录
     *
     * @param memoryId 会话ID
     * @return 聊天记录实体，不存在返回null
     */
    public ChatMemoryEntity findByMemoryId(String memoryId) {

        //1.定义SQL
        String sql = "SELECT id, memory_id, chat_message FROM chat_memory WHERE memory_id = ?";

        //2.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, memoryId);

        //3.判空
        if (CollUtil.isEmpty(rows)) {
            return null;
        }

        //4.映射返回
        return mapRowToEntity(rows.get(0));
    }

    /**
     * 保存或更新聊天记录（基于memory_id做upsert）
     *
     * @param memoryId    会话ID
     * @param chatMessage 聊天消息JSON
     */
    public void saveOrUpdate(String memoryId, String chatMessage) {

        //1.定义SQL（SQLite的UPSERT语法）
        String sql = "INSERT INTO chat_memory (memory_id, chat_message) VALUES (?, ?) " +
                "ON CONFLICT(memory_id) DO UPDATE SET chat_message = excluded.chat_message";

        //2.执行
        jdbcTemplate.update(sql, memoryId, chatMessage);
    }

    /**
     * 根据会话ID删除聊天记录
     *
     * @param memoryId 会话ID
     */
    public void deleteByMemoryId(String memoryId) {

        //1.定义SQL
        String sql = "DELETE FROM chat_memory WHERE memory_id = ?";

        //2.执行
        jdbcTemplate.update(sql, memoryId);
    }

    /**
     * 行数据映射为实体
     *
     * @param row 行数据
     * @return 实体
     */
    private ChatMemoryEntity mapRowToEntity(Map<String, Object> row) {
        return ChatMemoryEntity.builder()
                .id(((Number) row.get("id")).longValue())
                .memoryId((String) row.get("memory_id"))
                .chatMessage((String) row.get("chat_message"))
                .build();
    }
}
