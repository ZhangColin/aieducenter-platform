package com.aieducenter.account.domain.valueobject;

import com.cartisan.core.domain.ValueObject;
import com.cartisan.core.exception.DomainException;
import com.cartisan.core.util.Assertions;
import com.aieducenter.account.domain.error.UserError;

/**
 * 邮箱值对象。
 *
 * <h3>格式要求</h3>
 * <p>标准邮箱格式：local-part@domain</p>
 * <ul>
 *   <li>local-part: 允许字母、数字、以及 + - _ . 等字符</li>
 *   <li>domain: 标准域名格式</li>
 * </ul>
 *
 * <h3>正则表达式</h3>
 * <pre>^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$</pre>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建邮箱
 * Email email = new Email("user@example.com");
 *
 * // 值比较
 * boolean same = email.sameValueAs(other);
 * }</pre>
 *
 * @since 0.1.0
 */
public record Email(String value) implements ValueObject<String> {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public Email {
        // 前置条件：值不能为空
        Assertions.require(value != null, UserError.EMAIL_INVALID);

        // 前置条件：格式校验
        Assertions.require(
            isValidEmail(value),
            UserError.EMAIL_INVALID
        );
    }

    /**
     * 校验邮箱格式。
     *
     * @param value 邮箱地址
     * @return 格式是否正确
     */
    private static boolean isValidEmail(String value) {
        return value.matches(EMAIL_PATTERN);
    }

    public boolean sameValueAs(Email other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }
}
