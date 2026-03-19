package com.aieducenter.tenant.application.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.aieducenter.account.domain.event.UserRegisteredEvent;
import com.aieducenter.tenant.application.TenantAppService;

@Component
public class UserRegisteredEventListener {

    private final TenantAppService tenantAppService;

    public UserRegisteredEventListener(TenantAppService tenantAppService) {
        this.tenantAppService = tenantAppService;
    }

    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        String tenantName = (event.nickname() != null && !event.nickname().isBlank())
                ? event.nickname() : event.username();
        tenantAppService.createPersonalTenant(event.userId(), tenantName);
    }
}
