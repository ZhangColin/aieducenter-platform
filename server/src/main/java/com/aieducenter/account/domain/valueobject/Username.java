package com.aieducenter.account.domain.valueobject;

import com.cartisan.core.domain.ValueObject;
import com.cartisan.core.exception.DomainException;
import com.cartisan.core.util.Assertions;
import com.aieducenter.account.domain.error.UserError;

/**
 * 用户名值对象。
 *
 * <h3>格式要求</h3>
 * <ul>
 *   <li>长度：3-20 位</li>
 *   <li>必须以字母开头</li>
 *   <li>只允许字母、数字、下划线</li>
 * </ul>
 *
 * <h3>正则表达式</h3>
 * <pre>^[a-zA-Z][a-zA-Z0-9_]{2,19}$</pre>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建用户名
 * Username username = new Username("john_doe");
 *
 * // 值比较
 * boolean same = username.sameValueAs(other);
 * }</pre>
 *
 * @since 0.1.0
 */
public record Username(String value) implements ValueObject<String> {

    private static final String USERNAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$";

    public Username {
        // 前置条件：值不能为空
        Assertions.require(value != null, UserError.USERNAME_INVALID);

        // 前置条件：格式校验
        Assertions.require(
            isValidUsername(value),
            UserError.USERNAME_INVALID
        );
    }

    /**
     * 校验用户名格式。
     *
     * @param value 用户名
     * @return 格式是否正确
     */
    private static boolean isValidUsername(String value) {
        return value.matches(USERNAME_PATTERN);
    }

    public boolean sameValueAs(Username other) {
        if (other == null) {
            return false;
        }
        return this.value.equals(other.value);
    }
}
