package com.aieducenter.account.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.aieducenter.account.domain.error.UserError;
import com.aieducenter.account.domain.valueobject.Email;
import com.aieducenter.account.domain.valueobject.PhoneNumber;
import com.aieducenter.account.domain.valueobject.Username;
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
}
