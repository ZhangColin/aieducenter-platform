package com.aieducenter.account.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aieducenter.account.application.AccountRegistrationAppService;
import com.aieducenter.account.application.dto.RegisterByEmailCommand;
import com.aieducenter.account.application.dto.RegisterResult;
import com.cartisan.web.response.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountRegistrationAppService registrationAppService;

    public AccountController(AccountRegistrationAppService registrationAppService) {
        this.registrationAppService = registrationAppService;
    }

    @PostMapping("/register/email")
    public ApiResponse<RegisterResult> registerByEmail(@Valid @RequestBody RegisterByEmailCommand command) {
        return ApiResponse.ok(registrationAppService.registerByEmail(command));
    }
}
