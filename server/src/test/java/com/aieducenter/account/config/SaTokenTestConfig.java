package com.aieducenter.account.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.spring.SaTokenContextForSpringInJakartaServlet;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * Sa-Token 测试配置。
 *
 * <p>在 MockMvc 测试中手动初始化 Sa-Token 上下文，确保
 * {@code StpUtil.isLogin()} 能通过 {@code RequestContextHolder} 读取请求。</p>
 */
@TestConfiguration
public class SaTokenTestConfig {

    public static void initSaTokenContext() {
        if (SaManager.getSaTokenContext() == null) {
            SaManager.setSaTokenContext(new SaTokenContextForSpringInJakartaServlet());
        }
    }
}
