package com.aieducenter.verification.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import com.cartisan.core.exception.DomainException;

import static org.assertj.core.api.Assertions.*;

class VerificationCodeTest {

    @Test
    void should_create_verification_code_successfully_when_input_valid() {
        // Given
        String email = "test@example.com";
        String code = "123456";
        VerificationPurpose purpose = VerificationPurpose.REGISTER;

        // When
        VerificationCode verificationCode = VerificationCode.create(
            VerificationType.EMAIL, email, code, purpose
        );

        // Then
        assertThat(verificationCode.getId()).isNotEmpty();
        assertThat(verificationCode.getType()).isEqualTo(VerificationType.EMAIL);
        assertThat(verificationCode.getTarget()).isEqualTo(email);
        assertThat(verificationCode.getCode()).isEqualTo(code);
        assertThat(verificationCode.getPurpose()).isEqualTo(purpose);
        assertThat(verificationCode.isUsed()).isFalse();
        assertThat(verificationCode.getExpireAt()).isAfter(Instant.now());
        assertThat(verificationCode.getCreatedAt()).isNotNull();
    }

    @Test
    void should_return_true_when_code_matches_and_not_expired_and_not_used() {
        // Given
        VerificationCode verificationCode = VerificationCode.create(
            VerificationType.EMAIL, "test@example.com", "123456", VerificationPurpose.REGISTER
        );

        // When
        boolean valid = verificationCode.isValid("123456");

        // Then
        assertThat(valid).isTrue();
    }

    @Test
    void should_return_false_when_code_not_match() {
        // Given
        VerificationCode verificationCode = VerificationCode.create(
            VerificationType.EMAIL, "test@example.com", "123456", VerificationPurpose.REGISTER
        );

        // When
        boolean valid = verificationCode.isValid("999999");

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    void should_return_false_when_code_is_used() {
        // Given
        VerificationCode verificationCode = VerificationCode.create(
            VerificationType.EMAIL, "test@example.com", "123456", VerificationPurpose.REGISTER
        );
        verificationCode.markAsUsed();

        // When
        boolean valid = verificationCode.isValid("123456");

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    void should_mark_as_used_successfully_when_code_not_used() {
        // Given
        VerificationCode verificationCode = VerificationCode.create(
            VerificationType.EMAIL, "test@example.com", "123456", VerificationPurpose.REGISTER
        );

        // When
        verificationCode.markAsUsed();

        // Then
        assertThat(verificationCode.isUsed()).isTrue();
    }

    @Test
    void should_throw_exception_when_mark_used_on_already_used_code() {
        // Given
        VerificationCode verificationCode = VerificationCode.create(
            VerificationType.EMAIL, "test@example.com", "123456", VerificationPurpose.REGISTER
        );
        verificationCode.markAsUsed();

        // When & Then
        assertThatThrownBy(() -> verificationCode.markAsUsed())
            .isInstanceOf(DomainException.class);
    }

    @Test
    void should_have_same_identity_when_id_matches() {
        // Given
        VerificationCode code1 = VerificationCode.create(
            VerificationType.EMAIL, "test@example.com", "123456", VerificationPurpose.REGISTER
        );
        VerificationCode code2 = VerificationCode.create(
            VerificationType.EMAIL, "test@example.com", "654321", VerificationPurpose.REGISTER
        );

        // When & Then
        assertThat(code1.sameIdentityAs(code2)).isTrue();
    }
}
