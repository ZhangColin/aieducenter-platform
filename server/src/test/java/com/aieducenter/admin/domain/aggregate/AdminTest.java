package com.aieducenter.admin.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.aieducenter.admin.domain.error.AdminError;
import com.cartisan.core.exception.DomainException;

/**
 * Admin 聚合根测试。
 */
class AdminTest {

    @Test
    void given_valid_input_when_create_admin_then_success() {
        // When
        Admin admin = new Admin("testuser", "Test1234", "测试用户");

        // Then
        assertThat(admin.getUsername()).isEqualTo("testuser");
        assertThat(admin.getNickname()).isEqualTo("测试用户");
        assertThat(admin.getStatus()).isEqualTo(Admin.AdminStatus.ACTIVE);
        assertThat(admin.isSystem()).isFalse();
    }

    @Test
    void given_invalid_username_when_create_admin_then_throw_exception() {
        // When & Then
        assertThatThrownBy(() -> new Admin("invalid user", "Test1234", "测试用户"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(AdminError.USERNAME_INVALID.message());
    }

    @Test
    void given_weak_password_when_create_admin_then_throw_exception() {
        // When & Then
        assertThatThrownBy(() -> new Admin("testuser", "weak", "测试用户"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(AdminError.PASSWORD_WEAK.message());
    }

    @Test
    void given_correct_password_when_matches_password_then_true() {
        // Given
        String plainPassword = "Test1234";
        Admin admin = new Admin("testuser", plainPassword, "测试用户");

        // When & Then
        assertThat(admin.matchesPassword(plainPassword)).isTrue();
    }

    @Test
    void given_wrong_password_when_matches_password_then_false() {
        // Given
        Admin admin = new Admin("testuser", "Test1234", "测试用户");

        // When & Then
        assertThat(admin.matchesPassword("WrongPass123")).isFalse();
    }

    @Test
    void given_old_password_correct_when_update_password_then_success() {
        // Given
        Admin admin = new Admin("testuser", "Test1234", "测试用户");

        // When
        admin.updatePassword("Test1234", "NewPass567");

        // Then
        assertThat(admin.matchesPassword("NewPass567")).isTrue();
        assertThat(admin.matchesPassword("Test1234")).isFalse();
    }

    @Test
    void given_old_password_incorrect_when_update_password_then_throw_exception() {
        // Given
        Admin admin = new Admin("testuser", "Test1234", "测试用户");

        // When & Then
        assertThatThrownBy(() -> admin.updatePassword("WrongPass", "NewPass567"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(AdminError.PASSWORD_INCORRECT.message());
    }

    @Test
    void given_active_admin_when_disable_then_status_disabled() {
        // Given
        Admin admin = new Admin("testuser", "Test1234", "测试用户");

        // When
        admin.disable();

        // Then
        assertThat(admin.getStatus()).isEqualTo(Admin.AdminStatus.DISABLED);
    }

    @Test
    void given_disabled_admin_when_enable_then_status_active() {
        // Given
        Admin admin = new Admin("testuser", "Test1234", "测试用户");
        admin.disable();

        // When
        admin.enable();

        // Then
        assertThat(admin.getStatus()).isEqualTo(Admin.AdminStatus.ACTIVE);
    }

    @Test
    void given_admin_when_reset_password_then_success() {
        // Given
        Admin admin = new Admin("testuser", "Test1234", "测试用户");

        // When
        admin.resetPassword("NewPass567");

        // Then
        assertThat(admin.matchesPassword("NewPass567")).isTrue();
        assertThat(admin.matchesPassword("Test1234")).isFalse();
    }
}
