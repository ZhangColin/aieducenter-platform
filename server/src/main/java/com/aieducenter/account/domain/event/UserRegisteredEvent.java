package com.aieducenter.account.domain.event;

import java.time.Instant;

public record UserRegisteredEvent(
        Long userId,
        String username,
        String email,
        String nickname,
        Instant occurredAt
) {}
