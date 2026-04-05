/**
 * 租户限界上下文。
 *
 * <h3>职责</h3>
 * <ul>
 *   <li>租户创建与管理</li>
 *   <li>租户类型（个人/企业）</li>
 *   <li>租户归属（ownerId）</li>
 * </ul>
 *
 * @since 0.1.0
 */
@BoundedContext(name = "Tenant", subDomain = SubDomain.CORE)
package com.aieducenter.tenant;

import com.cartisan.core.stereotype.BoundedContext;
import com.cartisan.core.stereotype.SubDomain;