package com.nexus.gateway.filter;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class OpaGatewayFilter extends AbstractGatewayFilterFactory<OpaGatewayFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(OpaGatewayFilter.class);
    private final WebClient webClient;

    public OpaGatewayFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClient = webClientBuilder.baseUrl("http://nexus-opa:8181").build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> executeWithCircuitBreaker(exchange, chain);
    }

    @CircuitBreaker(name = "opaAuth", fallbackMethod = "opaFallback")
    private Mono<Void> executeWithCircuitBreaker(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(sc -> (Jwt) sc.getAuthentication().getPrincipal())
                .flatMap(jwt -> {
                    String tenantId = jwt.getClaimAsString("tenant_id");
                    String userRole = jwt.getClaimAsString("role");
                    String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

                    Map<String, Object> input = Map.of(
                            "input", Map.of(
                                    "user", Map.of("tenant_id", tenantId, "role", userRole),
                                    "request", Map.of("method", exchange.getRequest().getMethod().name(), "path", exchange.getRequest().getPath().toString()),
                                    "resource", Map.of("tenant_id", tenantId), 
                                    "ip", clientIp
                            )
                    );

                    return webClient.post()
                            .uri("/v1/data/nexus/authz/allow")
                            .bodyValue(input)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .flatMap(response -> {
                                boolean allowed = (boolean) ((Map) response.get("result")).get("allow");
                                if (allowed) {
                                    return chain.filter(exchange);
                                } else {
                                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                    return exchange.getResponse().setComplete();
                                }
                            });
                });
    }

    /**
     * Fallback Profissional para caso o OPA esteja indisponível.
     * ESPECIALISTA: Por segurança (Fail-Safe), negamos o acesso se o validador cair.
     */
    public Mono<Void> opaFallback(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain, Throwable t) {
        logger.error("OPA indisponível! Acionando Fallback de Segurança. Erro: {}", t.getMessage());
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    public static class Config {}
}
