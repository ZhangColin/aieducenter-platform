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
 * <h3>包结构</h3>
 * <ul>
 *   <li>domain - 领域层：聚合根、实体、仓储接口、领域服务</li>
 *   <li>application - 应用层：应用服务、DTO</li>
 *   <li>infrastructure - 基础设施层：仓储实现</li>
 *   <li>web - 表现层：控制器</li>
 * </ul>
 *
 * @since 0.1.0
 */
@BoundedContext(name = "PlatformAdmin", subDomain = SubDomain.SUPPORT)
package com.aieducenter.admin;

import com.cartisan.core.domain.SubDomain;
import com.cartisan.core.stereotype.BoundedContext;
