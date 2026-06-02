package com.ftc.ftcli.service.impl;

import com.ftc.ftcli.ai.tool.spec.ToolSpecEntity;
import com.ftc.ftcli.infra.ToolSpecRepository;
import com.ftc.ftcli.service.AIToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:23:55
 * @describe AI工具Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIToolServiceImpl implements AIToolService {

    private final ToolSpecRepository toolSpecRepository;

    @Override
    public List<ToolSpecEntity> getTools() {
        return toolSpecRepository.findAll();
    }

    @Override
    public Long addTool(ToolSpecEntity entity) {

        //1.校验工具名称是否已存在
        if (toolSpecRepository.existsByName(entity.getName())) {
            throw new IllegalArgumentException("工具名称已存在: " + entity.getName());
        }

        //2.保存工具,返回
        return toolSpecRepository.save(entity);
    }

    @Override
    public boolean removeTool(String name) {
        return toolSpecRepository.deleteByName(name);
    }
}
