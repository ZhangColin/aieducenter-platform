package com.aieducenter.tenant.domain.aggregate;

import jakarta.persistence.*;

import com.aieducenter.tenant.domain.model.TenantType;
import com.cartisan.core.domain.AggregateRoot;
import com.cartisan.data.jpa.domain.SoftDeletable;

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
public class Tenant extends SoftDeletable implements AggregateRoot<Tenant> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TenantType type;

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
            this.id = com.cartisan.data.jpa.id.TsidGenerator.newInstance().generate();
        }
    }

    // ========== Getter ==========

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TenantType getType() {
        return type;
    }

    public Long getOwnerId() {
        return ownerId;
    }
}
