package com.ftc.ftcli.controller;

import com.ftc.ftcli.ai.tool.spec.ToolSpecEntity;
import com.ftc.ftcli.entity.result.RestfulResult;
import com.ftc.ftcli.service.AIToolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:19:28
 * @describe AI工具控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "AI工具", description = "AI工具控制器")
@RequestMapping("/api/rest/v1/ai/tools")
public class AIToolController {

    private final AIToolService aiToolService;

    @GetMapping
    @Operation(summary = "查询全部工具")
    public RestfulResult<List<ToolSpecEntity>> getTools() {

        //1.获取工具集合
        List<ToolSpecEntity> tools = aiToolService.getTools();
        log.info("[AI] 查询全部工具 出参:[{}]", tools);

        //2.返回
        return RestfulResult.Success.getOrUpdateData(tools);
    }

    @PostMapping
    @Operation(summary = "新增工具")
    public RestfulResult<Long> addTool(@RequestBody ToolSpecEntity toolSpec) {

        //1.打印日志
        log.info("[AI] 新增工具 入参:[{}]", toolSpec);

        //2.新增工具
        Long toolId = aiToolService.addTool(toolSpec);
        log.info("[AI] 新增工具 出参:[{}]", toolId);

        //3.返回
        return RestfulResult.Success.addData(toolId);
    }

    @DeleteMapping("/{name}")
    @Operation(summary = "删除工具")
    public RestfulResult<Void> removeTool(@PathVariable String name) {

        //1.打印日志
        log.info("[AI] 删除工具 入参:[{}]", name);

        //2.删除工具
        boolean removeSuccess = aiToolService.removeTool(name);
        log.info("[AI] 删除工具 出参:[{}]", removeSuccess);

        //3.返回
        return RestfulResult.Success.removeData();
    }
}
