package com.aieducenter.tenant.domain.repository;

import com.aieducenter.tenant.domain.aggregate.Tenant;
import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;

@Port(PortType.REPOSITORY)
public interface TenantRepository {
    Tenant save(Tenant tenant);
}
