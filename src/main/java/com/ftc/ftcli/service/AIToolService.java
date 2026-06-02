package com.ftc.ftcli.service;

import com.ftc.ftcli.ai.tool.spec.ToolSpecEntity;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 19:32:11
 * @describe AI工具服务
 */
public interface AIToolService {

    /**
     * 获取工具列表
     *
     * @return 工具列表
     */
    List<ToolSpecEntity> getTools();

    /**
     * 新增工具
     *
     * @param entity 工具实体
     * @return 工具ID
     */
    Long addTool(ToolSpecEntity entity);

    /**
     * 删除工具
     *
     * @param name 工具名称
     * @return 是否删除成功
     */
    boolean removeTool(String name);
}
