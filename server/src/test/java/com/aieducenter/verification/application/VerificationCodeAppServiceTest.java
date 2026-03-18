package com.aieducenter.verification.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.verification.application.dto.SendEmailCodeCommand;
import com.aieducenter.verification.application.dto.SendCodeResponse;
import com.aieducenter.verification.application.dto.VerifyCodeCommand;
import com.aieducenter.verification.application.dto.VerifyCodeResult;
import com.aieducenter.verification.config.VerificationCodeProperties;
import com.aieducenter.verification.domain.error.VerificationCodeError;
import com.aieducenter.verification.domain.model.VerificationCode;
import com.aieducenter.verification.domain.model.VerificationPurpose;
import com.aieducenter.verification.domain.model.VerificationType;
import com.aieducenter.verification.domain.repository.VerificationCodeRepository;
import com.aieducenter.verification.domain.service.VerificationCodeGenerationService;
import com.aieducenter.verification.domain.port.MessageSender;
import com.cartisan.core.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class VerificationCodeAppServiceTest {

    @Mock
    private VerificationCodeRepository repository;

    @Mock
    private VerificationCodeGenerationService generator;

    @Mock
    private MessageSender messageSender;

    @Mock
    private VerificationCodeProperties properties;

    @InjectMocks
    private VerificationCodeAppService service;

    @Test
    void given_valid_email_and_no_rate_limit_when_send_verification_code_then_success() {
        // Given
        when(properties.getExpireMinutes()).thenReturn(5);
        when(properties.getEmailCooldownSeconds()).thenReturn(60L);
        when(properties.getIpMaxPerHour()).thenReturn(10);

        SendEmailCodeCommand command = new SendEmailCodeCommand("test@example.com", "REGISTER");
        when(generator.generate()).thenReturn("123456");
        when(repository.tryAcquireEmailLock("test@example.com", "REGISTER")).thenReturn(true);
        when(repository.checkAndIncrementIp("127.0.0.1")).thenReturn(1L);

        // When
        SendCodeResponse response = service.sendEmailVerificationCode(command, "127.0.0.1");

        // Then
        assertThat(response.expireInSeconds()).isEqualTo(300);
        assertThat(response.resentAfterSeconds()).isEqualTo(60);
        verify(generator).generate();
        verify(repository).save(any(VerificationCode.class));
        verify(repository).tryAcquireEmailLock("test@example.com", "REGISTER");
        verify(repository).checkAndIncrementIp("127.0.0.1");
        verify(messageSender).send("test@example.com", "123456", VerificationPurpose.REGISTER);
    }

    @Test
    void given_invalid_email_format_when_send_verification_code_then_throw_exception() {
        // Given
        SendEmailCodeCommand command = new SendEmailCodeCommand("invalid-email", "REGISTER");

        // When & Then
        assertThatThrownBy(() -> service.sendEmailVerificationCode(command, "127.0.0.1"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.EMAIL_INVALID.message());
    }

    @Test
    void given_email_rate_limited_when_send_verification_code_then_throw_exception() {
        // Given
        when(repository.tryAcquireEmailLock("test@example.com", "REGISTER")).thenReturn(false);

        SendEmailCodeCommand command = new SendEmailCodeCommand("test@example.com", "REGISTER");

        // When & Then
        assertThatThrownBy(() -> service.sendEmailVerificationCode(command, "127.0.0.1"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.RATE_LIMIT_EMAIL.message());
    }

    @Test
    void given_ip_rate_limited_when_send_verification_code_then_throw_exception() {
        // Given
        when(properties.getIpMaxPerHour()).thenReturn(10);
        when(repository.tryAcquireEmailLock("test@example.com", "REGISTER")).thenReturn(true);
        when(repository.checkAndIncrementIp("127.0.0.1")).thenReturn(11L);

        SendEmailCodeCommand command = new SendEmailCodeCommand("test@example.com", "REGISTER");

        // When & Then
        assertThatThrownBy(() -> service.sendEmailVerificationCode(command, "127.0.0.1"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.RATE_LIMIT_IP.message());
    }

    @Test
    void given_valid_code_when_verify_code_then_success_and_mark_used() {
        // Given
        VerifyCodeCommand command = new VerifyCodeCommand("test@example.com", "123456", "REGISTER");
        when(repository.verifyAndMarkAsUsed("test@example.com:REGISTER", "123456")).thenReturn(true);

        // When
        VerifyCodeResult result = service.verifyCode(command);

        // Then
        assertThat(result.verified()).isTrue();
        verify(repository).verifyAndMarkAsUsed("test@example.com:REGISTER", "123456");
    }

    @Test
    void given_invalid_code_when_verify_code_then_throw_exception() {
        // Given
        VerifyCodeCommand command = new VerifyCodeCommand("test@example.com", "999999", "REGISTER");
        VerificationCode code = VerificationCode.create(
            VerificationType.EMAIL, "test@example.com", "123456", VerificationPurpose.REGISTER);
        when(repository.verifyAndMarkAsUsed("test@example.com:REGISTER", "999999")).thenReturn(false);
        when(repository.findById("test@example.com:REGISTER"))
            .thenReturn(java.util.Optional.of(code));

        // When & Then
        assertThatThrownBy(() -> service.verifyCode(command))
            .isInstanceOf(DomainException.class);
    }

    @Test
    void given_code_not_found_when_verify_code_then_throw_invalid_exception() {
        // Given
        VerifyCodeCommand command = new VerifyCodeCommand("test@example.com", "123456", "REGISTER");
        when(repository.verifyAndMarkAsUsed("test@example.com:REGISTER", "123456")).thenReturn(false);
        when(repository.findById("test@example.com:REGISTER"))
            .thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.verifyCode(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.CODE_INVALID.message());
    }

    @Test
    void given_ip_count_at_limit_when_send_verification_code_then_success() {
        // Given — count == limit (<=, not <)
        when(properties.getExpireMinutes()).thenReturn(5);
        when(properties.getEmailCooldownSeconds()).thenReturn(60L);
        when(properties.getIpMaxPerHour()).thenReturn(10);
        when(repository.tryAcquireEmailLock("test@example.com", "REGISTER")).thenReturn(true);
        when(repository.checkAndIncrementIp("127.0.0.1")).thenReturn(10L);
        when(generator.generate()).thenReturn("123456");

        SendEmailCodeCommand command = new SendEmailCodeCommand("test@example.com", "REGISTER");

        // When & Then — exactly at limit should succeed
        assertThatCode(() -> service.sendEmailVerificationCode(command, "127.0.0.1"))
            .doesNotThrowAnyException();
    }

    @Test
    void given_invalid_email_when_verify_code_then_throw_exception() {
        // Given
        VerifyCodeCommand command = new VerifyCodeCommand("invalid-email", "123456", "REGISTER");

        // When & Then
        assertThatThrownBy(() -> service.verifyCode(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.EMAIL_INVALID.message());
    }

    @Test
    void given_code_already_used_when_verify_code_then_throw_code_already_used_exception() {
        // Given
        VerifyCodeCommand command = new VerifyCodeCommand("test@example.com", "123456", "REGISTER");
        when(repository.verifyAndMarkAsUsed("test@example.com:REGISTER", "123456")).thenReturn(false);
        VerificationCode usedCode = VerificationCode.restore(
            "test@example.com:REGISTER", VerificationType.EMAIL, "test@example.com",
            "123456", java.time.Instant.now().plusSeconds(300), true, VerificationPurpose.REGISTER
        );
        when(repository.findById("test@example.com:REGISTER")).thenReturn(java.util.Optional.of(usedCode));

        // When & Then
        assertThatThrownBy(() -> service.verifyCode(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.CODE_ALREADY_USED.message());
    }

    @Test
    void given_code_expired_when_verify_code_then_throw_code_expired_exception() {
        // Given
        VerifyCodeCommand command = new VerifyCodeCommand("test@example.com", "123456", "REGISTER");
        when(repository.verifyAndMarkAsUsed("test@example.com:REGISTER", "123456")).thenReturn(false);
        VerificationCode expiredCode = VerificationCode.restore(
            "test@example.com:REGISTER", VerificationType.EMAIL, "test@example.com",
            "123456", java.time.Instant.now().minusSeconds(1), false, VerificationPurpose.REGISTER
        );
        when(repository.findById("test@example.com:REGISTER")).thenReturn(java.util.Optional.of(expiredCode));

        // When & Then
        assertThatThrownBy(() -> service.verifyCode(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.CODE_EXPIRED.message());
    }
}
