package com.aieducenter.account.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterCommand(
    @NotBlank @Size(min = 3, max = 20) @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$") String username,
    @NotBlank @Size(min = 8, max = 20) @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$") String password,
    @Size(max = 50) String nickname,
    @Email String email,
    @Pattern(regexp = "^1[3-9]\\d{9}$") String phone
) {}
