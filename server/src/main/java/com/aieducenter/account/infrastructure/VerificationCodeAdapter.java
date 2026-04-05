package com.aieducenter.account.infrastructure;

import com.aieducenter.verification.application.VerificationCodeAppService;
import com.aieducenter.verification.application.dto.VerifyCodeCommand;
import com.aieducenter.verification.application.dto.VerifySmsCodeCommand;
import com.aieducenter.account.domain.port.VerificationCodePort;
import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;

/**
 * 验证码适配器。
 *
 * <p>适配 verification 上下文的 VerificationCodeAppService，实现验证码验证端口。</p>
 *
 * @since 0.1.0
 */
@Adapter(PortType.CLIENT)
@org.springframework.stereotype.Component
public class VerificationCodeAdapter implements VerificationCodePort {

    private final VerificationCodeAppService verificationCodeAppService;

    public VerificationCodeAdapter(VerificationCodeAppService verificationCodeAppService) {
        this.verificationCodeAppService = verificationCodeAppService;
    }

    @Override
    public void verifyPhoneCode(String phone, String code, String purpose) {
        verificationCodeAppService.verifyPhoneCode(new VerifySmsCodeCommand(phone, code, purpose));
    }

    @Override
    public void verifyCode(String email, String code, String purpose) {
        verificationCodeAppService.verifyCode(new VerifyCodeCommand(email, code, purpose));
    }
}
