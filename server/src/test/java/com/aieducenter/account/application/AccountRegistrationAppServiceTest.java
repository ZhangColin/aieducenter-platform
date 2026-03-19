package com.aieducenter.account.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.aieducenter.account.application.dto.RegisterByEmailCommand;
import com.aieducenter.account.domain.error.UserError;
import com.aieducenter.account.domain.event.UserRegisteredEvent;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.domain.error.VerificationCodeError;
import com.cartisan.core.exception.DomainException;
import com.cartisan.security.authentication.AuthenticationService;
import com.cartisan.security.authentication.TokenInfo;

@ExtendWith(MockitoExtension.class)
class AccountRegistrationAppServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodeAppService verificationCodeAppService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AccountRegistrationAppService registrationAppService;

    @Test
    void given_valid_command_when_register_by_email_then_return_token_and_publish_event() {
        // Given
        var command = new RegisterByEmailCommand("testuser", "test@example.com", "password123", "Test User", "123456");
        var tokenInfo = new TokenInfo("test-token", 1L, Instant.now().plusSeconds(3600));

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(authenticationService.login(any())).thenReturn(tokenInfo);

        // When
        var result = registrationAppService.registerByEmail(command);

        // Then
        assertThat(result.token()).isEqualTo("test-token");
        verify(applicationEventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void given_invalid_verification_code_when_register_by_email_then_exception_propagates() {
        // Given
        var command = new RegisterByEmailCommand("testuser", "test@example.com", "password123", "Test User", "wrong");
        doThrow(new DomainException(VerificationCodeError.CODE_INVALID))
            .when(verificationCodeAppService).verifyCode(any());

        // When & Then
        assertThatThrownBy(() -> registrationAppService.registerByEmail(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(VerificationCodeError.CODE_INVALID.message());
    }

    @Test
    void given_existing_username_when_register_by_email_then_throw_username_already_exists() {
        // Given
        var command = new RegisterByEmailCommand("testuser", "test@example.com", "password123", "Test User", "123456");
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> registrationAppService.registerByEmail(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(UserError.USERNAME_ALREADY_EXISTS.message());
    }

    @Test
    void given_existing_email_when_register_by_email_then_throw_email_already_exists() {
        // Given
        var command = new RegisterByEmailCommand("testuser", "test@example.com", "password123", "Test User", "123456");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> registrationAppService.registerByEmail(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(UserError.EMAIL_ALREADY_EXISTS.message());
    }

    @Test
    void given_weak_password_when_register_by_email_then_throw_password_weak() {
        // Given
        var command = new RegisterByEmailCommand("testuser", "test@example.com", "weakpass", "Test User", "123456");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> registrationAppService.registerByEmail(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(UserError.PASSWORD_WEAK.message());
    }
}
