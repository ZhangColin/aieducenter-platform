package com.aieducenter.account.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.cartisan.core.exception.DomainException;
import com.aieducenter.account.domain.error.UserError;

/**
 * Username 值对象测试。
 */
class UsernameTest {

    // ========== 成功场景 ==========

    @Test
    void shouldCreateUsername_whenFormatValid() {
        // When
        Username username = new Username("john_doe");

        // Then
        assertThat(username.value()).isEqualTo("john_doe");
    }

    @Test
    void shouldCreateUsername_whenIsMinimumLength() {
        // When
        Username username = new Username("abc");  // 3 位，最小长度

        // Then
        assertThat(username.value()).isEqualTo("abc");
    }

    @Test
    void shouldCreateUsername_whenIsMaximumLength() {
        // When
        Username username = new Username("abc12345678901234567");  // 20 位，最大长度

        // Then
        assertThat(username.value()).isEqualTo("abc12345678901234567");
    }

    @Test
    void shouldCreateUsername_whenContainsNumbers() {
        // When
        Username username = new Username("user123");

        // Then
        assertThat(username.value()).isEqualTo("user123");
    }

    @Test
    void shouldCreateUsername_whenContainsUnderscore() {
        // When
        Username username = new Username("user_name");

        // Then
        assertThat(username.value()).isEqualTo("user_name");
    }

    // ========== 失败场景 ==========

    @Test
    void shouldThrow_whenUsernameIsNull() {
        // When & Then
        assertThatThrownBy(() -> new Username(null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void shouldThrow_whenUsernameTooShort() {
        // When & Then
        assertThatThrownBy(() -> new Username("ab"))  // 2 位，小于 3
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void shouldThrow_whenUsernameTooLong() {
        // When & Then
        assertThatThrownBy(() -> new Username("abc123456789012345678"))  // 21 位，大于 20
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void shouldThrow_whenUsernameStartsWithNumber() {
        // When & Then
        assertThatThrownBy(() -> new Username("123abc"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void shouldThrow_whenUsernameStartsWithUnderscore() {
        // When & Then
        assertThatThrownBy(() -> new Username("_username"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void shouldThrow_whenUsernameContainsSpecialChars() {
        // When & Then
        assertThatThrownBy(() -> new Username("user-name"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void shouldThrow_whenUsernameContainsSpaces() {
        // When & Then
        assertThatThrownBy(() -> new Username("user name"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    // ========== 值对象相等性 ==========

    @Test
    void shouldEqual_whenSameValue() {
        // Given
        Username username1 = new Username("john_doe");
        Username username2 = new Username("john_doe");

        // Then
        assertThat(username1).isEqualTo(username2);
        assertThat(username1.sameValueAs(username2)).isTrue();
    }

    @Test
    void shouldNotEqual_whenDifferentValue() {
        // Given
        Username username1 = new Username("john_doe");
        Username username2 = new Username("jane_doe");

        // Then
        assertThat(username1).isNotEqualTo(username2);
        assertThat(username1.sameValueAs(username2)).isFalse();
    }
}
