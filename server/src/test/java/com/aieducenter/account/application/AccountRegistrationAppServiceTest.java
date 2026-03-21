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

import com.aieducenter.account.application.dto.RegisterCommand;
import com.aieducenter.account.domain.error.UserError;
import com.aieducenter.account.domain.event.UserRegisteredEvent;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.application.dto.VerifyCodeResult;
import com.cartisan.core.exception.DomainException;
import com.cartisan.security.authentication.AuthenticationService;
import com.cartisan.security.authentication.TokenInfo;

@ExtendWith(MockitoExtension.class)
class AccountRegistrationAppServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private VerificationCodeAppService verificationCodeAppService;

    @InjectMocks
    private AccountRegistrationAppService registrationAppService;

    @Test
    void given_valid_command_when_register_then_return_token_and_publish_event() {
        // Given
        var command = new RegisterCommand("testuser", "Password1", null, null, "123456");
        var tokenInfo = new TokenInfo("test-token", 1L, Instant.now().plusSeconds(3600));

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByPhoneNumber(null)).thenReturn(false);
        when(verificationCodeAppService.verifyPhoneCode(any()))
            .thenReturn(new VerifyCodeResult(true, "OK"));
        when(authenticationService.login(any())).thenReturn(tokenInfo);

        // When
        var result = registrationAppService.register(command);

        // Then
        assertThat(result.token()).isEqualTo("test-token");
        verify(applicationEventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void given_duplicate_username_when_register_then_throw_409() {
        // Given
        var command = new RegisterCommand("testuser", "Password1", null, null, "123456");
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> registrationAppService.register(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(UserError.USERNAME_ALREADY_EXISTS.message());
    }

    @Test
    void given_duplicate_phone_when_register_then_throw_409() {
        // Given
        var command = new RegisterCommand("testuser", "Password1", null, "13812345678", "123456");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("13812345678")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> registrationAppService.register(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(UserError.PHONE_NUMBER_ALREADY_EXISTS.message());
    }
}
