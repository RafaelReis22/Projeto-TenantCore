package com.nexus.product.filter;

import com.nexus.product.context.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TenantFilter implements Filter {

    private final JdbcTemplate jdbcTemplate;

    public TenantFilter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        try {
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String tenantId = jwt.getClaimAsString("tenant_id");
                
                // Injeta no Postgres (RLS)
                jdbcTemplate.execute("SET app.current_tenant = '" + tenantId + "'");
                
                // Injeta no Contexto Java (Service Layer)
                TenantContext.setTenantId(tenantId);
            }
            
            chain.doFilter(request, response);
        } finally {
            // ESPECIALISTA: Sempre limpar o ThreadLocal para evitar vazamento de memória!
            TenantContext.clear();
        }
    }
}
