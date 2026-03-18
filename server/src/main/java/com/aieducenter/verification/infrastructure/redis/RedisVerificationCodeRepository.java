package com.aieducenter.verification.infrastructure.redis;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;
import com.aieducenter.verification.domain.model.VerificationCode;
import com.aieducenter.verification.domain.model.VerificationPurpose;
import com.aieducenter.verification.domain.model.VerificationType;
import com.aieducenter.verification.domain.repository.VerificationCodeRepository;

/**
 * Redis 验证码仓储实现。
 *
 * @since 0.1.0
 */
@Adapter(PortType.REPOSITORY)
@Repository
public class RedisVerificationCodeRepository implements VerificationCodeRepository {

    private static final String CODE_KEY_PREFIX = "verification:";
    private static final String EMAIL_LIMIT_PREFIX = "limit:email:";
    private static final String IP_LIMIT_PREFIX = "limit:ip:";

    private static final Duration CODE_TTL = Duration.ofSeconds(300);  // 5分钟
    private static final Duration EMAIL_LIMIT_TTL = Duration.ofSeconds(60);  // 60秒
    private static final Duration IP_LIMIT_TTL = Duration.ofSeconds(3600);  // 1小时

    private static final int IP_LIMIT_MAX = 10;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisVerificationCodeRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(VerificationCode code) {
        String key = codeKey(code.getTarget(), code.getPurpose().name());

        // 序列化为 JSON 存储
        VerificationCodeData data = new VerificationCodeData(
            code.getCode(),
            code.getExpireAt().toEpochMilli(),
            code.isUsed()
        );

        redisTemplate.opsForValue().set(key, data, CODE_TTL);
    }

    @Override
    public Optional<VerificationCode> findById(String id) {
        String key = CODE_KEY_PREFIX + id;
        VerificationCodeData data = (VerificationCodeData) redisTemplate.opsForValue().get(key);

        if (data == null) {
            return Optional.empty();
        }

        // 从 ID 解析 target 和 purpose
        String[] parts = id.split(":", 2);
        String target = parts[0];
        String purposeName = parts.length > 1 ? parts[1] : "REGISTER";
        VerificationPurpose purpose = VerificationPurpose.valueOf(purposeName);

        return Optional.of(VerificationCode.restore(
            id,
            VerificationType.EMAIL,
            target,
            data.code(),
            data.expireAt() != null ? Instant.ofEpochMilli(data.expireAt()) : null,
            data.used()
        ));
    }

    @Override
    public boolean isEmailRateLimited(String email, String purpose) {
        String key = emailLimitKey(email, purpose);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean isIpRateLimited(String ip) {
        String key = ipLimitKey(ip);
        Object countObj = redisTemplate.opsForValue().get(key);
        if (countObj instanceof Long count) {
            return count >= IP_LIMIT_MAX;
        }
        if (countObj instanceof Integer count) {
            return count >= IP_LIMIT_MAX;
        }
        return false;
    }

    @Override
    public void incrementEmailCount(String email, String purpose) {
        String key = emailLimitKey(email, purpose);
        redisTemplate.opsForValue().set(key, 1, EMAIL_LIMIT_TTL);
    }

    @Override
    public long incrementIpCount(String ip) {
        String key = ipLimitKey(ip);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, IP_LIMIT_TTL);
        }
        return count != null ? count : 1;
    }

    private String codeKey(String target, String purpose) {
        return CODE_KEY_PREFIX + target + ":" + purpose;
    }

    private String emailLimitKey(String email, String purpose) {
        return EMAIL_LIMIT_PREFIX + email + ":" + purpose;
    }

    private String ipLimitKey(String ip) {
        return IP_LIMIT_PREFIX + ip;
    }

    /**
     * 验证码数据（Redis 存储）。
     */
    private record VerificationCodeData(
        String code,
        Long expireAt,
        boolean used
    ) {}
}
