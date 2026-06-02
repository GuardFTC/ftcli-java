package com.ftc.ftcli.config.sqlite;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 15:00:00
 * @describe SQLite初始化器 启动时自动建表和初始化数据
 */
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class SqliteInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //1.执行建表SQL
        executeSqlFile("sql/schema.sql");

        //2.执行数据初始化SQL
        executeSqlFile("sql/data.sql");

        //3.打印日志
        log.info("[SQLite] 数据库初始化完成");
    }

    /**
     * 执行SQL文件
     *
     * @param path 类路径下的SQL文件路径
     * @throws Exception 异常
     */
    private void executeSqlFile(String path) throws Exception {

        //1.读取SQL文件
        ClassPathResource resource = new ClassPathResource(path);
        String sql = resource.getContentAsString(StandardCharsets.UTF_8);

        //2.按分号拆分执行
        String[] statements = sql.split(";");
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                jdbcTemplate.execute(trimmed);
            }
        }
    }
}
