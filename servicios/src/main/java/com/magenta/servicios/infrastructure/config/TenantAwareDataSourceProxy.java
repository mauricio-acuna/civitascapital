package com.magenta.servicios.infrastructure.config;

import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Envuelve el DataSource de HikariCP y, en cada {@code getConnection()},
 * emite {@code SELECT set_config('app.tenant_id', ?, false)} para que
 * la política RLS de PostgreSQL pueda leer el tenant del request actual.
 *
 * <p>Se usa {@code set_config(..., false)} (nivel sesión, no transacción)
 * porque HikariCP reutiliza conexiones. Al llamar {@code getConnection()}
 * de nuevo siempre se sobreescribe con el tenant correcto del thread actual.
 * Si el thread no tiene tenant (endpoint público) se usa el UUID cero, que
 * no coincide con ningún tenant real y el RLS devuelve 0 filas en service_orders.
 *
 * <p>El bean se instancia en {@link TenantDataSourceConfig}.
 */
public class TenantAwareDataSourceProxy extends DelegatingDataSource {

    public TenantAwareDataSourceProxy(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection conn = super.getConnection();
        applyTenantId(conn);
        return conn;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn = super.getConnection(username, password);
        applyTenantId(conn);
        return conn;
    }

    private void applyTenantId(Connection conn) throws SQLException {
        String tenantId = TenantContext.getTenantId();
        // Usar PreparedStatement para evitar inyección SQL
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT set_config('app.tenant_id', ?, false)")) {
            ps.setString(1, tenantId);
            ps.execute();
        }
    }
}
