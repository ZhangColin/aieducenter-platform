package com.aieducenter.verification.web;

import com.aieducenter.verification.application.CaptchaAppService;
import com.aieducenter.verification.application.dto.CreateCaptchaResponse;
import com.cartisan.web.response.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图形验证码控制器。
 */
@RestController
@RequestMapping("/api")
public class CaptchaController {

    private final CaptchaAppService captchaAppService;

    public CaptchaController(CaptchaAppService captchaAppService) {
        this.captchaAppService = captchaAppService;
    }

    /**
     * 获取图形验证码。
     *
     * @return 验证码响应（图片 base64 和 ID）
     */
    @GetMapping("/captcha")
    public ApiResponse<CreateCaptchaResponse> getCaptcha() {
        return ApiResponse.ok(captchaAppService.createCaptcha());
    }
}
