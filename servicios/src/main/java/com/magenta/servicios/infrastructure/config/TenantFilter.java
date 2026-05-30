package com.magenta.servicios.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extrae el claim {@code tenant_id} del JWT validado por Spring Security y lo
 * almacena en {@link TenantContext} para el hilo actual.
 *
 * <p>Debe ejecutarse DESPUÉS de {@link org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter}
 * para que el {@code SecurityContext} ya tenga el JWT. Se registra vía
 * {@code SecurityConfig#filterChain} con {@code addFilterAfter}.
 *
 * <p>El bloque {@code finally} garantiza que el ThreadLocal se limpia al final
 * del request aunque se produzca una excepción, evitando fugas entre threads
 * del pool de Tomcat.
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                String tenantId = jwtAuth.getToken().getClaimAsString("tenant_id");
                if (tenantId != null && !tenantId.isBlank()) {
                    TenantContext.setTenantId(tenantId);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /** Los endpoints públicos no necesitan tenant; el ThreadLocal queda en ANONYMOUS. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }
}
