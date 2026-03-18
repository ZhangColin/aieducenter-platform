package com.aieducenter.account.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aieducenter.account.domain.aggregate.User;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * Spring Data JPA User Repository。
 *
 * <p>Spring Data JPA 自动生成实现，提供基础 CRUD 能力。</p>
 *
 * @since 0.1.0
 */
@Repository
interface SpringDataJpaUserRepository extends BaseRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.deleted = false")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deleted = false")
    boolean existsByUsername(@Param("username") String username);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deleted = false")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.phoneNumber = :phoneNumber AND u.deleted = false")
    boolean existsByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
