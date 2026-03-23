package com.aieducenter.admin.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.aieducenter.admin.application.AdminUserManagementAppService;
import com.aieducenter.admin.application.dto.command.CreateAdminCommand;
import com.aieducenter.admin.application.dto.command.ResetPasswordCommand;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AdminUserController 测试。
 *
 * <p>测试管理员管理相关的 HTTP 端点。
 *
 * <p>注意：由于 @WebMvcTest 不加载完整的 Spring 上下文，
 * Sa-Token 拦截器和 @RequireAuth 注解不会生效。
 * 因此，需要认证的端点测试需要在完整的集成测试中进行。
 */
@WebMvcTest(AdminUserController.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserManagementAppService adminUserManagementAppService;

    @Test
    void given_valid_input_when_createAdminUser_then_return_id() throws Exception {
        // Given
        CreateAdminCommand command = new CreateAdminCommand(
            "testuser", "Test1234", "测试用户", "test@example.com", null
        );
        when(adminUserManagementAppService.create(any(CreateAdminCommand.class))).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void given_invalid_username_when_createAdminUser_then_return_error() throws Exception {
        // Given - invalid username (starts with number)
        CreateAdminCommand command = new CreateAdminCommand(
            "123user", "Test1234", "测试用户", "test@example.com", null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void given_invalid_password_when_createAdminUser_then_return_error() throws Exception {
        // Given - weak password
        CreateAdminCommand command = new CreateAdminCommand(
            "testuser", "weak", "测试用户", "test@example.com", null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void given_existing_user_when_delete_then_success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminUserManagementAppService).delete(1L);
    }

    @Test
    void given_valid_input_when_resetPassword_then_success() throws Exception {
        // Given
        ResetPasswordCommand command = new ResetPasswordCommand("NewPass56");

        // When & Then
        mockMvc.perform(put("/api/v1/admin/users/1/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminUserManagementAppService).resetPassword(1L, "NewPass56");
    }

    @Test
    void given_invalid_password_when_resetPassword_then_return_error() throws Exception {
        // Given - weak password
        ResetPasswordCommand command = new ResetPasswordCommand("weak");

        // When & Then
        mockMvc.perform(put("/api/v1/admin/users/1/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }
}
