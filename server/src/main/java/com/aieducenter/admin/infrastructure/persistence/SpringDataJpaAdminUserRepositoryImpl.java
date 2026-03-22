package com.aieducenter.admin.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * SpringDataJpaAdminUserRepository 自定义方法实现。
 */
public class SpringDataJpaAdminUserRepositoryImpl implements SpringDataJpaAdminUserRepositoryCustom {

    @PersistenceContext
    private EntityManager em;
}
