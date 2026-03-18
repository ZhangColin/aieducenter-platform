package com.aieducenter.verification.domain.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class VerificationCodeGeneratorTest {

    @Test
    void given_generator_when_generate_code_then_return_6_digit() {
        // Given
        VerificationCodeGenerator generator = new VerificationCodeGenerator();

        // When
        String code = generator.generate();

        // Then
        assertThat(code).hasSize(6);
        assertThat(code).matches("\\d{6}");
    }

    @Test
    void given_generator_when_generate_multiple_times_then_return_different_codes() {
        // Given
        VerificationCodeGenerator generator = new VerificationCodeGenerator();

        // When
        String code1 = generator.generate();
        String code2 = generator.generate();

        // Then
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void given_generator_when_generate_code_then_between_100000_and_999999() {
        // Given
        VerificationCodeGenerator generator = new VerificationCodeGenerator();

        // When
        String code = generator.generate();
        int numericCode = Integer.parseInt(code);

        // Then
        assertThat(numericCode).isGreaterThanOrEqualTo(100000);
        assertThat(numericCode).isLessThanOrEqualTo(999999);
    }
}
