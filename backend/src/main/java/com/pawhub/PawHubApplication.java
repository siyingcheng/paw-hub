package com.pawhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Paw-Hub Backend Application Entry Point
 *
 * This is the main Spring Boot application class that initializes the Paw-Hub
 * test result analytics platform. It enables component scanning across the
 * com.pawhub package and all subpackages.
 *
 * Features enabled:
 * - Spring Data JPA repositories for database access
 * - Caching layer for improved performance
 * - Asynchronous processing for long-running tasks
 *
 * @author Paw-Hub Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.pawhub.repository")
@ComponentScan(basePackages = "com.pawhub")
@EnableCaching
@EnableAsync
public class PawHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(PawHubApplication.class, args);
    }
}
