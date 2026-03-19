package com.aieducenter.tenant.infrastructure.persistence;

import org.springframework.stereotype.Repository;

import com.aieducenter.tenant.domain.aggregate.Tenant;
import com.aieducenter.tenant.domain.repository.TenantRepository;
import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;
import com.cartisan.data.jpa.repository.BaseRepository;

@Adapter(PortType.REPOSITORY)
@Repository
public interface SpringDataJpaTenantRepository extends BaseRepository<Tenant, Long>, TenantRepository {

    @Override
    Tenant save(Tenant tenant);
}
