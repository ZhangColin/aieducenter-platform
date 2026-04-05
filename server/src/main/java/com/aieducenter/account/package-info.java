/**
 * Account Context。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>用户注册（用户名/邮箱/手机号）</li>
 *   <li>用户登录（密码登录/手机短信验证码登录）</li>
 *   <li>密码管理（修改密码/重置密码）</li>
 *   <li>用户信息管理（昵称/头像/邮箱/手机号）</li>
 * </ul>
 *
 * <h3>限界上下文</h3>
 * <p>平台用户账号中心，独立于 Admin Context 和 Tenant Context</p>
 *
 * <h3>包结构</h3>
 * <ul>
 *   <li>domain - 领域层：聚合根（User）、仓储接口、领域服务、枚举、端口接口</li>
 *   <li>application - 应用层：应用服务（AccountRegistrationAppService、AccountLoginAppService 等）、DTO</li>
 *   <li>infrastructure - 基础设施层：持久化、缓存、消息队列等南向接口适配器</li>
 *   <li>endpoints - 北向接口适配器层：REST API（Controller）</li>
 * </ul>
 *
 * @since 0.1.0
 */
@BoundedContext(
    name = "Account",
    subDomain = SubDomain.SUPPORTING  // CORE → SUPPORTING
)
package com.aieducenter.account;

import com.cartisan.core.stereotype.BoundedContext;
import com.cartisan.core.stereotype.SubDomain;
