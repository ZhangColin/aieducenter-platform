package com.aieducenter.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.admin.application.dto.command.AdminLoginCommand;
import com.aieducenter.admin.domain.aggregate.Admin;
import com.aieducenter.admin.domain.error.AdminError;
import com.aieducenter.admin.domain.repository.AdminUserRepository;
import com.aieducenter.admin.domain.service.AdminPermissionService;
import com.cartisan.core.exception.ApplicationException;

import cn.dev33.satoken.stp.StpLogic;

/**
 * AdminAuthAppService 测试。
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthAppServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AdminPermissionService adminPermissionService;

    @Mock
    private StpLogic adminStpLogic;

    @InjectMocks
    private AdminAuthAppService adminAuthAppService;

    @Test
    void given_valid_credentials_when_login_then_success() {
        // Given
        Admin admin = new Admin("testadmin", "Test1234", "测试管理员");

        when(adminUserRepository.findByUsername("testadmin")).thenReturn(Optional.of(admin));
        when(adminStpLogic.getTokenValue()).thenReturn("test-token");

        AdminLoginCommand command = new AdminLoginCommand("testadmin", "Test1234", false);

        // When
        var result = adminAuthAppService.login(command);

        // Then
        assertThat(result.token()).isEqualTo("test-token");
        assertThat(result.admin().username()).isEqualTo("testadmin");
        verify(adminStpLogic).login(any(), eq(28800L));
    }

    @Test
    void given_wrong_password_when_login_then_throw_exception() {
        // Given
        Admin admin = new Admin("testadmin", "Test1234", "测试管理员");

        when(adminUserRepository.findByUsername("testadmin")).thenReturn(Optional.of(admin));

        AdminLoginCommand command = new AdminLoginCommand("testadmin", "WrongPass", false);

        // When & Then
        assertThatThrownBy(() -> adminAuthAppService.login(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(AdminError.LOGIN_FAILED.message());
    }

    @Test
    void given_nonexistent_user_when_login_then_throw_exception() {
        // Given
        when(adminUserRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        AdminLoginCommand command = new AdminLoginCommand("nonexistent", "Test1234", false);

        // When & Then
        assertThatThrownBy(() -> adminAuthAppService.login(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(AdminError.LOGIN_FAILED.message());
    }

    @Test
    void given_disabled_admin_when_login_then_throw_exception() {
        // Given
        Admin admin = new Admin("testadmin", "Test1234", "测试管理员");
        admin.disable();

        when(adminUserRepository.findByUsername("testadmin")).thenReturn(Optional.of(admin));

        AdminLoginCommand command = new AdminLoginCommand("testadmin", "Test1234", false);

        // When & Then
        assertThatThrownBy(() -> adminAuthAppService.login(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(AdminError.ADMIN_DISABLED.message());
    }

    @Test
    void given_remember_me_true_when_login_then_timeout_extended() {
        // Given
        Admin admin = new Admin("testadmin", "Test1234", "测试管理员");

        when(adminUserRepository.findByUsername("testadmin")).thenReturn(Optional.of(admin));
        when(adminStpLogic.getTokenValue()).thenReturn("test-token");

        AdminLoginCommand command = new AdminLoginCommand("testadmin", "Test1234", true);

        // When
        adminAuthAppService.login(command);

        // Then
        verify(adminStpLogic).login(any(), eq(604800L));
    }

    @Test
    void given_login_when_logout_then_call_stp_logout() {
        // When
        adminAuthAppService.logout();

        // Then
        verify(adminStpLogic).logout();
    }
}
