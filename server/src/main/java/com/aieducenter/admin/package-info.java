/**
 * Platform Admin Context。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>平台运营人员管理（管理员、角色、权限、菜单）</li>
 *   <li>管理员登录认证</li>
 *   <li>RBAC 权限体系</li>
 * </ul>
 *
 * <h3>限界上下文</h3>
 * <p>平台运营后台，独立于 Account Context 和 Tenant Context</p>
 *
 * @since 0.1.0
 */
@com.cartisan.core.stereotype.BoundedContext(name = "PlatformAdmin", subDomain = com.cartisan.core.stereotype.SubDomain.SUPPORTING)
package com.aieducenter.admin;
