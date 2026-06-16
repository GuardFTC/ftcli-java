package com.ftc.ftcli.controller;

import com.ftc.ftcli.entity.embedding.EmbeddingFileUploadPayload;
import com.ftc.ftcli.entity.embedding.EmbeddingFileUploadResult;
import com.ftc.ftcli.entity.embedding.EmbeddingRecordEntity;
import com.ftc.ftcli.entity.result.RestfulResult;
import com.ftc.ftcli.service.AIEmbeddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 11:19:28
 * @describe AI向量嵌入控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "AI向量嵌入", description = "AI向量嵌入控制器")
@RequestMapping("/api/rest/v1/ai/embedding")
public class AIEmbeddingController {

    private final AIEmbeddingService aiEmbeddingService;

    @GetMapping("docs")
    @Operation(summary = "查询文档")
    public RestfulResult<List<EmbeddingRecordEntity>> getDocs() {

        //1.查询文档
        List<EmbeddingRecordEntity> docs = aiEmbeddingService.getDocs();
        log.info("[AI] 查询文档 出参:[{}]", docs);

        //3.返回
        return RestfulResult.Success.getOrUpdateData(docs);
    }

    @PostMapping("docs")
    @Operation(summary = "新增文档")
    public RestfulResult<EmbeddingFileUploadResult> upload(@RequestBody EmbeddingFileUploadPayload payload) {

        //1.打印日志
        log.info("[AI] 新增文档 入参:[{}]", payload);

        //2.新增文档
        EmbeddingFileUploadResult fileUploadResult = aiEmbeddingService.upload(payload);
        log.info("[AI] 新增文档 出参:[{}]", fileUploadResult);

        //3.返回
        return RestfulResult.Success.addData(fileUploadResult);
    }

    @DeleteMapping("docs/{id}")
    @Operation(summary = "删除文档")
    public RestfulResult<Void> upload(@PathVariable Long id) {

        //1.打印日志
        log.info("[AI] 删除文档 入参:[{}]", id);

        //2.删除文档
        aiEmbeddingService.remove(id);

        //3.返回
        return RestfulResult.Success.removeData();
    }

    @GetMapping("vectors/count")
    @Operation(summary = "查询向量记录数")
    public RestfulResult<Integer> getVectorCount() {

        //1.查询向量记录数
        int count = aiEmbeddingService.getVectorCount();
        log.info("[AI] 查询向量记录数 出参:[{}]", count);

        //2.返回
        return RestfulResult.Success.getOrUpdateData(count);
    }
}
