package com.aieducenter.tenant.domain.aggregate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.aieducenter.tenant.domain.model.TenantType;
import com.cartisan.data.jpa.annotation.EnumConvert;
import com.cartisan.core.domain.AggregateRoot;
import com.cartisan.core.stereotype.Aggregate;
import com.cartisan.data.jpa.domain.AuditableSoftDeletable;
import com.cartisan.data.jpa.id.TsidGenerator;

import lombok.Getter;

/**
 * Tenant 聚合根。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>封装租户状态</li>
 *   <li>管理租户归属（ownerId）</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "tenants")
@Aggregate
public class Tenant extends AuditableSoftDeletable implements AggregateRoot<Tenant> {

    @Getter
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Getter
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Getter
    @EnumConvert(TenantType.class)
    @Column(name = "type", nullable = false)
    private TenantType type;

    @Getter
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    public Tenant(String name, TenantType type, Long ownerId) {
        this.name = name;
        this.type = type;
        this.ownerId = ownerId;
    }

    /**
     * JPA 默认构造函数（仅用于框架）。
     */
    protected Tenant() {
        // JPA required
    }

    /**
     * JPA 保存前生成 ID。
     */
    @PrePersist
    void prePersist() {
        if (id == null) {
            this.id = TsidGenerator.newInstance().generate();
        }
    }
}
