package com.aieducenter.account.domain.aggregate;

import java.time.LocalDateTime;
import java.util.Optional;

import jakarta.persistence.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import cn.hutool.core.lang.Validator;
import com.cartisan.core.domain.AggregateRoot;
import com.cartisan.core.exception.DomainException;
import com.cartisan.core.util.Assertions;
import com.cartisan.data.jpa.domain.SoftDeletable;
import com.aieducenter.account.domain.error.UserError;

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
@Entity
@Table(name = "users")
public class User extends SoftDeletable implements AggregateRoot<User> {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(10);

    /**
     * 验证用户名格式：3-20 位，字母开头，允许字母/数字/下划线
     */
    private boolean isValidUsername(String value) {
        return value.matches("^[a-zA-Z][a-zA-Z0-9_]{2,19}$");
    }

    private void validatePasswordStrength(String plainPassword) {
        if (plainPassword == null || !plainPassword.matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$")) {
            throw new DomainException(UserError.PASSWORD_WEAK);
        }
    }

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 20, unique = true)
    private String username;

    @Column(name = "email", length = 255, unique = true)
    private String email;

    @Column(name = "phone_number", length = 20, unique = true)
    private String phoneNumber;

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
        // 验证用户名格式
        if (!isValidUsername(username)) {
            throw new DomainException(UserError.USERNAME_INVALID);
        }
        this.username = username;

        validatePasswordStrength(plainPassword);
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
        user.username = username;
        user.email = email;
        user.phoneNumber = phoneNumber;
        user.password = password;
        user.nickname = nickname;
        user.avatar = avatar;
        return user;
    }

    public static User registerByEmail(String username, String email, String plainPassword, String nickname) {
        User user = new User(username, plainPassword, nickname, null);
        user.updateEmail(email);
        return user;
    }

    // ========== Getter ==========

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getPhoneNumber() {
        return Optional.ofNullable(phoneNumber);
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
        if (!isValidUsername(newUsername)) {
            throw new DomainException(UserError.USERNAME_INVALID);
        }
        this.username = newUsername;
    }

    /**
     * 修改邮箱（含格式验证）。
     *
     * @param email 新邮箱（null 则清空）
     * @throws DomainException 邮箱格式不正确时抛出
     */
    public void updateEmail(String email) {
        if (email != null && !Validator.isEmail(email)) {
            throw new DomainException(UserError.EMAIL_INVALID);
        }
        this.email = email;
    }

    /**
     * 修改手机号（含格式验证）。
     *
     * @param phoneNumber 新手机号（null 则清空）
     * @throws DomainException 手机号格式不正确时抛出
     */
    public void updatePhoneNumber(String phoneNumber) {
        if (phoneNumber != null && !Validator.isMobile(phoneNumber)) {
            throw new DomainException(UserError.PHONE_NUMBER_INVALID);
        }
        this.phoneNumber = phoneNumber;
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
        validatePasswordStrength(newPassword);
        this.password = PASSWORD_ENCODER.encode(newPassword);
    }
}
