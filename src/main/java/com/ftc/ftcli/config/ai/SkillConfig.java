package com.ftc.ftcli.config.ai;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.ftc.ftcli.entity.skill.SkillEntity;
import com.ftc.ftcli.service.AISkillService;
import dev.langchain4j.skills.Skill;
import dev.langchain4j.skills.Skills;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-15 14:00:00
 * @describe AI Skills 配置类
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SkillConfig {

    private final AISkillService aiSkillService;

    @Bean
    public Skills skills() {

        //1.查询Skill列表
        List<SkillEntity> skillBeans = aiSkillService.getSkills();

        //2.定义最终加载Skill集合
        List<Skill> skills = new ArrayList<>();

        //3.遍历Skill列表
        for (SkillEntity skillBean : skillBeans) {

            //4.获取内容,如果内容为空,从文件中获取
            String content = StrUtil.isNotBlank(skillBean.getSkillMdContent()) ?
                    skillBean.getSkillMdContent() :
                    ResourceUtil.readUtf8Str(skillBean.getSkillMdPath());

            //5.如果内容为空，跳过
            if (StrUtil.isBlank(content)) {
                log.warn("[Skill] 加载失败,无法获取到内容: [{}]", skillBean.getSkillName());
                continue;
            }

            //6.创建 Skill
            Skill skill = Skill.builder()
                    .name(skillBean.getSkillName())
                    .description(skillBean.getSkillDescription())
                    .content(content)
                    .build();

            //7.存入集合
            skills.add(skill);
        }

        //8.打印日志
        log.info("[Skill] 加载完成,共[{}]个技能", skills.size());

        //9.创建 Skills
        return Skills.from(skills);
    }
}
