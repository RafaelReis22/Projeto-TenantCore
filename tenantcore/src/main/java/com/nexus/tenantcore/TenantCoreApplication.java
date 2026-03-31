package com.nexus.tenantcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class TenantCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenantCoreApplication.class, args);
    }

    @Bean
    public KeyResolver tenantKeyResolver() {
        return exchange -> ReactiveSecurityContextHolder.getContext()
                .map(sc -> (Jwt) sc.getAuthentication().getPrincipal())
                .map(jwt -> jwt.getClaimAsString("tenant_id"))
                .switchIfEmpty(Mono.just("anonymous"));
    }
}
