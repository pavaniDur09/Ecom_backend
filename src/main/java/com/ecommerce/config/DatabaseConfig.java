package com.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Explicitly declares the repository base package and enables annotation-
 * driven transaction management (@Transactional in the service layer).
 * Spring Boot auto-configures the DataSource/EntityManagerFactory from
 * application.yml, so this class only needs to switch on the extra features.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.ecommerce.repository")
@EnableTransactionManagement
public class DatabaseConfig {
}
