package com.aieducenter.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.admin.application.dto.command.CreateAdminUserCommand;
import com.aieducenter.admin.domain.aggregate.AdminUser;
import com.aieducenter.admin.domain.error.AdminMessage;
import com.aieducenter.admin.domain.repository.AdminRoleRepository;
import com.aieducenter.admin.domain.repository.AdminUserRepository;
import com.aieducenter.admin.application.mapper.AdminUserMapper;
import com.cartisan.core.exception.ApplicationException;
import com.cartisan.core.exception.DomainException;

@ExtendWith(MockitoExtension.class)
class AdminUserManagementAppServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private AdminRoleRepository adminRoleRepository;

    @Mock
    private AdminUserAuthAppService adminUserAuthAppService;

    @Mock
    private AdminUserMapper adminUserMapper;

    private AdminUserManagementAppService adminUserManagementAppService;

    @BeforeEach
    void setUp() {
        adminUserManagementAppService = new AdminUserManagementAppService(
            adminUserRepository,
            adminRoleRepository,
            adminUserAuthAppService,
            adminUserMapper
        );
    }

    @Test
    void given_valid_input_when_createAdminUser_then_success() {
        // Given
        CreateAdminUserCommand command = new CreateAdminUserCommand(
            "testuser", "Test1234", "测试用户", "test@example.com", null
        );
        when(adminUserRepository.existsByUsername("testuser")).thenReturn(false);
        when(adminUserRepository.save(any(AdminUser.class)))
            .thenAnswer(invocation -> {
                AdminUser user = invocation.getArgument(0);
                // Simulate JPA @PrePersist behavior by setting ID via reflection on the actual saved entity
                java.lang.reflect.Field idField = AdminUser.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, 1L);
                return user;
            });

        // When
        Long id = adminUserManagementAppService.create(command);

        // Then
        assertThat(id).isNotNull();
        assertThat(id).isEqualTo(1L);
    }

    @Test
    void given_duplicate_username_when_createAdminUser_then_throw_exception() {
        // Given
        CreateAdminUserCommand command = new CreateAdminUserCommand(
            "testuser", "Test1234", "测试用户", null, null
        );
        when(adminUserRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> adminUserManagementAppService.create(command))
            .isInstanceOf(DomainException.class)
            .hasMessageContaining(AdminMessage.USERNAME_ALREADY_EXISTS.message());
    }

    @Test
    void given_only_one_admin_when_delete_then_throw_exception() {
        // Given
        when(adminUserRepository.count()).thenReturn(1L);

        // When & Then
        assertThatThrownBy(() -> adminUserManagementAppService.delete(1L))
            .isInstanceOf(ApplicationException.class)
            .hasMessageContaining(AdminMessage.LAST_ADMIN_CANNOT_DELETE.message());
    }

    @Test
    void given_multiple_admins_when_delete_then_success() {
        // Given
        when(adminUserRepository.count()).thenReturn(2L);
        AdminUser adminUser = new AdminUser("testuser", "Test1234", "测试用户");
        when(adminUserRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        // When
        adminUserManagementAppService.delete(1L);

        // Then
        verify(adminUserRepository).delete(adminUser);
    }
}
