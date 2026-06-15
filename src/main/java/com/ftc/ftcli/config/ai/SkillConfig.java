package com.ftc.ftcli.config.ai;

import dev.langchain4j.skills.FileSystemSkill;
import dev.langchain4j.skills.FileSystemSkillLoader;
import dev.langchain4j.skills.Skills;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-15 14:00:00
 * @describe AI Skills 配置类
 */
@Slf4j
@Configuration
public class SkillConfig {

    @Bean
    public Skills skills() throws IOException {

        //1.扫描 classpath 下所有 skills/*/SKILL.md
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:skills/*/SKILL.md");

        //2.创建临时目录,将 Skill 文件从 classpath 提取到文件系统
        Path tempSkillsDir = Files.createTempDirectory("ftcli-skills");
        tempSkillsDir.toFile().deleteOnExit();

        //3.遍历每个 SKILL.md,复制到临时目录
        for (Resource resource : resources) {

            //4.解析 Skill 名称（父目录名）
            String path = resource.getURL().getPath();
            String[] parts = path.split("/");
            String skillName = parts[parts.length - 2];

            //5.创建 Skill 子目录
            Path skillDir = tempSkillsDir.resolve(skillName);
            Files.createDirectories(skillDir);

            //6.复制 SKILL.md 到临时目录
            Path targetFile = skillDir.resolve("SKILL.md");
            try (InputStream is = resource.getInputStream()) {
                Files.copy(is, targetFile);
            }
            targetFile.toFile().deleteOnExit();
            skillDir.toFile().deleteOnExit();
        }

        //7.从临时目录加载全部 Skill
        List<FileSystemSkill> skillList = FileSystemSkillLoader.loadSkills(tempSkillsDir);
        log.info("[Skills] 加载完成, 共[{}]个 Skill", skillList.size());

        //8.构建 Skills 实例
        return Skills.from(skillList);
    }
}
