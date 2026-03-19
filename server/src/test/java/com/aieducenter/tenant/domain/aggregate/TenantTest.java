package com.aieducenter.tenant.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.aieducenter.tenant.domain.model.TenantType;

class TenantTest {

    @Test
    void shouldCreateTenant_whenFieldsValid() {
        // When
        Tenant tenant = new Tenant("John's Space", TenantType.PERSONAL, 123L);

        // Then
        assertThat(tenant.getName()).isEqualTo("John's Space");
        assertThat(tenant.getType()).isEqualTo(TenantType.PERSONAL);
        assertThat(tenant.getOwnerId()).isEqualTo(123L);
    }
}
