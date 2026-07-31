package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Application entry point.
 *
 * - @EnableJpaAuditing turns on @CreatedDate / @LastModifiedDate support
 *   used across all entities (see BaseAuditEntity).
 * - @EnableCaching turns on @Cacheable support used by ProductService
 *   to cache the product catalog.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
