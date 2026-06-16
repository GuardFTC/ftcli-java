package com.ftc.ftcli.infra.sqlite;

import cn.hutool.core.collection.CollUtil;
import com.ftc.ftcli.entity.skill.SkillEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-15 15:00:00
 * @describe Skill技能数据访问层
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SkillRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 查询全部Skill
     *
     * @return 全部Skill列表
     */
    public List<SkillEntity> findAll() {

        //1.定义SQL
        String sql = "SELECT id, skill_name, skill_description, skill_md_content, skill_md_path, created_at, updated_at FROM skill ORDER BY updated_at DESC, id ASC";

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
     * 根据ID查询Skill
     *
     * @param id 记录ID
     * @return Skill实体，不存在返回null
     */
    public SkillEntity findById(Long id) {

        //1.定义SQL
        String sql = "SELECT id, skill_name, skill_description, skill_md_content, skill_md_path, created_at, updated_at FROM skill WHERE id = ?";

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
     * 根据技能名称查询Skill
     *
     * @param skillName 技能名称
     * @return Skill实体，不存在返回null
     */
    public SkillEntity findByName(String skillName) {

        //1.定义SQL
        String sql = "SELECT id, skill_name, skill_description, skill_md_content, skill_md_path, created_at, updated_at FROM skill WHERE skill_name = ?";

        //2.查询
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, skillName);

        //3.判空
        if (CollUtil.isEmpty(rows)) {
            return null;
        }

        //4.映射返回
        return mapRowToEntity(rows.get(0));
    }

    /**
     * 新增Skill
     *
     * @param entity 待保存的Skill实体
     * @return 是否保存成功
     */
    public boolean save(SkillEntity entity) {

        //1.定义SQL
        String sql = "INSERT INTO skill (skill_name, skill_description, skill_md_content, skill_md_path) VALUES (?, ?, ?, ?)";

        //2.执行
        int affected = jdbcTemplate.update(sql,
                entity.getSkillName(),
                entity.getSkillDescription(),
                entity.getSkillMdContent(),
                entity.getSkillMdPath()
        );

        //3.返回
        return affected > 0;
    }

    /**
     * 更新Skill
     *
     * @param entity 待更新的Skill实体（需包含id）
     * @return 是否更新成功
     */
    public boolean update(SkillEntity entity) {

        //1.定义SQL
        String sql = "UPDATE skill SET skill_name = ?, skill_description = ?, skill_md_content = ?, skill_md_path = ?, updated_at = datetime('now', 'localtime') WHERE id = ?";

        //2.执行
        int affected = jdbcTemplate.update(sql,
                entity.getSkillName(),
                entity.getSkillDescription(),
                entity.getSkillMdContent(),
                entity.getSkillMdPath(),
                entity.getId()
        );

        //3.返回
        return affected > 0;
    }

    /**
     * 根据ID删除Skill
     *
     * @param id 记录ID
     * @return 是否删除成功
     */
    public boolean deleteById(Long id) {

        //1.定义SQL
        String sql = "DELETE FROM skill WHERE id = ?";

        //2.执行
        int affected = jdbcTemplate.update(sql, id);

        //3.返回
        return affected > 0;
    }

    /**
     * 根据技能名称判断是否存在
     *
     * @param skillName 技能名称
     * @return 是否存在
     */
    public boolean existsByName(String skillName) {

        //1.定义SQL
        String sql = "SELECT COUNT(1) FROM skill WHERE skill_name = ?";

        //2.查询
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, skillName);

        //3.返回
        return count != null && count > 0;
    }

    /**
     * 行数据映射为实体
     *
     * @param row 行数据
     * @return 实体
     */
    private SkillEntity mapRowToEntity(Map<String, Object> row) {
        return SkillEntity.builder()
                .id(((Number) row.get("id")).longValue())
                .skillName((String) row.get("skill_name"))
                .skillDescription((String) row.get("skill_description"))
                .skillMdContent((String) row.get("skill_md_content"))
                .skillMdPath((String) row.get("skill_md_path"))
                .createdAt((String) row.get("created_at"))
                .updatedAt((String) row.get("updated_at"))
                .build();
    }
}
