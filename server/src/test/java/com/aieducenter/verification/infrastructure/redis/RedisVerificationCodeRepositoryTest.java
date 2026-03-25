package com.aieducenter.verification.infrastructure.redis;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Redis 仓储集成测试。
 *
 * <p>此测试直接使用 Redis 连接验证原子操作的正确性。
 * 测试环境通过 Docker Compose 启动 Redis 容器（服务名: redis）。
 * 本地开发时需要启动 Redis：docker run -d -p 6379:6379 redis:7-alpine
 *
 * <p>环境变量 REDIS_HOST 可覆盖 Redis 主机名（Docker 测试用 redis，本地开发用 localhost）
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RedisVerificationCodeRepositoryTest.RedisTestConfig.class)
@TestPropertySource(properties = {
    "spring.data.redis.host=${REDIS_HOST:localhost}",
    "spring.data.redis.port=6379"
})
class RedisVerificationCodeRepositoryTest {

    private static final String EMAIL_LIMIT_PREFIX = "limit:email:";
    private static final String IP_LIMIT_PREFIX = "limit:ip:";
    private static final Duration EMAIL_LIMIT_TTL = Duration.ofSeconds(60);
    private static final Duration IP_LIMIT_TTL = Duration.ofSeconds(3600);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        // 清空测试数据
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @TestConfiguration
    static class RedisTestConfig {
        @Bean
        RedisProperties redisProperties() {
            RedisProperties properties = new RedisProperties();
            // 支持环境变量 REDIS_HOST，默认为 localhost
            String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
            properties.setHost(redisHost);
            properties.setPort(6379);
            return properties;
        }

        @Bean
        LettuceConnectionFactory redisConnectionFactory(RedisProperties properties) {
            LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .build();
            return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(properties.getHost(), properties.getPort()),
                clientConfig
            );
        }

        @Bean
        StringRedisTemplate redisTemplate(LettuceConnectionFactory connectionFactory) {
            StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
            template.afterPropertiesSet();
            return template;
        }
    }

    @Test
    void given_first_request_when_try_acquire_email_lock_then_success() {
        // Given
        String email = "test@example.com";
        String purpose = "REGISTER";
        String key = EMAIL_LIMIT_PREFIX + email + ":" + purpose;

        // When
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", EMAIL_LIMIT_TTL);

        // Then
        assertThat(acquired).isTrue();
    }

    @Test
    void given_second_request_within_60s_when_try_acquire_email_lock_then_fail() {
        // Given
        String email = "test@example.com";
        String purpose = "REGISTER";
        String key = EMAIL_LIMIT_PREFIX + email + ":" + purpose;
        redisTemplate.opsForValue().setIfAbsent(key, "1", EMAIL_LIMIT_TTL);

        // When
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", EMAIL_LIMIT_TTL);

        // Then
        assertThat(acquired).isFalse();
    }

    @Test
    void given_first_ip_request_when_check_and_increment_then_return_1() {
        // Given
        String ip = "192.168.1.1";
        String key = IP_LIMIT_PREFIX + ip;

        // When
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, IP_LIMIT_TTL);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void given_multiple_ip_requests_when_check_and_increment_then_return_incrementing_count() {
        // Given
        String ip = "192.168.1.1";
        String key = IP_LIMIT_PREFIX + ip;

        // When
        for (int i = 0; i < 5; i++) {
            redisTemplate.opsForValue().increment(key);
        }
        Long count = redisTemplate.opsForValue().increment(key);

        // Then
        assertThat(count).isEqualTo(6);
    }

    @Test
    void given_ip_limit_exceeded_when_check_and_increment_then_return_count_over_limit() {
        // Given
        String ip = "192.168.1.1";
        String key = IP_LIMIT_PREFIX + ip;

        // When - 发送 10 次请求
        for (int i = 0; i < 10; i++) {
            redisTemplate.opsForValue().increment(key);
        }
        redisTemplate.expire(key, IP_LIMIT_TTL);
        Long count = redisTemplate.opsForValue().increment(key);

        // Then
        assertThat(count).isEqualTo(11); // 超过限制
    }

    @Test
    void given_email_lock_acquired_when_ttl_expires_then_can_acquire_again() throws InterruptedException {
        // Given
        String email = "test@example.com";
        String purpose = "REGISTER";
        String key = EMAIL_LIMIT_PREFIX + email + ":" + purpose;

        // 设置较短的 TTL 用于测试
        Duration shortTtl = Duration.ofMillis(100);
        redisTemplate.opsForValue().setIfAbsent(key, "1", shortTtl);

        // When - 等待 TTL 过期
        Thread.sleep(150);
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", shortTtl);

        // Then
        assertThat(acquired).isTrue();
    }
}
