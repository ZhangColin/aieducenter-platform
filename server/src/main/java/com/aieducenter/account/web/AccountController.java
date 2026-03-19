package com.aieducenter.account.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aieducenter.account.application.AccountLoginAppService;
import com.aieducenter.account.application.AccountPasswordResetAppService;
import com.aieducenter.account.application.AccountRegistrationAppService;
import com.aieducenter.account.application.dto.LoginByPasswordCommand;
import com.aieducenter.account.application.dto.LoginBySmsCommand;
import com.aieducenter.account.application.dto.LoginResult;
import com.aieducenter.account.application.dto.RegisterCommand;
import com.aieducenter.account.application.dto.RegisterResult;
import com.aieducenter.account.application.dto.ResetPasswordCommand;
import com.cartisan.web.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountRegistrationAppService registrationAppService;
    private final AccountLoginAppService loginAppService;
    private final AccountPasswordResetAppService passwordResetAppService;

    public AccountController(AccountRegistrationAppService registrationAppService,
            AccountLoginAppService loginAppService,
            AccountPasswordResetAppService passwordResetAppService) {
        this.registrationAppService = registrationAppService;
        this.loginAppService = loginAppService;
        this.passwordResetAppService = passwordResetAppService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResult> register(@Valid @RequestBody RegisterCommand command) {
        return ApiResponse.ok(registrationAppService.register(command));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> loginByPassword(@Valid @RequestBody LoginByPasswordCommand command) {
        return ApiResponse.ok(loginAppService.loginByPassword(command));
    }

    @PostMapping("/login/sms")
    public ApiResponse<LoginResult> loginBySms(@Valid @RequestBody LoginBySmsCommand command) {
        return ApiResponse.ok(loginAppService.loginBySms(command));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordCommand command) {
        passwordResetAppService.resetPassword(command);
        return ApiResponse.ok(null);
    }
}
