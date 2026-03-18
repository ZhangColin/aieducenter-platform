package com.aieducenter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 审计配置。
 *
 * <p>启用 JPA 审计功能，自动填充 {@code @CreatedDate}、{@code @LastModifiedDate}
 * 等审计字段。
 *
 * @since 0.1.0
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
    // JPA 审计由 cartisan-data-jpa 的 Auditable 基类提供
    // 此配置启用自动填充功能
}
