package com.aieducenter.account.domain.aggregate;

import java.util.Optional;

import jakarta.persistence.*;

import com.cartisan.core.domain.AggregateRoot;
import com.cartisan.core.exception.DomainException;
import com.cartisan.core.util.Assertions;
import com.cartisan.core.stereotype.Aggregate;
import com.cartisan.data.jpa.domain.AuditableSoftDeletable;
import com.cartisan.data.jpa.id.TsidGenerator;
import com.aieducenter.account.domain.error.UserError;

import lombok.Getter;
import lombok.Setter;

/**
 * User 聚合根。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>封装用户状态和行为</li>
 *   <li>管理用户登录凭证（用户名、密码）</li>
 *   <li>管理个人信息（昵称、头像）</li>
 * </ul>
 *
 * <h3>不变量</h3>
 * <ul>
 *   <li>用户名不能为空</li>
 *   <li>密码必须加密存储（由应用层加密后传入）</li>
 *   <li>昵称为空时默认显示用户名</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "act_users")
@Aggregate
public class User extends AuditableSoftDeletable implements AggregateRoot<User> {

    private static final String USERNAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$";

    @Getter
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Getter
    @Column(name = "username", nullable = false, length = 20, unique = true)
    private String username;

    @Getter
    @Setter
    @Column(name = "email", length = 255, unique = true)
    private String email;

    @Getter
    @Setter
    @Column(name = "phone_number", length = 20, unique = true)
    private String phoneNumber;

    @Getter
    @Column(name = "password", nullable = false)
    private String password;

    @Getter
    @Column(name = "nickname", length = 50)
    private String nickname;

    @Getter
    @Setter
    @Column(name = "avatar", length = 512)
    private String avatar;

    /**
     * 创建新用户。
     *
     * @param username 用户名（必填）
     * @param encodedPassword 加密后的密码（应用服务层已加密）
     * @param nickname 昵称（可选，为空则使用用户名）
     */
    public User(String username, String encodedPassword, String nickname) {
        validateUsername(username);
        Assertions.require(encodedPassword != null, UserError.PASSWORD_WEAK);
        this.username = username;
        this.password = encodedPassword;
        this.nickname = (nickname == null || nickname.isBlank()) ? username : nickname;
        this.email = null;
        this.phoneNumber = null;
        this.avatar = null;
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
            this.id = TsidGenerator.newInstance().generate();
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

    /**
     * 注册新用户。
     *
     * @param username 用户名（必填）
     * @param encodedPassword 加密后的密码（应用服务层已加密）
     * @param nickname 昵称（可选），如果为 null 或空字符串则默认使用用户名
     * @param email 邮箱（可选）
     * @param phone 手机号（可选）
     * @return 新创建的 User 实例
     */
    public static User register(String username, String encodedPassword, String nickname, String email, String phone) {
        User user = new User(username, encodedPassword, nickname);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        return user;
    }

    // ========== 业务行为 ==========

    /**
     * 修改用户名。
     * <p>注意：应用层需先校验唯一性</p>
     *
     * @param newUsername 新用户名
     */
    public void updateUsername(String newUsername) {
        validateUsername(newUsername);
        this.username = newUsername;
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
     * 修改密码。
     *
     * @param oldEncodedPassword 旧密码（已加密）
     * @param newEncodedPassword 新密码（已加密）
     * @throws DomainException 旧密码不正确时抛出
     */
    public void updatePassword(String oldEncodedPassword, String newEncodedPassword) {
        Assertions.require(
            oldEncodedPassword.equals(this.password),
            UserError.PASSWORD_INCORRECT
        );
        Assertions.require(newEncodedPassword != null, UserError.PASSWORD_WEAK);
        this.password = newEncodedPassword;
    }

    /**
     * 重置密码（无需旧密码，管理员或找回密码场景使用）。
     *
     * @param encodedPassword 新密码（已加密）
     * @throws DomainException 密码为 null 时抛出
     */
    public void resetPassword(String encodedPassword) {
        Assertions.require(encodedPassword != null, UserError.PASSWORD_WEAK);
        this.password = encodedPassword;
    }

    // ========== Private 方法 ==========

    private void validateUsername(String value) {
        if (value == null || !value.matches(USERNAME_PATTERN)) {
            throw new DomainException(UserError.USERNAME_INVALID);
        }
    }
}
