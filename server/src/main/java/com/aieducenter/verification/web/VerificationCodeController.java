package com.aieducenter.verification.web;

import com.aieducenter.verification.application.VerificationCodeApplicationService;
import com.aieducenter.verification.application.dto.SendCodeResponse;
import com.aieducenter.verification.application.dto.SendEmailCodeCommand;
import com.aieducenter.verification.application.dto.VerifyCodeCommand;
import com.aieducenter.verification.application.dto.VerifyCodeResult;
import com.cartisan.web.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器。
 *
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/account")
public class VerificationCodeController {

    private final VerificationCodeApplicationService service;

    public VerificationCodeController(VerificationCodeApplicationService service) {
        this.service = service;
    }

    /**
     * 发送邮箱验证码。
     *
     * @param command 命令
     * @param request HTTP请求
     * @return 响应
     */
    @PostMapping("/verification-code/email")
    public ApiResponse<SendCodeResponse> sendEmailVerificationCode(
            @RequestBody SendEmailCodeCommand command,
            HttpServletRequest request) {

        String ip = getClientIp(request);
        SendCodeResponse response = service.sendEmailVerificationCode(command, ip);
        return ApiResponse.ok(response);
    }

    /**
     * 校验验证码。
     *
     * @param command 命令
     * @return 响应
     */
    @PostMapping("/verify-code")
    public ApiResponse<VerifyCodeResult> verifyCode(@RequestBody VerifyCodeCommand command) {
        VerifyCodeResult result = service.verifyCode(command);
        return ApiResponse.ok(result);
    }

    /**
     * 获取客户端IP。
     *
     * <p>仅当请求来自受信任的反向代理（回环或私有 IP）时，才读取 X-Forwarded-For，
     * 防止攻击者伪造头绕过 IP 限流。
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (isTrustedProxy(remoteAddr)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isEmpty()) {
                // 取第一个 IP（最原始的客户端 IP）
                return forwarded.split(",")[0].trim();
            }
        }
        return remoteAddr != null ? remoteAddr : "127.0.0.1";
    }

    private boolean isTrustedProxy(String remoteAddr) {
        if (remoteAddr == null) return false;
        return remoteAddr.startsWith("127.")
            || remoteAddr.startsWith("10.")
            || remoteAddr.startsWith("192.168.")
            || remoteAddr.startsWith("172.")
            || remoteAddr.equals("0:0:0:0:0:0:0:1")
            || remoteAddr.equals("::1");
    }
}
