package com.aieducenter.account.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.aieducenter.account.domain.error.UserError;
import com.cartisan.core.exception.DomainException;

/**
 * User 聚合根测试。
 */
class UserTest {

    // ========== 创建用户 ==========

    @Test
    void shouldCreateUser_whenRequiredFieldsValid() {
        // When
        User user = new User("john_doe", "password123", "John Doe", null);

        // Then
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getNickname()).isEqualTo("John Doe");
        assertThat(user.getAvatar()).isEmpty();
    }

    @Test
    void shouldSetNicknameToUsername_whenNicknameBlank() {
        // When
        User user = new User("john_doe", "password123", "", null);

        // Then
        assertThat(user.getNickname()).isEqualTo("john_doe");
    }

    @Test
    void shouldSetNicknameToUsername_whenNicknameNull() {
        // When
        User user = new User("john_doe", "password123", null, null);

        // Then
        assertThat(user.getNickname()).isEqualTo("john_doe");
    }

    @Test
    void shouldSetNicknameToUsername_whenNicknameOnlySpaces() {
        // When
        User user = new User("john_doe", "password123", "   ", null);

        // Then
        assertThat(user.getNickname()).isEqualTo("john_doe");
    }

    // ========== 用户名格式验证 ==========

    @Test
    void shouldCreateUser_whenUsernameValid() {
        // When & Then - 字母开头
        assertThat(new User("abc", "password123", null, null).getUsername()).isEqualTo("abc");

        // When & Then - 字母开头，包含数字和下划线
        assertThat(new User("user_123", "password123", null, null).getUsername()).isEqualTo("user_123");

        // When & Then - 最大长度 20
        String maxUsername = "a" + "_".repeat(18) + "b";
        assertThat(new User(maxUsername, "password123", null, null).getUsername()).hasSize(20);
    }

    @Test
    void shouldThrow_whenUsernameStartsWithNumber() {
        // When & Then
        assertThatThrownBy(() -> new User("123invalid", "password123", null, null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void shouldThrow_whenUsernameStartsWithUnderscore() {
        // When & Then
        assertThatThrownBy(() -> new User("_invalid", "password123", null, null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void shouldThrow_whenUsernameTooShort() {
        // When & Then - 2 字符
        assertThatThrownBy(() -> new User("ab", "password123", null, null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);

        // When & Then - 1 字符
        assertThatThrownBy(() -> new User("a", "password123", null, null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void shouldThrow_whenUsernameTooLong() {
        // When & Then - 21 字符
        assertThatThrownBy(() -> new User("a".repeat(21), "password123", null, null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    @Test
    void shouldThrow_whenUsernameContainsInvalidChars() {
        // When & Then - 包含特殊字符
        assertThatThrownBy(() -> new User("user@name", "password123", null, null))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.USERNAME_INVALID);
    }

    // ========== 密码验证 ==========

    @Test
    void shouldMatchPassword_whenCorrect() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When & Then
        assertThat(user.matchesPassword("password123")).isTrue();
    }

    @Test
    void shouldNotMatchPassword_whenIncorrect() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When & Then
        assertThat(user.matchesPassword("wrongpassword")).isFalse();
    }

    @Test
    void shouldMatchPassword_whenEmptyPassword() {
        // Given
        User user = new User("john_doe", "", null, null);

        // When & Then
        assertThat(user.matchesPassword("")).isTrue();
    }

    // ========== 修改用户名 ==========

    @Test
    void shouldUpdateUsername_whenNewUsernameValid() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When
        user.updateUsername("jane_doe");

        // Then
        assertThat(user.getUsername()).isEqualTo("jane_doe");
    }

    // ========== 修改昵称 ==========

    @Test
    void shouldUpdateNickname_whenNewNicknameValid() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When
        user.updateNickname("New Nickname");

        // Then
        assertThat(user.getNickname()).isEqualTo("New Nickname");
    }

    @Test
    void shouldNotUpdateNickname_whenNewNicknameBlank() {
        // Given
        User user = new User("john_doe", "password123", "Old Nickname", null);

        // When
        user.updateNickname("");

        // Then - 昵称不应被更新
        assertThat(user.getNickname()).isEqualTo("Old Nickname");
    }

    @Test
    void shouldNotUpdateNickname_whenNewNicknameNull() {
        // Given
        User user = new User("john_doe", "password123", "Old Nickname", null);

        // When
        user.updateNickname(null);

        // Then - 昵称不应被更新
        assertThat(user.getNickname()).isEqualTo("Old Nickname");
    }

    // ========== 修改头像 ==========

    @Test
    void shouldUpdateAvatar_whenNewAvatarValid() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When
        user.updateAvatar("https://example.com/avatar.jpg");

        // Then
        assertThat(user.getAvatar()).isPresent();
        assertThat(user.getAvatar().get()).isEqualTo("https://example.com/avatar.jpg");
    }

    @Test
    void shouldClearAvatar_whenNewAvatarNull() {
        // Given
        User user = new User("john_doe", "password123", null, "https://example.com/avatar.jpg");

        // When
        user.updateAvatar(null);

        // Then
        assertThat(user.getAvatar()).isEmpty();
    }

    // ========== 修改密码 ==========

    @Test
    void shouldUpdatePassword_whenOldPasswordCorrect() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When
        user.updatePassword("password123", "newPassword456");

        // Then
        assertThat(user.matchesPassword("newPassword456")).isTrue();
        assertThat(user.matchesPassword("password123")).isFalse();
    }

    @Test
    void shouldThrow_whenOldPasswordIncorrect() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When & Then
        assertThatThrownBy(() -> user.updatePassword("wrongpassword", "newPassword456"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PASSWORD_INCORRECT);
    }

    @Test
    void shouldKeepOldPassword_whenUpdatePasswordFails() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When
        try {
            user.updatePassword("wrongpassword", "newPassword456");
        } catch (DomainException e) {
            // Expected
        }

        // Then - 密码不应被修改
        assertThat(user.matchesPassword("password123")).isTrue();
        assertThat(user.matchesPassword("newPassword456")).isFalse();
    }

    // ========== 可选登录凭证 ==========

    @Test
    void shouldReturnEmptyEmail_whenNotSet() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // Then
        assertThat(user.getEmail()).isEmpty();
    }

    @Test
    void shouldReturnEmptyPhoneNumber_whenNotSet() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // Then
        assertThat(user.getPhoneNumber()).isEmpty();
    }

    // ========== Email 验证 ==========

    @Test
    void shouldUpdateEmail_whenEmailValid() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When
        user.updateEmail("john@example.com");

        // Then
        assertThat(user.getEmail()).isPresent();
        assertThat(user.getEmail().get()).isEqualTo("john@example.com");
    }

    @Test
    void shouldClearEmail_whenEmailNull() {
        // Given
        User user = new User("john_doe", "password123", null, null);
        user.updateEmail("john@example.com");

        // When
        user.updateEmail(null);

        // Then
        assertThat(user.getEmail()).isEmpty();
    }

    @Test
    void shouldThrow_whenEmailInvalid() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When & Then - 缺少 @
        assertThatThrownBy(() -> user.updateEmail("invalidemail"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.EMAIL_INVALID);

        // When & Then - 缺少域名
        assertThatThrownBy(() -> user.updateEmail("invalid@"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.EMAIL_INVALID);
    }

    @Test
    void shouldUpdateEmail_whenEmailWithSubdomain() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When
        user.updateEmail("john@mail.example.com");

        // Then
        assertThat(user.getEmail()).isPresent();
        assertThat(user.getEmail().get()).isEqualTo("john@mail.example.com");
    }

    // ========== PhoneNumber 验证 ==========

    @Test
    void shouldUpdatePhoneNumber_whenPhoneValid() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When
        user.updatePhoneNumber("13812345678");

        // Then
        assertThat(user.getPhoneNumber()).isPresent();
        assertThat(user.getPhoneNumber().get()).isEqualTo("13812345678");
    }

    @Test
    void shouldClearPhoneNumber_whenPhoneNull() {
        // Given
        User user = new User("john_doe", "password123", null, null);
        user.updatePhoneNumber("13812345678");

        // When
        user.updatePhoneNumber(null);

        // Then
        assertThat(user.getPhoneNumber()).isEmpty();
    }

    @Test
    void shouldThrow_whenPhoneNumberInvalid() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When & Then - 不是 1 开头
        assertThatThrownBy(() -> user.updatePhoneNumber("23812345678"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);

        // When & Then - 第二位不是 3-9
        assertThatThrownBy(() -> user.updatePhoneNumber("10812345678"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);

        // When & Then - 少于 11 位
        assertThatThrownBy(() -> user.updatePhoneNumber("1381234567"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);

        // When & Then - 多于 11 位
        assertThatThrownBy(() -> user.updatePhoneNumber("138123456789"))
            .isInstanceOf(DomainException.class)
            .extracting("codeMessage")
            .isEqualTo(UserError.PHONE_NUMBER_INVALID);
    }

    @Test
    void shouldUpdatePhoneNumber_whenPhoneWithValidSecondDigit() {
        // Given
        User user = new User("john_doe", "password123", null, null);

        // When - 第二位是 3
        user.updatePhoneNumber("13123456789");
        assertThat(user.getPhoneNumber().get()).isEqualTo("13123456789");

        // When - 第二位是 9
        user.updatePhoneNumber("19123456789");
        assertThat(user.getPhoneNumber().get()).isEqualTo("19123456789");
    }
}
