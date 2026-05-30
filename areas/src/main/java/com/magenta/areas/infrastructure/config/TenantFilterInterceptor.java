package com.magenta.areas.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

/**
 * Establece el parámetro de sesión PostgreSQL {@code app.tenant_id} en cada request,
 * de forma que las políticas RLS de las tablas del esquema {@code areas} puedan
 * filtrar por tenant de forma transparente.
 *
 * <p>El tenant_id se extrae del claim {@code tenant_id} del JWT (Keycloak). Si el
 * claim no existe, se permite el paso pero el RLS bloqueará el acceso a filas.</p>
 *
 * <p>Orden: debe ejecutarse DESPUÉS de {@link SecurityConfig} ha procesado el JWT,
 * por eso {@code @Order(10)}.</p>
 *
 * <p>Referencia: RFC 9457 (errores), ADR-001 (multi-tenancy por RLS).</p>
 */
@Component
@Order(10)
public class TenantFilterInterceptor extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilterInterceptor.class);
    private static final String TENANT_CLAIM = "tenant_id";

    private final DataSource dataSource;

    public TenantFilterInterceptor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = extractTenantId();

        if (tenantId != null) {
            applyTenantToSession(tenantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Limpiar para evitar contaminación entre requests en el mismo thread
            if (tenantId != null) {
                clearTenantFromSession();
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String extractTenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (auth.getPrincipal() instanceof Jwt jwt) {
            String claim = jwt.getClaimAsString(TENANT_CLAIM);
            if (claim != null) {
                try {
                    UUID.fromString(claim); // validar formato UUID
                    return claim;
                } catch (IllegalArgumentException e) {
                    log.warn("tenant_id claim is not a valid UUID: {}", claim);
                }
            }
        }
        return null;
    }

    private void applyTenantToSession(String tenantId) {
        try (Connection conn = dataSource.getConnection()) {
            // SET LOCAL aplica solo a la transacción actual; SET aplica a la sesión
            conn.createStatement()
                .execute("SET app.tenant_id = '" + tenantId.replace("'", "''") + "'");
        } catch (SQLException e) {
            log.error("Failed to set app.tenant_id on PostgreSQL session", e);
        }
    }

    private void clearTenantFromSession() {
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute("RESET app.tenant_id");
        } catch (SQLException e) {
            log.debug("Failed to reset app.tenant_id", e);
        }
    }
}
