package com.aieducenter.account.infrastructure;

import org.springframework.stereotype.Service;

import com.cartisan.core.stereotype.Adapter;
import com.cartisan.core.stereotype.PortType;
import com.aieducenter.account.domain.port.SessionManagementPort;
import cn.dev33.satoken.stp.StpUtil;

/**
 * Sa-Token 会话管理适配器。
 *
 * <h3>职责</h3>
 * 使用 Sa-Token 库实现用户会话的注销功能。
 *
 * @since 0.1.0
 */
@Adapter(PortType.CLIENT)
@Service
public class SaTokenSessionManagementAdapter implements SessionManagementPort {

    @Override
    public void kickout(Long userId) {
        StpUtil.kickout(userId);
    }
}
