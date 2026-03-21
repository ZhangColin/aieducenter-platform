package com.aieducenter.verification.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.aieducenter.verification.application.CaptchaAppService;
import com.aieducenter.verification.application.dto.CreateCaptchaResponse;

@WebMvcTest(CaptchaController.class)
class CaptchaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CaptchaAppService captchaAppService;

    @Test
    void given_validCaptcha_when_getCaptcha_then_returnResponse() throws Exception {
        // Given
        String expectedImage = "data:image/png;base64,iVBORw0KGgo...";
        String expectedId = "uuid-123";
        when(captchaAppService.createCaptcha())
            .thenReturn(new CreateCaptchaResponse(expectedImage, expectedId));

        // When/Then
        mvc.perform(get("/api/captcha"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.image").value(expectedImage))
            .andExpect(jsonPath("$.data.captchaId").value(expectedId));
    }
}
