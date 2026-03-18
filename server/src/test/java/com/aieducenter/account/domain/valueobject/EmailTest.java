package com.aieducenter.account.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.cartisan.core.exception.DomainException;
import com.aieducenter.account.domain.error.UserError;

/**
 * Email 值对象测试。
 */
class EmailTest {

    // ========== 成功场景 ==========

    @Test
    void shouldCreateEmail_whenFormatValid() {
        // When
        Email email = new Email("user@example.com");

        // Then
        assertThat(email.value()).isEqualTo("user@example.com");
    }

    @Test
    void shouldCreateEmail_whenWithSubdomain() {
        // When
        Email email = new Email("user@mail.example.com");

        // Then
        assertThat(email.value()).isEqualTo("user@mail.example.com");
    }

    @Test
    void shouldCreateEmail_whenWithNumbers() {
        // When
        Email email = new Email("user123@example.com");

        // Then
        assertThat(email.value()).isEqualTo("user123@example.com");
    }

    @Test
    void shouldCreateEmail_whenWithPlus() {
        // When
        Email email = new Email("user+tag@example.com");

        // Then
        assertThat(email.value()).isEqualTo("user+tag@example.com");
    }

    @Test
    void shouldCreateEmail_whenWithHyphen() {
        // When
        Email email = new Email("user-name@example.com");

        // Then
        assertThat(email.value()).isEqualTo("user-name@example.com");
    }

    // ========== 失败场景 ==========

    @Test
    void shouldThrow_whenEmailIsNull() {
        // When & Then
        assertThatThrownBy(() -> new Email(null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.EMAIL_INVALID);
    }

    @Test
    void shouldThrow_whenEmailMissingAt() {
        // When & Then
        assertThatThrownBy(() -> new Email("userexample.com"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.EMAIL_INVALID);
    }

    @Test
    void shouldThrow_whenEmailMissingDomain() {
        // When & Then
        assertThatThrownBy(() -> new Email("user@"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.EMAIL_INVALID);
    }

    @Test
    void shouldThrow_whenEmailMissingLocal() {
        // When & Then
        assertThatThrownBy(() -> new Email("@example.com"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.EMAIL_INVALID);
    }

    @Test
    void shouldThrow_whenEmailHasSpaces() {
        // When & Then
        assertThatThrownBy(() -> new Email("user @example.com"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.EMAIL_INVALID);
    }

    // ========== 值对象相等性 ==========

    @Test
    void shouldEqual_whenSameValue() {
        // Given
        Email email1 = new Email("user@example.com");
        Email email2 = new Email("user@example.com");

        // Then
        assertThat(email1).isEqualTo(email2);
        assertThat(email1.sameValueAs(email2)).isTrue();
    }

    @Test
    void shouldNotEqual_whenDifferentValue() {
        // Given
        Email email1 = new Email("user1@example.com");
        Email email2 = new Email("user2@example.com");

        // Then
        assertThat(email1).isNotEqualTo(email2);
        assertThat(email1.sameValueAs(email2)).isFalse();
    }
}
