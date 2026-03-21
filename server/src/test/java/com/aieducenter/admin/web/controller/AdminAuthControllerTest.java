package com.aieducenter.admin.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.aieducenter.admin.application.AdminAuthAppService;
import com.aieducenter.admin.application.dto.command.AdminLoginCommand;
import com.aieducenter.admin.application.dto.query.AdminDto;
import com.aieducenter.admin.application.dto.query.LoginResult;
import com.aieducenter.admin.application.dto.query.RoleDto;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AdminAuthController 测试。
 */
@WebMvcTest(AdminAuthController.class)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminAuthAppService adminAuthAppService;

    private LoginResult loginResult;

    @BeforeEach
    void setUp() {
        AdminDto adminDto = new AdminDto(
                1L,
                "testadmin",
                "测试管理员",
                null,
                null,
                null,
                "ACTIVE",
                false,
                null,
                null,
                List.of()
        );

        loginResult = new LoginResult(
                "test-token",
                Instant.now().plusSeconds(28800),
                adminDto,
                List.of(new RoleDto(null, null, "ADMIN", null, null, null, null)),
                List.of(),
                List.of("admin:user:read")
        );
    }

    @Test
    void given_valid_credentials_when_login_then_return_token() throws Exception {
        // Given
        when(adminAuthAppService.login(any(AdminLoginCommand.class))).thenReturn(loginResult);

        String loginJson = """
                {
                    "username": "testadmin",
                    "password": "Test1234",
                    "rememberMe": false
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("test-token"))
                .andExpect(jsonPath("$.data.admin.username").value("testadmin"));
    }

    @Test
    void given_invalid_json_when_login_then_return_bad_request() throws Exception {
        // Given
        String invalidJson = """
                {
                    "username": "testadmin"
                }
                """;

        // When & Then
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void given_authenticated_user_when_logout_then_success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/admin/auth/logout"))
                .andExpect(status().isOk());

        verify(adminAuthAppService).logout();
    }
}
