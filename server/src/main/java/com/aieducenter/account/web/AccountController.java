package com.aieducenter.account.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.aieducenter.account.domain.repository.UserRepository;
import com.cartisan.web.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountRegistrationAppService registrationAppService;
    private final AccountLoginAppService loginAppService;
    private final AccountPasswordResetAppService passwordResetAppService;
    private final UserRepository userRepository;

    public AccountController(AccountRegistrationAppService registrationAppService,
            AccountLoginAppService loginAppService,
            AccountPasswordResetAppService passwordResetAppService,
            UserRepository userRepository) {
        this.registrationAppService = registrationAppService;
        this.loginAppService = loginAppService;
        this.passwordResetAppService = passwordResetAppService;
        this.userRepository = userRepository;
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

    @GetMapping("/check-username")
    public ApiResponse<Boolean> checkUsername(
            @RequestParam
            @NotBlank
            @Size(min = 3, max = 20)
            String username) {
        boolean available = !userRepository.existsByUsername(username);
        return ApiResponse.ok(available);
    }

    @GetMapping("/check-phone")
    public ApiResponse<Boolean> checkPhone(
            @RequestParam
            @NotBlank
            @Pattern(regexp = "^1[3-9]\\d{9}$")
            String phone) {
        boolean available = !userRepository.existsByPhoneNumber(phone);
        return ApiResponse.ok(available);
    }
}
