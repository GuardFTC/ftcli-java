package com.ftc.ftcli.entity.skill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-15 15:00:00
 * @describe Skill技能实体
 */
@Data
@Builder
public class SkillEntity {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "技能名称")
    private String skillName;

    @Schema(description = "技能描述")
    private String skillDescription;

    @Schema(description = "SKILL.md文件内容")
    private String skillMdContent;

    @Schema(description = "SKILL.md文件路径(resources相对路径)")
    private String skillMdPath;

    @Schema(description = "创建时间")
    private String createdAt;

    @Schema(description = "更新时间")
    private String updatedAt;
}
