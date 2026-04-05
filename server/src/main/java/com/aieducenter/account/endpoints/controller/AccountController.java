package com.aieducenter.account.endpoints.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aieducenter.account.application.AccountLoginAppService;
import com.aieducenter.account.application.AccountPasswordResetAppService;
import com.aieducenter.account.application.AccountQueryAppService;
import com.aieducenter.account.application.AccountRegistrationAppService;
import com.aieducenter.account.application.dto.command.LoginByPasswordCommand;
import com.aieducenter.account.application.dto.command.LoginBySmsCommand;
import com.aieducenter.account.application.dto.command.RegisterCommand;
import com.aieducenter.account.application.dto.command.ResetPasswordCommand;
import com.aieducenter.account.application.dto.response.LoginResult;
import com.aieducenter.account.application.dto.response.RegisterResult;
import com.cartisan.web.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/account")
@Validated
@Tag(name = "Account", description = "账号管理")
public class AccountController {

    private final AccountRegistrationAppService registrationAppService;
    private final AccountLoginAppService loginAppService;
    private final AccountPasswordResetAppService passwordResetAppService;
    private final AccountQueryAppService queryAppService;

    public AccountController(AccountRegistrationAppService registrationAppService,
            AccountLoginAppService loginAppService,
            AccountPasswordResetAppService passwordResetAppService,
            AccountQueryAppService queryAppService) {
        this.registrationAppService = registrationAppService;
        this.loginAppService = loginAppService;
        this.passwordResetAppService = passwordResetAppService;
        this.queryAppService = queryAppService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "通过用户名+密码或手机号+验证码注册新用户")
    public ApiResponse<RegisterResult> register(@Valid @RequestBody RegisterCommand command) {
        return ApiResponse.ok(registrationAppService.register(command));
    }

    @PostMapping("/login")
    @Operation(summary = "密码登录", description = "通过用户名/邮箱/手机号+密码登录")
    public ApiResponse<LoginResult> loginByPassword(@Valid @RequestBody LoginByPasswordCommand command) {
        return ApiResponse.ok(loginAppService.loginByPassword(command));
    }

    @PostMapping("/login/sms")
    @Operation(summary = "短信验证码登录", description = "通过手机号+短信验证码登录")
    public ApiResponse<LoginResult> loginBySms(@Valid @RequestBody LoginBySmsCommand command) {
        return ApiResponse.ok(loginAppService.loginBySms(command));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "清除服务端会话状态")
    public ApiResponse<Void> logout() {
        loginAppService.logout();
        return ApiResponse.ok(null);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过手机号验证码重置密码")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordCommand command) {
        passwordResetAppService.resetPassword(command);
        return ApiResponse.ok(null);
    }

    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否可用", description = "用于注册表单的实时用户名可用性验证")
    public ApiResponse<Boolean> checkUsername(
            @RequestParam
            @NotBlank
            @Size(min = 3, max = 20)
            String username) {
        boolean available = queryAppService.isUsernameAvailable(username);
        return ApiResponse.ok(available);
    }

    @GetMapping("/check-phone")
    @Operation(summary = "检查手机号是否可用", description = "用于注册表单的实时手机号可用性验证")
    public ApiResponse<Boolean> checkPhone(
            @RequestParam
            @NotBlank
            @Pattern(regexp = "^1[3-9]\\d{9}$")
            String phone) {
        boolean available = queryAppService.isPhoneNumberAvailable(phone);
        return ApiResponse.ok(available);
    }
}
