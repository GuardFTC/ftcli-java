package com.ftc.ftcli.ai.tool.spec;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 15:00:00
 * @describe 工具描述数据访问层
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ToolSpecRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 查询全部工具规格（含参数）
     *
     * @return 工具规格集合
     */
    public List<ToolSpecEntity> findAll() {

        //1.定义SQL
        String sql = "SELECT id, name, description, type FROM tool_spec";

        //2.查询全部工具
        List<Map<String, Object>> toolRows = jdbcTemplate.queryForList(sql);

        //3.判空
        if (CollUtil.isEmpty(toolRows)) {
            return Collections.emptyList();
        }

        //4.遍历构建ToolSpecEntity
        return toolRows.stream().map(row -> {

            //5.获取工具ID
            Long toolId = ((Number) row.get("id")).longValue();

            //6.查询工具参数
            List<ToolSpecParamEntity> params = findParamsByToolId(toolId);

            //7.构建ToolSpecEntity
            return ToolSpecEntity.builder()
                    .name((String) row.get("name"))
                    .description((String) row.get("description"))
                    .type((String) row.get("type"))
                    .params(params)
                    .build();
        }).toList();
    }

    /**
     * 根据工具名称判断工具是否存在
     *
     * @param name 工具名称
     * @return 是否存在
     */
    public boolean existsByName(String name) {

        //1.定义SQL
        String sql = "SELECT COUNT(*) FROM tool_spec WHERE name = ?";

        //2.执行SQL
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name);

        //3.判断是否存在
        return count != null && count > 0;
    }

    /**
     * 新增工具规格（含参数）
     *
     * @param entity 工具规格实体
     */
    @Transactional(rollbackFor = Exception.class)
    public Long save(ToolSpecEntity entity) {

        //1.定义插入工具表SQL
        String insertToolSql = "INSERT INTO tool_spec (name, description, type) VALUES (?, ?, ?)";

        //2.执行插入工具表SQL
        jdbcTemplate.update(insertToolSql, entity.getName(), entity.getDescription(), entity.getType());

        //3.查询刚插入的工具ID
        Long toolId = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);

        //4.如果参数不为空，则插入参数
        if (CollUtil.isNotEmpty(entity.getParams())) {

            //5.定义插入参数表SQL
            String insertParamSql = "INSERT INTO tool_spec_param (tool_spec_id, name, description, required, type, enum_values) VALUES (?, ?, ?, ?, ?, ?)";

            //6.遍历插入参数
            for (ToolSpecParamEntity param : entity.getParams()) {

                //7.如果存在枚举值，则转换为字符串
                String enumValues = CollUtil.isNotEmpty(param.getEnumValues())
                        ? String.join(",", param.getEnumValues())
                        : null;

                //8.执行插入参数表SQL
                jdbcTemplate.update(insertParamSql,
                        toolId,
                        param.getName(),
                        param.getDescription(),
                        param.isRequired() ? 1 : 0,
                        param.getType().name().toLowerCase(),
                        enumValues
                );
            }
        }

        //9.返回工具ID
        return toolId;
    }

    /**
     * 根据工具名称删除工具规格（含参数）
     *
     * @param name 工具名称
     * @return 是否删除成功（工具是否存在）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByName(String name) {

        //1.定义查询工具SQL
        String selectToolSql = "SELECT id FROM tool_spec WHERE name = ?";

        //2.查询工具ID
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectToolSql, name);

        //3.工具不存在
        if (CollUtil.isEmpty(rows)) {
            return false;
        }

        //4.获取工具ID
        Long toolId = ((Number) rows.get(0).get("id")).longValue();

        //5.先删除参数
        String deleteParamSql = "DELETE FROM tool_spec_param WHERE tool_spec_id = ?";
        jdbcTemplate.update(deleteParamSql, toolId);

        //6.再删除工具
        String deleteToolSql = "DELETE FROM tool_spec WHERE id = ?";
        jdbcTemplate.update(deleteToolSql, toolId);

        //7.返回
        return true;
    }

    /**
     * 根据工具ID查询工具参数
     *
     * @param toolId 工具ID
     * @return 工具参数集合
     */
    private List<ToolSpecParamEntity> findParamsByToolId(Long toolId) {

        //1.定义SQL
        String sql = "SELECT name, description, required, type, enum_values FROM tool_spec_param WHERE tool_spec_id = ?";

        //2.查询工具参数
        List<Map<String, Object>> paramRows = jdbcTemplate.queryForList(sql, toolId);

        //3.判空
        if (CollUtil.isEmpty(paramRows)) {
            return Collections.emptyList();
        }

        //4.遍历构建ToolSpecParamEntity
        return paramRows.stream().map(paramRow -> {

            //5.解析枚举值
            String enumValuesStr = (String) paramRow.get("enum_values");
            List<String> enumValues = StrUtil.isNotBlank(enumValuesStr)
                    ? Arrays.asList(enumValuesStr.split(","))
                    : null;

            //6.构建ToolSpecParamEntity
            return ToolSpecParamEntity.builder()
                    .name((String) paramRow.get("name"))
                    .description((String) paramRow.get("description"))
                    .required(((Number) paramRow.get("required")).intValue() == 1)
                    .type(ToolParamTypeEnum.valueOf(((String) paramRow.get("type")).toUpperCase()))
                    .enumValues(enumValues)
                    .build();
        }).toList();
    }
}
