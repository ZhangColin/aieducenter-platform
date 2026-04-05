package com.aieducenter.tenant.domain.model;

import com.cartisan.core.domain.BaseEnum;

/**
 * 租户类型。
 *
 * @since 0.1.0
 */
public enum TenantType implements BaseEnum<TenantType> {
    /**
     * 个人租户。
     */
    PERSONAL(1, "个人");

    private final Integer code;
    private final String name;

    TenantType(Integer code, String name) {
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
