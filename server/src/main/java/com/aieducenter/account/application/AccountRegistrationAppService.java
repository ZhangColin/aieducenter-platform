package com.aieducenter.account.application;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.account.application.dto.RegisterCommand;
import com.aieducenter.account.application.dto.RegisterResult;
import com.aieducenter.account.domain.aggregate.User;
import com.aieducenter.account.domain.error.UserError;
import com.aieducenter.account.domain.event.UserRegisteredEvent;
import com.aieducenter.account.domain.repository.UserRepository;
import com.cartisan.core.exception.DomainException;
import com.cartisan.security.authentication.AuthenticationService;

@Service
public class AccountRegistrationAppService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountRegistrationAppService(
            UserRepository userRepository,
            AuthenticationService authenticationService,
            ApplicationEventPublisher applicationEventPublisher) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public RegisterResult register(RegisterCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new DomainException(UserError.USERNAME_ALREADY_EXISTS);
        }

        if (command.email() != null && !command.email().isBlank()) {
            if (userRepository.existsByEmail(command.email())) {
                throw new DomainException(UserError.EMAIL_ALREADY_EXISTS);
            }
        }

        if (command.phone() != null && !command.phone().isBlank()) {
            if (userRepository.existsByPhoneNumber(command.phone())) {
                throw new DomainException(UserError.PHONE_NUMBER_ALREADY_EXISTS);
            }
        }

        User user = User.register(command.username(), command.password(), command.nickname(), command.email(), command.phone());
        userRepository.save(user);

        var tokenInfo = authenticationService.login(user.getId());

        applicationEventPublisher.publishEvent(new UserRegisteredEvent(
            user.getId(), user.getUsername(), user.getEmail().orElse(null), user.getNickname(), Instant.now()));

        return new RegisterResult(tokenInfo.token());
    }
}
