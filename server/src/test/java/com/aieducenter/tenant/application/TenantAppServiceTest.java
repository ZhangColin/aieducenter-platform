package com.aieducenter.tenant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aieducenter.tenant.domain.aggregate.Tenant;
import com.aieducenter.tenant.domain.model.TenantType;
import com.aieducenter.tenant.domain.repository.TenantRepository;

@ExtendWith(MockitoExtension.class)
class TenantAppServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantAppService tenantAppService;

    @Test
    void given_validOwnerAndName_when_createPersonalTenant_then_savesCorrectTenant() {
        // When
        tenantAppService.createPersonalTenant(123L, "John's Space");

        // Then
        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        Tenant saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("John's Space");
        assertThat(saved.getType()).isEqualTo(TenantType.PERSONAL);
        assertThat(saved.getOwnerId()).isEqualTo(123L);
    }
}
