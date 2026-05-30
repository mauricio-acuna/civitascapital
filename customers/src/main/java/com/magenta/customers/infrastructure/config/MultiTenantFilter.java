package com.magenta.customers.infrastructure.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extrae `tenant_id` del JWT y lo inyecta como:
 *  - MDC para logging estructurado
 *  - Hibernate filter (se pasa a través de TenantContext)
 *  - PostgreSQL SET LOCAL app.tenant_id (via EntityManager interceptor)
 */
@Component
@Slf4j
public class MultiTenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String tenantId = jwt.getClaimAsString("tenant_id");
            if (tenantId != null) {
                TenantContext.set(tenantId);
                org.slf4j.MDC.put("tenantId", tenantId);
            }
            String userId = jwt.getSubject();
            if (userId != null) {
                org.slf4j.MDC.put("userId", userId);
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            org.slf4j.MDC.remove("tenantId");
            org.slf4j.MDC.remove("userId");
        }
    }
}
