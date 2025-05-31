package com.achievesync.goalservice.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.achievesync.goalservice.projection")
@EntityScan(basePackages = "com.achievesync.goalservice.projection")
@EnableTransactionManagement
public class DatabaseConfig {
    // JPA configuration will be handled by Spring Boot auto-configuration
}