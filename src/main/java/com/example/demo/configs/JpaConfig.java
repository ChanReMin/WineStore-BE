package com.example.demo.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaAuditing
public class JpaConfig {

    /**
     * Write EntityManager - Cho Command repositories
     */
    @Primary
    @Bean(name = "writeEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean writeEntityManagerFactory(
            @Qualifier("writeDataSource") DataSource writeDataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(writeDataSource);
        em.setPackagesToScan("com.example.demo.entities");
        em.setPersistenceUnitName("writeUnit");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(true);
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "none");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        em.setJpaProperties(properties);
        return em;
    }

    /**
     * Write Transaction Manager
     */
    @Primary
    @Bean(name = "writeTransactionManager")
    public PlatformTransactionManager writeTransactionManager(
            @Qualifier("writeEntityManagerFactory") LocalContainerEntityManagerFactoryBean writeEntityManagerFactory) {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(writeEntityManagerFactory.getObject());
        return transactionManager;
    }

    /**
     * Read EntityManager - Cho Query repositories
     */
    @Bean(name = "readEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(
            @Qualifier("readDataSource") DataSource readDataSource) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(readDataSource);
        em.setPackagesToScan("com.example.demo.entities");
        em.setPersistenceUnitName("readUnit");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false); // Read DB không tự tạo table
        vendorAdapter.setShowSql(true);
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "none"); // Không modify schema
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        em.setJpaProperties(properties);
        return em;
    }

    /**
     * Read Transaction Manager
     */
    @Bean(name = "readTransactionManager")
    public PlatformTransactionManager readTransactionManager(
            @Qualifier("readEntityManagerFactory") LocalContainerEntityManagerFactoryBean readEntityManagerFactory) {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(readEntityManagerFactory.getObject());
        return transactionManager;
    }

    /**
     * Configuration cho Command Repositories
     */
    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.example.demo.repositories.commands",
            entityManagerFactoryRef = "writeEntityManagerFactory",
            transactionManagerRef = "writeTransactionManager"
    )
    static class CommandRepositoryConfig {
    }

    /**
     * Configuration cho Query Repositories
     */
    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.example.demo.repositories.queries",
            entityManagerFactoryRef = "readEntityManagerFactory",
            transactionManagerRef = "readTransactionManager"
    )
    static class QueryRepositoryConfig {
    }
}