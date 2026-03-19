package com.aieducenter.account.web;

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

import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.application.dto.VerifyCodeResult;
import com.aieducenter.account.config.SaTokenTestConfig;

/**
 * 账号登录集成测试。
 *
 * <p>验证密码登录（用户名/邮箱/手机号）和短信验证码登录场景。</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountLoginIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VerificationCodeAppService verificationCodeAppService;

    @BeforeAll
    static void setup() {
        SaTokenTestConfig.initSaTokenContext();
    }

    // ── 密码登录 ──────────────────────────────────────────────────────────────

    @Test
    @Transactional
    void given_username_and_password_when_login_by_password_then_return_token() throws Exception {
        registerUser("loginuser1", "Password1", null, null);

        String loginBody = """
            {
                "account": "loginuser1",
                "password": "Password1"
            }
            """;

        mvc.perform(post("/api/account/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @Transactional
    void given_email_and_password_when_login_by_password_then_return_token() throws Exception {
        registerUser("emailuser1", "Password1", "emailuser@example.com", null);

        String loginBody = """
            {
                "account": "emailuser@example.com",
                "password": "Password1"
            }
            """;

        mvc.perform(post("/api/account/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @Transactional
    void given_phone_and_password_when_login_by_password_then_return_token() throws Exception {
        registerUser("phoneuser1", "Password1", null, "13800138001");

        String loginBody = """
            {
                "account": "13800138001",
                "password": "Password1"
            }
            """;

        mvc.perform(post("/api/account/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @Transactional
    void given_unknown_account_when_login_by_password_then_return_401() throws Exception {
        String loginBody = """
            {
                "account": "nosuchuser",
                "password": "Password1"
            }
            """;

        mvc.perform(post("/api/account/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
            .andExpect(status().isUnauthorized());
    }

    // ── 短信验证码登录 ────────────────────────────────────────────────────────

    @Test
    @Transactional
    void given_valid_sms_code_when_login_by_sms_then_return_token() throws Exception {
        registerUser("smsuser1", "Password1", null, "13900139001");

        when(verificationCodeAppService.verifyPhoneCode(any()))
            .thenReturn(new VerifyCodeResult(true, "OK"));

        String smsBody = """
            {
                "phone": "13900139001",
                "code": "123456"
            }
            """;

        mvc.perform(post("/api/account/login/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(smsBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @Transactional
    void given_unregistered_phone_when_login_by_sms_then_return_401() throws Exception {
        when(verificationCodeAppService.verifyPhoneCode(any()))
            .thenReturn(new VerifyCodeResult(true, "OK"));

        String smsBody = """
            {
                "phone": "13700137001",
                "code": "123456"
            }
            """;

        mvc.perform(post("/api/account/login/sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(smsBody))
            .andExpect(status().isUnauthorized());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void registerUser(String username, String password, String email, String phone) throws Exception {
        StringBuilder body = new StringBuilder();
        body.append("{");
        body.append("\"username\":\"").append(username).append("\",");
        body.append("\"password\":\"").append(password).append("\"");
        if (email != null) body.append(",\"email\":\"").append(email).append("\"");
        if (phone != null) body.append(",\"phone\":\"").append(phone).append("\"");
        body.append("}");

        mvc.perform(post("/api/account/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.toString()))
            .andExpect(status().isOk());
    }
}
