package com.aieducenter.verification.domain.repository;

import java.util.Optional;

import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;
import com.aieducenter.verification.domain.model.VerificationCode;

/**
 * 验证码仓储接口。
 *
 * @since 0.1.0
 */
@Port(PortType.REPOSITORY)
public interface VerificationCodeRepository {

    /**
     * 保存验证码。
     *
     * @param code 验证码
     */
    void save(VerificationCode code);

    /**
     * 查找验证码。
     *
     * @param id 验证码标识
     * @return 验证码
     */
    Optional<VerificationCode> findById(String id);

    /**
     * 检查邮箱限流。
     *
     * @param email 邮箱
     * @param purpose 目的
     * @return 是否在限流期内
     */
    boolean isEmailRateLimited(String email, String purpose);

    /**
     * 检查 IP 限流。
     *
     * @param ip IP地址
     * @return 是否在限流期内
     */
    boolean isIpRateLimited(String ip);

    /**
     * 增加邮箱发送计数。
     *
     * @param email 邮箱
     * @param purpose 目的
     */
    void incrementEmailCount(String email, String purpose);

    /**
     * 增加IP发送计数。
     *
     * @param ip IP地址
     * @return 当前计数
     */
    long incrementIpCount(String ip);
}
