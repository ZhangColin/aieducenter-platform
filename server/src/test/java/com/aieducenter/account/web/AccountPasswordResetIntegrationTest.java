package com.aieducenter.account.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.application.dto.VerifyCodeCommand;
import com.aieducenter.verification.application.dto.VerifyCodeResult;
import com.aieducenter.verification.application.dto.VerifySmsCodeCommand;
import com.aieducenter.verification.domain.error.VerificationCodeError;
import com.cartisan.core.exception.DomainException;

/**
 * 密码重置 Controller 集成测试。
 *
 * <p>验证 POST /api/account/reset-password 的各场景。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountPasswordResetIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VerificationCodeAppService verificationCodeAppService;

    @BeforeAll
    static void setup() {
        SaTokenTestConfig.initSaTokenContext();
    }

    // ── Scenario 1: Email reset success — new password works ─────────────────

    @Test
    @Transactional
    void given_email_account_when_reset_password_then_new_password_works() throws Exception {
        registerUser("resetuser1", "OldPass123", "test@example.com", null);

        when(verificationCodeAppService.verifyCode(any(VerifyCodeCommand.class)))
            .thenReturn(new VerifyCodeResult(true, "OK"));

        String resetBody = """
            {
                "account": "test@example.com",
                "verificationCode": "123456",
                "newPassword": "NewPass456"
            }
            """;

        mvc.perform(post("/api/account/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetBody))
            .andExpect(status().isOk());

        mvc.perform(post("/api/account/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "account": "test@example.com",
                        "password": "NewPass456"
                    }
                    """))
            .andExpect(status().isOk());
    }

    // ── Scenario 2: Phone reset success ──────────────────────────────────────

    @Test
    @Transactional
    void given_phone_account_when_reset_password_then_new_password_works() throws Exception {
        registerUser("resetuser2", "OldPass123", null, "13800138000");

        when(verificationCodeAppService.verifyPhoneCode(any(VerifySmsCodeCommand.class)))
            .thenReturn(new VerifyCodeResult(true, "OK"));

        String resetBody = """
            {
                "account": "13800138000",
                "verificationCode": "123456",
                "newPassword": "NewPass456"
            }
            """;

        mvc.perform(post("/api/account/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetBody))
            .andExpect(status().isOk());

        mvc.perform(post("/api/account/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "account": "13800138000",
                        "password": "NewPass456"
                    }
                    """))
            .andExpect(status().isOk());
    }

    // ── Scenario 3: Invalid verification code returns 400 ────────────────────

    @Test
    @Transactional
    void given_invalid_verification_code_when_reset_password_then_return_400() throws Exception {
        doThrow(new DomainException(VerificationCodeError.CODE_INVALID))
            .when(verificationCodeAppService).verifyCode(any(VerifyCodeCommand.class));

        String resetBody = """
            {
                "account": "any@example.com",
                "verificationCode": "000000",
                "newPassword": "NewPass456"
            }
            """;

        mvc.perform(post("/api/account/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetBody))
            .andExpect(status().isBadRequest());
    }

    // ── Scenario 4: Non-existent account returns 400 ─────────────────────────

    @Test
    @Transactional
    void given_nonexistent_account_when_reset_password_then_return_400() throws Exception {
        when(verificationCodeAppService.verifyCode(any(VerifyCodeCommand.class)))
            .thenReturn(new VerifyCodeResult(true, "OK"));

        String resetBody = """
            {
                "account": "nonexistent@example.com",
                "verificationCode": "123456",
                "newPassword": "NewPass456"
            }
            """;

        mvc.perform(post("/api/account/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(resetBody))
            .andExpect(status().isBadRequest());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void registerUser(String username, String password, String email, String phone) throws Exception {
        String emailField = email != null ? ",\n                \"email\": \"" + email + "\"" : "";
        String phoneField = phone != null ? ",\n                \"phone\": \"" + phone + "\"" : "";
        String body = """
            {
                "username": "%s",
                "password": "%s"%s%s
            }
            """.formatted(username, password, emailField, phoneField);

        mvc.perform(post("/api/account/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());
    }
}
