package com.ftc.ftcli.service;

import com.ftc.ftcli.entity.skill.SkillEntity;

import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-15 15:00:00
 * @describe AI Skill技能服务
 */
public interface AISkillService {

    /**
     * 获取全部Skill
     *
     * @return Skill列表
     */
    List<SkillEntity> getSkills();

    /**
     * 根据ID获取Skill
     *
     * @param id Skill ID
     * @return Skill实体
     */
    SkillEntity getSkillById(Long id);

    /**
     * 新增Skill
     *
     * @param payload Skill实体
     * @return 是否新增成功
     */
    boolean addSkill(SkillEntity payload);

    /**
     * 更新Skill
     *
     * @param oldName 旧名称
     * @param payload Skill实体（包含id）
     * @return 是否更新成功
     */
    boolean updateSkill(String oldName, SkillEntity payload);

    /**
     * 删除Skill
     *
     * @param id Skill ID
     * @return 是否删除成功
     */
    boolean removeSkill(Long id);
}
