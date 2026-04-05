package com.aieducenter.account.domain.repository;

import java.util.Optional;

import com.aieducenter.account.domain.aggregate.User;
import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;
import com.cartisan.data.jpa.repository.BaseRepository;

/**
 * 用户仓储接口。
 *
 * @since 0.1.0
 */
@Port(PortType.REPOSITORY)
public interface UserRepository extends BaseRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
