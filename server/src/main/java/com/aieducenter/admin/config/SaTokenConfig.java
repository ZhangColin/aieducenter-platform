package com.aieducenter.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.dev33.satoken.stp.StpLogic;

/**
 * Sa-Token 配置。
 *
 * <p>为管理员模块配置独立的 StpLogic Bean，实现多账号体系隔离。</p>
 *
 * @since 0.1.0
 */
@Configuration
public class SaTokenConfig {

    /**
     * 管理员 StpLogic Bean。
     *
     * <p>使用 "admin" 作为 loginType，与普通用户登录隔离。</p>
     *
     * @return 管理员专用的 StpLogic 实例
     */
    @Bean
    public StpLogic adminStpLogic() {
        return new StpLogic("admin");
    }
}
