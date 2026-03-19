package com.aieducenter.account.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.account.config.SaTokenTestConfig;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.domain.repository.UserRepository;
import com.aieducenter.tenant.infrastructure.persistence.SpringDataJpaTenantRepository;
import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.application.dto.VerifyCodeResult;

/**
 * 邮箱注册流程集成测试。
 *
 * <p>验证注册成功、重复邮箱、密码强度不足三个场景。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRegistrationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VerificationCodeAppService verificationCodeAppService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpringDataJpaTenantRepository tenantRepository;

    @BeforeAll
    static void setup() {
        SaTokenTestConfig.initSaTokenContext();
    }

    @Test
    @Transactional
    void given_valid_registration_when_register_by_email_then_return_token_and_create_user_and_tenant() throws Exception {
        // Given — verifyCode returns success (mock)
        when(verificationCodeAppService.verifyCode(any())).thenReturn(new VerifyCodeResult(true, "ok"));

        String requestBody = """
            {
                "username": "newuser123",
                "email": "newuser@example.com",
                "password": "password123",
                "nickname": "New User",
                "verificationCode": "123456"
            }
            """;

        // When
        mvc.perform(post("/api/account/register/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty());

        // Then — verify DB state
        assertThat(userRepository.existsByEmail("newuser@example.com")).isTrue();
        assertThat(tenantRepository.count()).isGreaterThan(0);
    }

    @Test
    @Transactional
    void given_existing_email_when_register_by_email_then_return_409() throws Exception {
        // Given — a user with that email already exists
        when(verificationCodeAppService.verifyCode(any())).thenReturn(new VerifyCodeResult(true, "ok"));

        User existingUser = User.registerByEmail("existing_user", "dup@example.com", "password123", null);
        userRepository.save(existingUser);

        String requestBody = """
            {
                "username": "another_user",
                "email": "dup@example.com",
                "password": "password123",
                "nickname": "Another",
                "verificationCode": "123456"
            }
            """;

        // When / Then
        mvc.perform(post("/api/account/register/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void given_weak_password_when_register_by_email_then_return_400() throws Exception {
        // Given — verifyCode returns success (mock)
        when(verificationCodeAppService.verifyCode(any())).thenReturn(new VerifyCodeResult(true, "ok"));

        String requestBody = """
            {
                "username": "weakuser1",
                "email": "weakuser@example.com",
                "password": "weakpass",
                "nickname": "Weak User",
                "verificationCode": "123456"
            }
            """;

        // When / Then
        mvc.perform(post("/api/account/register/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }
}
