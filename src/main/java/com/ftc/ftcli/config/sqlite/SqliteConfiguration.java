package com.ftc.ftcli.config.sqlite;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-02 15:00:00
 * @describe SQLite配置类 确保数据库目录存在
 */
@Slf4j
@Configuration
public class SqliteConfiguration {

    /**
     * 创建数据源 确保SQLite数据库目录存在
     *
     * @param properties 数据源配置属性
     * @return 数据源
     */
    @Bean
    public DataSource dataSource(DataSourceProperties properties) {

        //1.从jdbc url中解析文件路径
        String url = properties.getUrl();
        String filePath = url.replace("jdbc:sqlite:", "");

        //2.确保父目录存在
        File dbFile = new File(filePath);
        FileUtil.mkdir(dbFile.getParentFile());

        //3.创建数据源返回
        return properties.initializeDataSourceBuilder().build();
    }
}
