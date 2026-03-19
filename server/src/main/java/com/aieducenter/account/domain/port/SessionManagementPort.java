package com.aieducenter.account.domain.port;

import com.cartisan.core.stereotype.Port;
import com.cartisan.core.stereotype.PortType;

/**
 * 会话管理接口。
 *
 * <h3>职责</h3>
 * 管理用户会话的生命周期，包括注销用户的所有活跃会话。
 *
 * @since 0.1.0
 */
@Port(PortType.CLIENT)
public interface SessionManagementPort {

    /**
     * 踢出用户的所有会话。
     *
     * @param userId 用户ID
     */
    void kickout(Long userId);
}
