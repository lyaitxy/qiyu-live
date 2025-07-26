package org.qiyu.live.framework.datasource.starter.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class ShardingJdbcDatasourceAutoInitConnectionConfig {

    @Bean
    public ApplicationRunner runner(DataSource dataSource) {
        return args -> {
            // 手动触发下连接池的连接创建
            Connection connection = dataSource.getConnection();
        };
    }
}
