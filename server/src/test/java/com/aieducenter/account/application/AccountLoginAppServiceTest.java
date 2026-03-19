package com.aieducenter.account.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.account.application.dto.LoginByPasswordCommand;
import com.aieducenter.account.application.dto.LoginBySmsCommand;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.domain.error.UserError;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.application.dto.VerifySmsCodeCommand;
import com.cartisan.core.exception.DomainException;
import com.cartisan.security.authentication.AuthenticationService;
import com.cartisan.security.authentication.TokenInfo;

@ExtendWith(MockitoExtension.class)
class AccountLoginAppServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodeAppService verificationCodeAppService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AccountLoginAppService loginAppService;

    // ========== loginByPassword tests ==========

    // BCrypt hash of "password123" (cost 10) — shared by the three happy-path tests
    private static final String HASH_PASSWORD123 =
        "$2a$10$blloEbxpkrK8297ectXfNubsI5EUdub2zl.vThFLy.G4Dqyj37PXe";

    @Test
    void given_username_when_login_by_password_then_return_token() {
        // Given
        var user = User.restore(1001L, "testuser", null, null, HASH_PASSWORD123, "Test", null);
        var command = new LoginByPasswordCommand("testuser", "password123");
        var tokenInfo = new TokenInfo("token-abc", 1001L, Instant.now().plusSeconds(3600));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(authenticationService.login(1001L)).thenReturn(tokenInfo);

        // When
        var result = loginAppService.loginByPassword(command);

        // Then
        assertThat(result.token()).isEqualTo("token-abc");
    }

    @Test
    void given_email_when_login_by_password_then_return_token() {
        // Given
        var user = User.restore(1002L, "testuser", "test@example.com", null, HASH_PASSWORD123, "Test", null);
        var command = new LoginByPasswordCommand("test@example.com", "password123");
        var tokenInfo = new TokenInfo("token-email", 1002L, Instant.now().plusSeconds(3600));

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(authenticationService.login(1002L)).thenReturn(tokenInfo);

        // When
        var result = loginAppService.loginByPassword(command);

        // Then
        assertThat(result.token()).isEqualTo("token-email");
    }

    @Test
    void given_phone_when_login_by_password_then_return_token() {
        // Given
        var user = User.restore(1003L, "testuser", null, "13812345678", HASH_PASSWORD123, "Test", null);
        var command = new LoginByPasswordCommand("13812345678", "password123");
        var tokenInfo = new TokenInfo("token-phone", 1003L, Instant.now().plusSeconds(3600));

        when(userRepository.findByUsername("13812345678")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("13812345678")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber("13812345678")).thenReturn(Optional.of(user));
        when(authenticationService.login(1003L)).thenReturn(tokenInfo);

        // When
        var result = loginAppService.loginByPassword(command);

        // Then
        assertThat(result.token()).isEqualTo("token-phone");
    }

    @Test
    void given_unknown_account_when_login_by_password_then_throw_account_not_found() {
        // Given
        var command = new LoginByPasswordCommand("unknown", "password123");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber("unknown")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginAppService.loginByPassword(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(UserError.ACCOUNT_NOT_FOUND.message());
    }

    @Test
    void given_wrong_password_when_login_by_password_then_throw_login_password_incorrect() {
        // Given
        // Use a real BCrypt hash of "correctPass1" so matchesPassword works
        var user = User.restore(1004L, "testuser", null, null,
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", "Test", null);
        var command = new LoginByPasswordCommand("testuser", "wrongPassword1");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> loginAppService.loginByPassword(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(UserError.LOGIN_PASSWORD_INCORRECT.message());
    }

    // ========== loginBySms tests ==========

    @Test
    void given_valid_sms_code_when_login_by_sms_then_return_token() {
        // Given
        var user = User.restore(1005L, "testuser", null, "13812345678", "$2a$10$validhashedpassword", "Test", null);
        var command = new LoginBySmsCommand("13812345678", "123456");
        var tokenInfo = new TokenInfo("token-sms", 1005L, Instant.now().plusSeconds(3600));

        when(userRepository.findByPhoneNumber("13812345678")).thenReturn(Optional.of(user));
        when(authenticationService.login(1005L)).thenReturn(tokenInfo);

        // When
        var result = loginAppService.loginBySms(command);

        // Then
        assertThat(result.token()).isEqualTo("token-sms");
        verify(verificationCodeAppService).verifyPhoneCode(new VerifySmsCodeCommand("13812345678", "123456", "LOGIN"));
    }

    @Test
    void given_unregistered_phone_when_login_by_sms_then_throw_account_not_found() {
        // Given
        var command = new LoginBySmsCommand("13899999999", "123456");

        when(userRepository.findByPhoneNumber("13899999999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> loginAppService.loginBySms(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(UserError.ACCOUNT_NOT_FOUND.message());
        verify(verificationCodeAppService).verifyPhoneCode(any());
    }

    @Test
    void given_invalid_sms_code_when_login_by_sms_then_propagate_verification_error() {
        // Given
        var command = new LoginBySmsCommand("13812345678", "000000");
        var verificationError = new DomainException(
            com.aieducenter.verification.domain.error.VerificationCodeError.CODE_INVALID);

        doThrow(verificationError)
            .when(verificationCodeAppService)
            .verifyPhoneCode(any(VerifySmsCodeCommand.class));

        // When & Then
        assertThatThrownBy(() -> loginAppService.loginBySms(command))
            .isSameAs(verificationError);
    }
}
