package com.ftc.ftcli.controller;

import com.ftc.ftcli.entity.result.RestfulResult;
import com.ftc.ftcli.entity.skill.SkillEntity;
import com.ftc.ftcli.service.AISkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-15 15:00:00
 * @describe AI Skill技能控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "AI技能", description = "AI Skill技能控制器")
@RequestMapping("/api/rest/v1/ai/skills")
public class AISkillController {

    private final AISkillService aiSkillService;

    @GetMapping
    @Operation(summary = "查询全部技能")
    public RestfulResult<List<SkillEntity>> getSkills() {

        //1.查询全部技能
        List<SkillEntity> skills = aiSkillService.getSkills();
        log.info("[AI] 查询全部技能 出参:[{}条]", skills.size());

        //2.返回
        return RestfulResult.Success.getOrUpdateData(skills);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询技能")
    public RestfulResult<SkillEntity> getSkillById(@PathVariable Long id) {
        log.info("[AI] 根据ID查询技能 入参:[{}]", id);

        //1.查询技能
        SkillEntity skill = aiSkillService.getSkillById(id);
        log.info("[AI] 查询技能 出参:[{}]", skill);

        //2.返回
        return RestfulResult.Success.getOrUpdateData(skill);
    }

    @PostMapping
    @Operation(summary = "新增技能")
    public RestfulResult<Boolean> addSkill(@RequestBody SkillEntity payload) {

        //1.打印日志
        log.info("[AI] 新增技能 入参:[{}]", payload);

        //2.新增技能
        boolean success = aiSkillService.addSkill(payload);
        log.info("[AI] 新增技能 出参:[{}]", success);

        //3.返回
        return RestfulResult.Success.addData(success);
    }

    @PutMapping
    @Operation(summary = "更新技能")
    public RestfulResult<Boolean> updateSkill(@RequestParam String oldName, @RequestBody SkillEntity payload) {

        //1.打印日志
        log.info("[AI] 更新技能 入参:[{}] [{}]", oldName, payload);

        //2.更新技能
        boolean success = aiSkillService.updateSkill(oldName, payload);
        log.info("[AI] 更新技能 出参:[{}]", success);

        //3.返回
        return RestfulResult.Success.getOrUpdateData(success);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除技能")
    public RestfulResult<Void> removeSkill(@PathVariable Long id) {

        //1.打印日志
        log.info("[AI] 删除技能 入参:[{}]", id);

        //2.删除技能
        boolean success = aiSkillService.removeSkill(id);
        log.info("[AI] 删除技能 出参:[{}]", success);

        //3.返回
        return RestfulResult.Success.removeData();
    }
}
