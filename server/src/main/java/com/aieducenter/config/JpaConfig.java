package com.aieducenter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 配置类。
 *
 * <p>启用 JPA Auditing 以支持审计字段自动填充：
 * <ul>
 *   <li>{@code createdAt} — 首次保存时自动填充</li>
 *   <li>{@code updatedAt} — 每次保存时自动更新</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
