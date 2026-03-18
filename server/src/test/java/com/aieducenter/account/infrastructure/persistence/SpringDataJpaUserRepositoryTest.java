package com.aieducenter.account.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.aieducenter.account.domain.aggregate.User;
import com.cartisan.test.base.IntegrationTestBase;

/**
 * SpringDataJpaUserRepository 集成测试。
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SpringDataJpaUserRepositoryTest extends IntegrationTestBase {

    @Autowired
    private SpringDataJpaUserRepository userRepository;

    @Test
    @Transactional
    void shouldSaveUser() {
        // When
        User user = new User("test_user", "password123", "Test User", null);
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("test_user");
    }

    @Test
    @Transactional
    void shouldFindUserByUsername() {
        // Given
        User user = new User("john_doe", "password123", null, null);
        userRepository.save(user);
        userRepository.flush();

        // When
        User found = userRepository.findByUsername("john_doe").orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("john_doe");
    }

    @Test
    @Transactional
    void shouldReturnEmpty_whenUsernameNotFound() {
        // When
        var found = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @Transactional
    void shouldCheckExistsByUsername() {
        // Given
        User user = new User("exists_test", "password123", null, null);
        userRepository.save(user);
        userRepository.flush();

        // When & Then
        assertThat(userRepository.existsByUsername("exists_test")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @Transactional
    void shouldSoftDeleteUser() {
        // Given
        User user = new User("delete_test", "password123", null, null);
        User saved = userRepository.save(user);
        userRepository.flush();
        Long userId = saved.getId();

        // When
        userRepository.deleteById(userId);
        userRepository.flush();

        // Then - 软删除后无法找到
        assertThat(userRepository.findByUsername("delete_test")).isEmpty();
    }

    @Test
    @Transactional
    void shouldNotFindDeletedUser() {
        // Given
        User user = new User("deleted_user", "password123", null, null);
        userRepository.save(user);
        userRepository.flush();
        userRepository.deleteById(user.getId());
        userRepository.flush();

        // When & Then
        assertThat(userRepository.findByUsername("deleted_user")).isEmpty();
        assertThat(userRepository.existsByUsername("deleted_user")).isFalse();
    }
}
