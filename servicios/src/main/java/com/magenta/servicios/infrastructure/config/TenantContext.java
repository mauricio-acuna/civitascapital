package com.magenta.servicios.infrastructure.config;

/**
 * Almacén ThreadLocal del tenant_id del request en curso.
 * Se inicializa en {@link TenantFilter} y se limpia en el finally del mismo.
 * Los repositorios JPA lo leen a través del DataSource proxy para inyectar
 * {@code set_config('app.tenant_id', ?, false)} en cada conexión PostgreSQL,
 * lo que activa la política RLS {@code so_tenant_isolation} de service_orders.
 */
public final class TenantContext {

    /** UUID cero: sentinel para requests sin tenant autenticado (endpoints públicos). */
    public static final String ANONYMOUS = "00000000-0000-0000-0000-000000000000";

    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static String getTenantId() {
        String value = TENANT_ID.get();
        return value != null ? value : ANONYMOUS;
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
