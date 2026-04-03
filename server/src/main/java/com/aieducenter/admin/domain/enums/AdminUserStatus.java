package com.aieducenter.admin.domain.enums;

import com.cartisan.core.domain.BaseEnum;

/**
 * 管理员状态枚举。
 *
 * @since 0.1.0
 */
public enum AdminUserStatus implements BaseEnum<AdminUserStatus> {

    /**
     * 激活。
     */
    ACTIVE(1, "激活"),

    /**
     * 禁用。
     */
    DISABLED(0, "禁用");

    private final Integer code;
    private final String name;

    AdminUserStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }
}
