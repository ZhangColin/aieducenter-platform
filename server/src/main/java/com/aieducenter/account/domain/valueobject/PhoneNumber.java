package com.aieducenter.account.domain.valueobject;

import com.cartisan.core.domain.ValueObject;
import com.cartisan.core.exception.DomainException;
import com.cartisan.core.util.Assertions;
import com.aieducenter.account.domain.error.UserError;

/**
 * 手机号值对象。
 *
 * <h3>格式要求</h3>
 * <p>中国大陆手机号格式</p>
 * <ul>
 *   <li>长度：11 位数字</li>
 *   <li>第一位：1</li>
 *   <li>第二位：3-9</li>
 * </ul>
 *
 * <h3>正则表达式</h3>
 * <pre>^1[3-9]\d{9}$</pre>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建手机号
 * PhoneNumber phone = new PhoneNumber("13812345678");
 *
 * // 值比较
 * boolean same = phone.sameValueAs(other);
 * }</pre>
 *
 * @since 0.1.0
 */
public record PhoneNumber(String value) implements ValueObject<String> {

    private static final String PHONE_NUMBER_PATTERN = "^1[3-9]\\d{9}$";

    public PhoneNumber {
        // 前置条件：值不能为空
        Assertions.require(value != null, UserError.PHONE_NUMBER_INVALID);

        // 前置条件：格式校验
        Assertions.require(
            isValidPhoneNumber(value),
            UserError.PHONE_NUMBER_INVALID
        );
    }

    /**
     * 校验手机号格式。
     *
     * @param value 手机号
     * @return 格式是否正确
     */
    private static boolean isValidPhoneNumber(String value) {
        return value.matches(PHONE_NUMBER_PATTERN);
    }

    public boolean sameValueAs(PhoneNumber other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }
}
