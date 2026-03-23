package com.aieducenter.admin.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.aieducenter.admin.domain.error.AdminMessage;
import com.cartisan.core.exception.DomainException;

/**
 * AdminUser 聚合根测试。
 */
class AdminUserTest {

    @Test
    void given_valid_input_when_create_admin_then_success() {
        // When
        AdminUser adminUser = new AdminUser("testuser", "Test1234", "测试用户");

        // Then
        assertThat(adminUser.getUsername()).isEqualTo("testuser");
        assertThat(adminUser.getNickname()).isEqualTo("测试用户");
        assertThat(adminUser.getStatus()).isEqualTo(AdminUser.AdminStatus.ACTIVE);
        assertThat(adminUser.isSystem()).isFalse();
    }

    @Test
    void given_invalid_username_when_create_admin_then_throw_exception() {
        // When & Then
        assertThatThrownBy(() -> new AdminUser("invalid user", "Test1234", "测试用户"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(AdminMessage.USERNAME_INVALID.message());
    }

    @Test
    void given_weak_password_when_create_admin_then_throw_exception() {
        // When & Then
        assertThatThrownBy(() -> new AdminUser("testuser", "weak", "测试用户"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(AdminMessage.PASSWORD_WEAK.message());
    }

    @Test
    void given_correct_password_when_matches_password_then_true() {
        // Given
        String plainPassword = "Test1234";
        AdminUser adminUser = new AdminUser("testuser", plainPassword, "测试用户");

        // When & Then
        assertThat(adminUser.matchesPassword(plainPassword)).isTrue();
    }

    @Test
    void given_wrong_password_when_matches_password_then_false() {
        // Given
        AdminUser adminUser = new AdminUser("testuser", "Test1234", "测试用户");

        // When & Then
        assertThat(adminUser.matchesPassword("WrongPass123")).isFalse();
    }

    @Test
    void given_old_password_correct_when_update_password_then_success() {
        // Given
        AdminUser adminUser = new AdminUser("testuser", "Test1234", "测试用户");

        // When
        adminUser.updatePassword("Test1234", "NewPass567");

        // Then
        assertThat(adminUser.matchesPassword("NewPass567")).isTrue();
        assertThat(adminUser.matchesPassword("Test1234")).isFalse();
    }

    @Test
    void given_old_password_incorrect_when_update_password_then_throw_exception() {
        // Given
        AdminUser adminUser = new AdminUser("testuser", "Test1234", "测试用户");

        // When & Then
        assertThatThrownBy(() -> adminUser.updatePassword("WrongPass", "NewPass567"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining(AdminMessage.PASSWORD_INCORRECT.message());
    }

    @Test
    void given_active_admin_when_disable_then_status_disabled() {
        // Given
        AdminUser adminUser = new AdminUser("testuser", "Test1234", "测试用户");

        // When
        adminUser.disable();

        // Then
        assertThat(adminUser.getStatus()).isEqualTo(AdminUser.AdminStatus.DISABLED);
    }

    @Test
    void given_disabled_admin_when_enable_then_status_active() {
        // Given
        AdminUser adminUser = new AdminUser("testuser", "Test1234", "测试用户");
        adminUser.disable();

        // When
        adminUser.enable();

        // Then
        assertThat(adminUser.getStatus()).isEqualTo(AdminUser.AdminStatus.ACTIVE);
    }

    @Test
    void given_admin_when_reset_password_then_success() {
        // Given
        AdminUser adminUser = new AdminUser("testuser", "Test1234", "测试用户");

        // When
        adminUser.resetPassword("NewPass567");

        // Then
        assertThat(adminUser.matchesPassword("NewPass567")).isTrue();
        assertThat(adminUser.matchesPassword("Test1234")).isFalse();
    }
}
