package com.aieducenter.verification.infrastructure.redis;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 仓储集成测试。
 *
 * <p>此测试直接使用 Redis 连接验证原子操作的正确性。
 * 集成测试需要本地 Redis (localhost:6379) 运行。
 */
class RedisVerificationCodeRepositoryTest {

    private static final String EMAIL_LIMIT_PREFIX = "limit:email:";
    private static final String IP_LIMIT_PREFIX = "limit:ip:";
    private static final Duration EMAIL_LIMIT_TTL = Duration.ofSeconds(60);
    private static final Duration IP_LIMIT_TTL = Duration.ofSeconds(3600);

    private StringRedisTemplate redisTemplate;
    private LettuceConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        connectionFactory = new LettuceConnectionFactory("localhost", 6379);
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();

        // 清空测试数据
        connectionFactory.getConnection().flushDb();
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
