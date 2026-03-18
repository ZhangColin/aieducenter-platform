package com.aieducenter.account.domain.aggregate;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.aieducenter.account.domain.valueobject.Email;
import com.aieducenter.account.domain.valueobject.PhoneNumber;
import com.aieducenter.account.domain.valueobject.Username;
import com.cartisan.core.domain.AggregateRoot;
import com.cartisan.core.exception.DomainException;
import com.cartisan.core.util.Assertions;
import com.cartisan.data.jpa.domain.SoftDeletable;
import com.aieducenter.account.domain.error.UserError;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * User 聚合根。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>封装用户状态和行为</li>
 *   <li>管理用户登录凭证（用户名、邮箱、手机号）</li>
 *   <li>管理密码加密和验证</li>
   *   <li>管理个人信息（昵称、头像）</li>
 * </ul>
 *
 * <h3>不变量</h3>
 * <ul>
 *   <li>用户名不能为空</li>
 *   <li>密码必须加密存储</li>
 *   <li>昵称为空时默认显示用户名</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Entity(name = "user")
@Table(name = "users")
public class User extends SoftDeletable implements AggregateRoot<User> {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(10);

    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 20, unique = true)
    private Username username;

    @Column(name = "email", length = 255, unique = true)
    private Email email;

    @Column(name = "phone_number", length = 20, unique = true)
    private PhoneNumber phoneNumber;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "avatar", length = 512)
    private String avatar;

    /**
     * 创建新用户。
     *
     * @param username 用户名（必填）
     * @param plainPassword 明文密码
     * @param nickname 昵称（可选，为空则使用用户名）
     * @param avatar 头像 URL（可选）
     */
    public User(String username, String plainPassword, String nickname, String avatar) {
        this.username = new Username(username);
        this.password = PASSWORD_ENCODER.encode(plainPassword);

        // 昵称为空则设置为用户名
        if (nickname == null || nickname.isBlank()) {
            this.nickname = username;
        } else {
            this.nickname = nickname;
        }

        this.avatar = avatar;
        this.email = null;
        this.phoneNumber = null;
    }

    /**
     * JPA 默认构造函数（仅用于框架）。
     */
    protected User() {
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

    /**
     * 从 JPA 实体恢复领域模型（仅限基础设施层使用）。
     *
     * @param id ID
     * @param username 用户名
     * @param email 邮箱
     * @param phoneNumber 手机号
     * @param password 加密后的密码
     * @param nickname 昵称
     * @param avatar 头像
     * @return User 聚合根
     */
    public static User restore(Long id, String username, String email, String phoneNumber,
                               String password, String nickname, String avatar) {
        User user = new User();
        user.id = id;
        user.username = new Username(username);
        user.email = email != null ? new Email(email) : null;
        user.phoneNumber = phoneNumber != null ? new PhoneNumber(phoneNumber) : null;
        user.password = password;
        user.nickname = nickname;
        user.avatar = avatar;
        return user;
    }

    // ========== Getter ==========

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username.value();
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email).map(Email::value);
    }

    public Optional<String> getPhoneNumber() {
        return Optional.ofNullable(phoneNumber).map(PhoneNumber::value);
    }

    public String getNickname() {
        return nickname;
    }

    public Optional<String> getAvatar() {
        return Optional.ofNullable(avatar);
    }

    /**
     * 获取加密后的密码。
     * <p>注意：仅供基础设施层使用，不应暴露给外部。</p>
     *
     * @return 加密后的密码
     */
    public String getPassword() {
        return password;
    }

    // ========== 业务行为 ==========

    /**
     * 验证密码。
     *
     * @param plainPassword 明文密码
     * @return 是否匹配
     */
    public boolean matchesPassword(String plainPassword) {
        return PASSWORD_ENCODER.matches(plainPassword, this.password);
    }

    /**
     * 修改用户名。
     * <p>注意：应用层需先校验唯一性</p>
     *
     * @param newUsername 新用户名
     */
    public void updateUsername(String newUsername) {
        this.username = new Username(newUsername);
    }

    /**
     * 修改昵称。
     *
     * @param nickname 新昵称（空则不修改）
     */
    public void updateNickname(String nickname) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
    }

    /**
     * 修改头像。
     *
     * @param avatar 新头像 URL（null 则清空）
     */
    public void updateAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * 修改密码。
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @throws DomainException 旧密码不正确时抛出
     */
    public void updatePassword(String oldPassword, String newPassword) {
        Assertions.require(
            matchesPassword(oldPassword),
            UserError.PASSWORD_INCORRECT
        );
        this.password = PASSWORD_ENCODER.encode(newPassword);
    }
}
