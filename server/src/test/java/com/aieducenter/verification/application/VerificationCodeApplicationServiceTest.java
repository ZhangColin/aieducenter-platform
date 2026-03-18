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
import com.aieducenter.verification.domain.error.VerificationCodeError;
import com.aieducenter.verification.domain.model.VerificationCode;
import com.aieducenter.verification.domain.model.VerificationPurpose;
import com.aieducenter.verification.domain.model.VerificationType;
import com.aieducenter.verification.domain.repository.VerificationCodeRepository;
import com.aieducenter.verification.domain.service.VerificationCodeGenerator;
import com.aieducenter.verification.domain.port.MessageSender;
import com.cartisan.core.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class VerificationCodeApplicationServiceTest {

    @Mock
    private VerificationCodeRepository repository;

    @Mock
    private VerificationCodeGenerator generator;

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private VerificationCodeApplicationService service;

    @Test
    void should_send_verification_code_successfully() {
        // Given
        SendEmailCodeCommand command = new SendEmailCodeCommand("test@example.com", "REGISTER");
        when(generator.generate()).thenReturn("123456");
        when(repository.isEmailRateLimited("test@example.com", "REGISTER")).thenReturn(false);
        when(repository.isIpRateLimited("127.0.0.1")).thenReturn(false);

        // When
        SendCodeResponse response = service.sendEmailVerificationCode(command, "127.0.0.1");

        // Then
        assertThat(response.expireInSeconds()).isEqualTo(300);
        assertThat(response.resentAfterSeconds()).isEqualTo(60);
        verify(generator).generate();
        verify(repository).save(any(VerificationCode.class));
        verify(repository).incrementEmailCount("test@example.com", "REGISTER");
        verify(repository).incrementIpCount("127.0.0.1");
        verify(messageSender).send("test@example.com", "123456", VerificationPurpose.REGISTER);
    }

    @Test
    void should_throw_exception_when_email_invalid() {
        // Given
        SendEmailCodeCommand command = new SendEmailCodeCommand("invalid-email", "REGISTER");

        // When & Then
        assertThatThrownBy(() -> service.sendEmailVerificationCode(command, "127.0.0.1"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.EMAIL_INVALID.message());
    }

    @Test
    void should_throw_exception_when_email_rate_limited() {
        // Given
        SendEmailCodeCommand command = new SendEmailCodeCommand("test@example.com", "REGISTER");
        when(repository.isEmailRateLimited("test@example.com", "REGISTER")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> service.sendEmailVerificationCode(command, "127.0.0.1"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.RATE_LIMIT_EMAIL.message());
    }

    @Test
    void should_throw_exception_when_ip_rate_limited() {
        // Given
        SendEmailCodeCommand command = new SendEmailCodeCommand("test@example.com", "REGISTER");
        when(repository.isEmailRateLimited("test@example.com", "REGISTER")).thenReturn(false);
        when(repository.isIpRateLimited("127.0.0.1")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> service.sendEmailVerificationCode(command, "127.0.0.1"))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.RATE_LIMIT_IP.message());
    }

    @Test
    void should_verify_code_successfully() {
        // Given
        VerifyCodeCommand command = new VerifyCodeCommand("test@example.com", "123456", "REGISTER");
        VerificationCode code = VerificationCode.create(
            VerificationType.EMAIL, "test@example.com", "123456", VerificationPurpose.REGISTER);
        when(repository.findById("test@example.com:REGISTER"))
            .thenReturn(java.util.Optional.of(code));

        // When
        VerifyCodeResult result = service.verifyCode(command);

        // Then
        assertThat(result.verified()).isTrue();
        assertThat(code.isUsed()).isTrue();
    }

    @Test
    void should_return_false_when_code_not_match() {
        // Given
        VerifyCodeCommand command = new VerifyCodeCommand("test@example.com", "999999", "REGISTER");
        VerificationCode code = VerificationCode.create(
            VerificationType.EMAIL, "test@example.com", "123456", VerificationPurpose.REGISTER);
        when(repository.findById("test@example.com:REGISTER"))
            .thenReturn(java.util.Optional.of(code));

        // When & Then
        assertThatThrownBy(() -> service.verifyCode(command))
            .isInstanceOf(DomainException.class);
    }

    @Test
    void should_throw_exception_when_code_not_found() {
        // Given
        VerifyCodeCommand command = new VerifyCodeCommand("test@example.com", "123456", "REGISTER");
        when(repository.findById("test@example.com:REGISTER"))
            .thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.verifyCode(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.CODE_INVALID.message());
    }
}
