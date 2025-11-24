package com.example.demo.configs;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Value("${spring.flyway.locations}")
    private String[] locations;

    @Value("${spring.datasource.write.url}")
    private String dbUrl;

    @Value("${spring.datasource.write.username}")
    private String dbUser;

    @Value("${spring.datasource.write.password}")
    private String dbPassword;

    @Bean
    public Flyway flyway(DataSource writeDataSource) {
        return Flyway.configure()
                .dataSource(dbUrl, dbUser, dbPassword)
                .locations(locations)
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();
    }

    /**
     * Chỉ migrate SAU KHI Hibernate tạo xong bảng.
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> flywayMigrationTrigger(Flyway flyway) {
        return event -> {
            System.out.println("🔄 Running Flyway migrations AFTER Hibernate schema creation...");
            flyway.migrate();
        };
    }
}
