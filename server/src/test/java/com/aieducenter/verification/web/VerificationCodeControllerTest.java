package com.aieducenter.verification.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.aieducenter.verification.application.dto.SendCodeResponse;
import com.aieducenter.verification.application.dto.VerifyCodeResult;
import com.aieducenter.verification.application.VerificationCodeApplicationService;

@WebMvcTest(VerificationCodeController.class)
class VerificationCodeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private VerificationCodeApplicationService service;

    @Test
    void given_valid_request_when_send_verification_code_then_return_success() throws Exception {
        // Given
        when(service.sendEmailVerificationCode(any(), eq("127.0.0.1")))
            .thenReturn(new SendCodeResponse(300, 60));

        // When & Then
        mvc.perform(post("/api/account/verification-code/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"purpose\":\"REGISTER\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.expireInSeconds").value(300))
            .andExpect(jsonPath("$.data.resentAfterSeconds").value(60));
    }

    @Test
    void given_valid_code_when_verify_code_then_return_verified() throws Exception {
        // Given
        when(service.verifyCode(any()))
            .thenReturn(new VerifyCodeResult(true, "验证码正确"));

        // When & Then
        mvc.perform(post("/api/account/verify-code")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"code\":\"123456\",\"purpose\":\"REGISTER\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.verified").value(true));
    }
}
