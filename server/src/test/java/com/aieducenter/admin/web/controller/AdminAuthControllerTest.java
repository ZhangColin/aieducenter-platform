package com.aieducenter.admin.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.aieducenter.admin.application.AdminUserAuthAppService;
import com.aieducenter.admin.application.dto.command.AdminUserLoginCommand;
import com.aieducenter.admin.application.dto.command.UpdatePasswordCommand;
import com.cartisan.security.authentication.AuthenticationService;
import com.cartisan.security.authentication.TokenInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AdminAuthController 测试。
 *
 * <p>测试管理员认证相关的 HTTP 端点。
 *
 * <p>注意：由于 @WebMvcTest 不加载完整的 Spring 上下文，
 * Sa-Token 拦截器和 @CurrentUser 注解解析器不会生效。
 * 因此，需要 @RequireAuth 认证的端点测试需要在完整的集成测试中进行。
 */
@WebMvcTest(AdminAuthController.class)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserAuthAppService adminAuthAppService;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void given_valid_credentials_when_login_then_return_token_info() throws Exception {
        // Given
        TokenInfo tokenInfo = new TokenInfo("test-token", 1L, Instant.now().plusSeconds(86400));
        when(adminAuthAppService.login(any(AdminUserLoginCommand.class))).thenReturn(tokenInfo);

        AdminUserLoginCommand command = new AdminUserLoginCommand("admin", "Test1234", false);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test-token"));
    }

    @Test
    void given_valid_credentials_with_remember_me_when_login_then_return_token_info() throws Exception {
        // Given
        TokenInfo tokenInfo = new TokenInfo("remember-token", 1L, Instant.now().plusSeconds(604800));
        when(adminAuthAppService.login(any(AdminUserLoginCommand.class))).thenReturn(tokenInfo);

        AdminUserLoginCommand command = new AdminUserLoginCommand("admin", "Test1234", true);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("remember-token"));
    }

    @Test
    void given_invalid_username_when_login_then_return_error() throws Exception {
        // Given - missing username
        AdminUserLoginCommand command = new AdminUserLoginCommand("", "Test1234", false);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void given_invalid_password_when_login_then_return_error() throws Exception {
        // Given - missing password
        AdminUserLoginCommand command = new AdminUserLoginCommand("admin", "", false);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void given_authenticated_user_when_logout_then_success() throws Exception {
        // When & Then - logout endpoint doesn't require authentication in the controller layer
        mockMvc.perform(post("/api/v1/admin/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminAuthAppService).logout();
    }

    @Test
    void given_invalid_old_password_when_updatePassword_then_return_error() throws Exception {
        // Given - missing old password
        UpdatePasswordCommand command = new UpdatePasswordCommand("", "NewPass56");

        // When & Then - validation happens before authentication check
        mockMvc.perform(put("/api/v1/admin/auth/current/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void given_invalid_new_password_when_updatePassword_then_return_error() throws Exception {
        // Given - weak new password
        UpdatePasswordCommand command = new UpdatePasswordCommand("Test1234", "weak");

        // When & Then - validation happens before authentication check
        mockMvc.perform(put("/api/v1/admin/auth/current/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }
}
