package com.aieducenter.account.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterByEmailCommand(
    @NotBlank String username,
    @NotBlank String email,
    @NotBlank String password,
    @Size(max = 50) String nickname,
    @NotBlank String verificationCode
) {}
