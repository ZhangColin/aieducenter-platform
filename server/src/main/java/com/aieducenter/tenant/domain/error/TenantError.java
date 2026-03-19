package com.aieducenter.tenant.domain.error;

import com.cartisan.core.exception.CodeMessage;

public enum TenantError implements CodeMessage {
    ;

    @Override
    public int httpStatus() { return 0; }

    @Override
    public String code() { return null; }

    @Override
    public String message() { return null; }
}
