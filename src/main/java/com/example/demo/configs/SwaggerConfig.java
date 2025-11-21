package com.example.demo.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI wineStoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🍷 Wine Store API - CQRS Pattern")
                        .description("""
                                **Wine Store REST API với CQRS Pattern**
                                
                                Hệ thống API được thiết kế với CQRS (Command Query Responsibility Segregation):
                                - **Write Operations** (Commands): Sử dụng Write Database (Primary PostgreSQL)
                                - **Read Operations** (Queries): Sử dụng Read Database (Replica PostgreSQL)
                                
                                ## Database Architecture
                                - Write DB: `postgres-write:5432` (Primary)
                                - Read DB: `postgres-read:5433` (Replica)
                                - Replication: PostgreSQL Logical Replication
                                
                                ## API Operations
                                ### Write Operations (🔴 Write DB)
                                - Create Product
                                - Update Product
                                - Delete Product (Soft Delete)
                                - Restore Product
                                
                                ### Read Operations (🔵 Read DB)
                                - Get Product by ID
                                - Get All Products (Paginated)
                                - Search Products (with filters)
                                - Get Products by Category/Brand/Wine Type
                                - Get Latest Products
                                
                                ## Technology Stack
                                - Spring Boot 3.2.1
                                - PostgreSQL 16
                                - JPA/Hibernate
                                - Docker & Docker Compose
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Vanthuyen")
                                .email("vanthuyen@winestore.com")
                                .url("https://github.com/vanthuyen"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Docker Container Server")
                ))
                .tags(List.of(
                        new Tag()
                                .name("Product Write Operations")
                                .description("🔴 APIs ghi dữ liệu (Create, Update, Delete) - Sử dụng **Write Database**"),
                        new Tag()
                                .name("Product Read Operations")
                                .description("🔵 APIs đọc dữ liệu (Get, Search, Filter) - Sử dụng **Read Database**")
                ));
    }
}