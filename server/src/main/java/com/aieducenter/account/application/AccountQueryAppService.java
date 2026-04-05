package com.aieducenter.account.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.account.domain.repository.UserRepository;

/**
 * 账号查询应用服务。
 *
 * <p>提供用户名、手机号可用性查询等只读操作。</p>
 *
 * @since 0.1.0
 */
@Service
public class AccountQueryAppService {

    private final UserRepository userRepository;

    public AccountQueryAppService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 检查用户名是否可用。
     *
     * @param username 用户名
     * @return true 可用，false 已被占用
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * 检查手机号是否可用。
     *
     * @param phone 手机号
     * @return true 可用，false 已被占用
     */
    @Transactional(readOnly = true)
    public boolean isPhoneNumberAvailable(String phone) {
        return !userRepository.existsByPhoneNumber(phone);
    }
}
