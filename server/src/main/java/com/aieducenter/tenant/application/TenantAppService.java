package com.aieducenter.tenant.application;

import org.springframework.stereotype.Service;

import com.aieducenter.tenant.domain.aggregate.Tenant;
import com.aieducenter.tenant.domain.model.TenantType;
import com.aieducenter.tenant.domain.repository.TenantRepository;

@Service
public class TenantAppService {

    private final TenantRepository tenantRepository;

    public TenantAppService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public void createPersonalTenant(Long ownerId, String name) {
        Tenant tenant = new Tenant(name, TenantType.PERSONAL, ownerId);
        tenantRepository.save(tenant);
    }
}
