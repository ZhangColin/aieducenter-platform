package com.aieducenter.admin.infrastructure;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;
import com.aieducenter.admin.domain.port.PasswordEncoderPort;

/**
 * BCrypt 密码编码器适配器。
 *
 * <p>使用 Spring Security 的 BCryptPasswordEncoder 实现密码编码端口。</p>
 *
 * @since 0.1.0
 */
@Component("adminBCryptPasswordEncoder")
@Adapter(PortType.CLIENT)
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private static final int STRENGTH = 10;
    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordEncoderAdapter() {
        this.encoder = new BCryptPasswordEncoder(STRENGTH);
    }

    @Override
    public String encode(String plainPassword) {
        return encoder.encode(plainPassword);
    }

    @Override
    public boolean matches(String plainPassword, String encodedPassword) {
        return encoder.matches(plainPassword, encodedPassword);
    }
}