package com.example.demo.configs;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean(name = "writeDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeDataSource() {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();

        // Manually set connection properties from environment
        String writeHost = System.getenv().getOrDefault("DB_WRITE_HOST", "localhost");
        String writePort = System.getenv().getOrDefault("DB_WRITE_PORT", "5432");
        String writeDatabase = System.getenv().getOrDefault("DB_DATABASE", "winestore");
        String writeJdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", writeHost, writePort, writeDatabase);
        dataSource.setJdbcUrl(writeJdbcUrl);

        dataSource.setUsername(System.getenv().getOrDefault("SPRING_DATASOURCE_WRITE_USERNAME", "user"));
        dataSource.setPassword(System.getenv().getOrDefault("SPRING_DATASOURCE_WRITE_PASSWORD", "password"));
        dataSource.setDriverClassName("org.postgresql.Driver");

        return dataSource;
    }

    @Bean(name = "readDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource readDataSource() {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();

        // Manually set connection properties from environment
        String readHost = System.getenv().getOrDefault("DB_READ_HOST", "localhost");
        String readPort = System.getenv().getOrDefault("DB_READ_PORT", "5432");
        String readDatabase = System.getenv().getOrDefault("DB_DATABASE", "winestore");
        String readJdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", readHost, readPort, readDatabase);
        dataSource.setJdbcUrl(readJdbcUrl);

        dataSource.setUsername(System.getenv().getOrDefault("SPRING_DATASOURCE_READ_USERNAME", "user"));
        dataSource.setPassword(System.getenv().getOrDefault("SPRING_DATASOURCE_READ_PASSWORD", "password"));
        dataSource.setDriverClassName("org.postgresql.Driver");

        // Add connection-level read-only enforcement
        dataSource.addDataSourceProperty("readOnly", "true");
        dataSource.setReadOnly(true);

        // Add this to connection string
        String url = dataSource.getJdbcUrl();
        if (!url.contains("?")) {
            url += "?";
        } else {
            url += "&";
        }
        url += "readOnly=true";
        dataSource.setJdbcUrl(url);


        return dataSource;
    }

    @Bean
    public DataSource routingDataSource(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource) {

        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("write", writeDataSource);
        dataSourceMap.put("read", readDataSource);

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(writeDataSource);

        return routingDataSource;
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}