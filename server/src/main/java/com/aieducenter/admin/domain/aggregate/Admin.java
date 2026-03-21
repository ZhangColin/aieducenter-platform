package com.aieducenter.admin.domain.aggregate;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.cartisan.core.domain.AggregateRoot;
import com.cartisan.core.exception.DomainException;
import com.cartisan.core.util.Assertions;
import com.cartisan.data.jpa.domain.SoftDeletable;
import com.aieducenter.admin.domain.error.AdminError;

import jakarta.persistence.*;

/**
 * Admin 聚合根。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>封装管理员状态和行为</li>
 *   <li>管理登录凭证（用户名、密码）</li>
 *   <li>管理个人信息（昵称、邮箱、手机号、头像）</li>
 * </ul>
 *
 * <h3>不变量</h3>
 * <ul>
 *   <li>用户名不能为空且格式正确</li>
 *   <li>密码必须加密存储</li>
 *   <li>系统内置管理员不能被删除</li>
 * </ul>
 *
 * @since 0.1.0
 */
@Entity
@Table(name = "admin_users")
public class Admin extends SoftDeletable implements AggregateRoot<Admin> {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(10);

    private static final String USERNAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$";

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "avatar", length = 512)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AdminStatus status;

    @Column(name = "system", nullable = false)
    private boolean system = false;

    /**
     * 管理员状态枚举。
     */
    public enum AdminStatus {
        ACTIVE,
        DISABLED
    }

    /**
     * 创建管理员。
     *
     * @param username 用户名（必填）
     * @param plainPassword 明文密码
     * @param nickname 昵称
     */
    public Admin(String username, String plainPassword, String nickname) {
        validateUsername(username);
        validatePasswordStrength(plainPassword);
        this.username = username;
        this.password = PASSWORD_ENCODER.encode(plainPassword);
        this.nickname = nickname != null && !nickname.isBlank() ? nickname : username;
        this.status = AdminStatus.ACTIVE;
    }

    /**
     * JPA 默认构造函数。
     */
    protected Admin() {
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

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getPhone() {
        return Optional.ofNullable(phone);
    }

    public Optional<String> getAvatar() {
        return Optional.ofNullable(avatar);
    }

    public AdminStatus getStatus() {
        return status;
    }

    public boolean isSystem() {
        return system;
    }

    public boolean isActive() {
        return status == AdminStatus.ACTIVE;
    }

    // ========== 业务行为 ==========

    /**
     * 验证密码。
     */
    public boolean matchesPassword(String plainPassword) {
        return PASSWORD_ENCODER.matches(plainPassword, this.password);
    }

    /**
     * 修改密码。
     */
    public void updatePassword(String oldPassword, String newPassword) {
        Assertions.require(matchesPassword(oldPassword), AdminError.PASSWORD_INCORRECT);
        validatePasswordStrength(newPassword);
        this.password = PASSWORD_ENCODER.encode(newPassword);
    }

    /**
     * 重置密码（管理员操作）。
     */
    public void resetPassword(String plainPassword) {
        validatePasswordStrength(plainPassword);
        this.password = PASSWORD_ENCODER.encode(plainPassword);
    }

    /**
     * 修改用户名。
     */
    public void updateUsername(String newUsername) {
        validateUsername(newUsername);
        this.username = newUsername;
    }

    /**
     * 修改昵称。
     */
    public void updateNickname(String nickname) {
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
    }

    /**
     * 修改邮箱。
     */
    public void updateEmail(String email) {
        this.email = email;
    }

    /**
     * 修改手机号。
     */
    public void updatePhone(String phone) {
        this.phone = phone;
    }

    /**
     * 修改头像。
     */
    public void updateAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * 禁用管理员。
     */
    public void disable() {
        this.status = AdminStatus.DISABLED;
    }

    /**
     * 启用管理员。
     */
    public void enable() {
        this.status = AdminStatus.ACTIVE;
    }

    /**
     * 检查是否可以删除。
     */
    public void checkCanBeDeleted() {
        Assertions.require(!system, AdminError.ADMIN_SYSTEM_CANNOT_DELETE);
    }

    // ========== 私有方法 ==========

    private void validateUsername(String username) {
        if (username == null || !username.matches(USERNAME_PATTERN)) {
            throw new DomainException(AdminError.USERNAME_INVALID);
        }
    }

    private void validatePasswordStrength(String plainPassword) {
        if (plainPassword == null || !plainPassword.matches(PASSWORD_PATTERN)) {
            throw new DomainException(AdminError.PASSWORD_WEAK);
        }
    }
}
