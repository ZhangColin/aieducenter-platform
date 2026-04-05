package com.aieducenter.account.domain.port;

import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;

/**
 * 验证码端口。
 *
 * <p>南向接口，定义验证码验证能力。</p>
 * <p>使用 PortType.CLIENT 表示这是对其他上下文服务的端口。</p>
 *
 * @since 0.1.0
 */
@Port(PortType.CLIENT)
public interface VerificationCodePort {

    /**
     * 验证短信验证码。
     *
     * @param phone 手机号
     * @param code 验证码
     * @param purpose 用途（REGISTER、LOGIN、RESET_PASSWORD 等）
     * @throws com.cartisan.core.exception.DomainException 验证码错误、已过期、已使用
     */
    void verifyPhoneCode(String phone, String code, String purpose);

    /**
     * 验证邮箱验证码。
     *
     * @param email 邮箱
     * @param code 验证码
     * @param purpose 用途（REGISTER、LOGIN、RESET_PASSWORD 等）
     * @throws com.cartisan.core.exception.DomainException 验证码错误、已过期、已使用
     */
    void verifyCode(String email, String code, String purpose);
}
