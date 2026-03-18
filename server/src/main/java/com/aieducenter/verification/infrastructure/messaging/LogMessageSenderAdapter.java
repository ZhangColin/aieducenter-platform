package com.aieducenter.verification.infrastructure.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;
import com.aieducenter.verification.domain.model.VerificationPurpose;
import com.aieducenter.verification.domain.port.MessageSender;

/**
 * 日志消息发送器（模拟实现）。
 *
 * <h3>职责</h3>
 * 将验证码输出到日志，模拟真实邮件发送。
 *
 * @since 0.1.0
 */
@Adapter(PortType.CLIENT)
@Component("verificationLogMessageSender")
public class LogMessageSenderAdapter implements MessageSender {

    private static final Logger log = LoggerFactory.getLogger(LogMessageSenderAdapter.class);

    @Override
    public void send(String target, String code, VerificationPurpose purpose) {
        log.info("[模拟发送验证码] 目标: {}, 验证码: {}, 目的: {}", target, code, purpose);
    }
}
