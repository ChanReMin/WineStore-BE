package com.example.demo.configs;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    /**
     * Write DataSource - Only for Command (Write operations)
     */
    @Primary
    @Bean(name = "writeDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.write")
    public DataSource writeDataSource() {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();

        String writeHost = System.getenv().getOrDefault("DB_WRITE_HOST", "localhost");
        String writePort = System.getenv().getOrDefault("DB_WRITE_PORT", "5432");
        String writeDatabase = System.getenv().getOrDefault("DB_DATABASE", "winestore");
        String writeJdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", writeHost, writePort, writeDatabase);

        dataSource.setJdbcUrl(writeJdbcUrl);
        dataSource.setUsername(System.getenv().getOrDefault("SPRING_DATASOURCE_WRITE_USERNAME", "user"));
        dataSource.setPassword(System.getenv().getOrDefault("SPRING_DATASOURCE_WRITE_PASSWORD", "password"));
        dataSource.setDriverClassName("org.postgresql.Driver");

        // Connection pool settings
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(5);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setPoolName("WriteDB-Pool");

        return dataSource;
    }

    /**
     * Read DataSource - Only for Query (Read operations)
     */
    @Bean(name = "readDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.read")
    public DataSource readDataSource() {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();

        String readHost = System.getenv().getOrDefault("DB_READ_HOST", "localhost");
        String readPort = System.getenv().getOrDefault("DB_READ_PORT", "5433");
        String readDatabase = System.getenv().getOrDefault("DB_DATABASE", "winestore");
        String readJdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?readOnly=true", readHost, readPort, readDatabase);

        dataSource.setJdbcUrl(readJdbcUrl);
        dataSource.setUsername(System.getenv().getOrDefault("SPRING_DATASOURCE_READ_USERNAME", "user"));
        dataSource.setPassword(System.getenv().getOrDefault("SPRING_DATASOURCE_READ_PASSWORD", "password"));
        dataSource.setDriverClassName("org.postgresql.Driver");

        // Enforce read-only
        dataSource.setReadOnly(true);
        dataSource.addDataSourceProperty("readOnly", "true");

        // Connection pool settings (can increase more than write because read is more)
        dataSource.setMaximumPoolSize(20);
        dataSource.setMinimumIdle(10);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setPoolName("ReadDB-Pool");

        return dataSource;
    }
}