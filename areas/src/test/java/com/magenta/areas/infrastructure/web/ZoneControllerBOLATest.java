package com.magenta.areas.infrastructure.web;

import com.magenta.areas.application.GetPriceIndexUseCase;
import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.*;
import com.magenta.areas.domain.port.in.*;
import com.magenta.areas.infrastructure.adapter.in.web.ZoneController;
import com.magenta.areas.infrastructure.adapter.in.web.GlobalExceptionHandler;
import com.magenta.areas.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de autorización BOLA (Broken Object Level Authorization — OWASP API Top 10 #1).
 *
 * <p>Verifica que los endpoints mutables de {@code /api/v1/zones} NO permiten que un actor
 * de un tenant ajeno modifique recursos que no le pertenecen.  El aislamiento real se
 * aplica en la capa de persistencia mediante el filtro Hibernate ({@code tenant_id}).
 * Estos tests simulan el comportamiento esperado del repositorio: si el recurso no es
 * visible para el tenant solicitante, el caso de uso lanza {@link ZoneNotFoundException}
 * → la respuesta HTTP debe ser 404, nunca 200 con datos de otro tenant.
 *
 * <p>Escenarios cubiertos:
 * <ol>
 *   <li>Acceso anónimo a endpoints ADMIN → 401</li>
 *   <li>Rol USER (sin ADMIN) intenta mutación → 403</li>
 *   <li>ADMIN de tenant ajeno intenta {@code PUT /zones/{id}} → 404</li>
 *   <li>ADMIN de tenant ajeno intenta {@code DELETE /zones/{id}} → 404</li>
 *   <li>ADMIN de tenant correcto puede actualizar → 200</li>
 * </ol>
 */
@WebMvcTest(ZoneController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
@DisplayName("BOLA – ZoneController authorization")
class ZoneControllerBOLATest {

    private static final UUID ZONE_ID   = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TENANT_A  = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID TENANT_B  = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");

    @Autowired MockMvc mockMvc;

    @MockBean SearchZonesPort     searchZonesPort;
    @MockBean GetZonePort          getZonePort;
    @MockBean ResolvePointPort     resolvePointPort;
    @MockBean CreateZonePort       createZonePort;
    @MockBean UpdateZonePort       updateZonePort;
    @MockBean DeleteZonePort       deleteZonePort;
    @MockBean ImportGeoJsonPort    importGeoJsonPort;
    @MockBean GetPriceIndexUseCase priceIndexUseCase;
    @MockBean GetEnrichmentPort    getEnrichmentPort;
    @MockBean CompareZonesPort     compareZonesPort;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private static final String ZONE_UPDATE_BODY = """
        {"code":"ES-MD-TST","name":"Test Zone","type":"MUNICIPALITY","lat":40.0,"lng":-3.0}
        """;

    private Zone stubZone() {
        return Zone.reconstitute(
            ZONE_ID, TENANT_A, "ES-MD-TST", "Test Zone",
            ZoneType.MUNICIPALITY, null, null,
            new GeoPoint(40.416, -3.703),
            null, null, null, null,
            ZoneStatus.ACTIVE, java.util.Set.of(), java.util.Set.of(),
            java.time.Instant.now(), java.time.Instant.now(), "system", "system", 0L
        );
    }

    // ── 1. Anonymous access ───────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /zones/{id} without JWT → 401")
    void put_anonymous_returns401() throws Exception {
        mockMvc.perform(put("/api/v1/zones/{id}", ZONE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ZONE_UPDATE_BODY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /zones/{id} without JWT → 401")
    void delete_anonymous_returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/zones/{id}", ZONE_ID))
            .andExpect(status().isUnauthorized());
    }

    // ── 2. Insufficient role ──────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /zones/{id} with USER role (no ADMIN) → 403")
    void put_userRole_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/zones/{id}", ZONE_ID)
                .with(jwt()
                    .jwt(b -> b.claim("tenant_id", TENANT_A.toString()).subject("user-a"))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ZONE_UPDATE_BODY))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /zones/{id} with USER role → 403")
    void delete_userRole_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/zones/{id}", ZONE_ID)
                .with(jwt()
                    .jwt(b -> b.claim("tenant_id", TENANT_A.toString()).subject("user-a"))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isForbidden());
    }

    // ── 3. BOLA: ADMIN from foreign tenant tries PUT ──────────────────────────

    @Test
    @DisplayName("BOLA – ADMIN from TENANT_B cannot update zone owned by TENANT_A → 404")
    void put_foreignTenant_bola_returns404() throws Exception {
        // Simula lo que haría el filtro Hibernate: la zona no existe en el scope de TENANT_B
        when(updateZone.execute(any())).thenThrow(new ZoneNotFoundException(ZONE_ID));

        mockMvc.perform(put("/api/v1/zones/{id}", ZONE_ID)
                .with(jwt()
                    .jwt(b -> b.claim("tenant_id", TENANT_B.toString()).subject("attacker"))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ZONE_UPDATE_BODY))
            .andExpect(status().isNotFound());
    }

    // ── 4. BOLA: ADMIN from foreign tenant tries DELETE ───────────────────────

    @Test
    @DisplayName("BOLA – ADMIN from TENANT_B cannot delete zone owned by TENANT_A → 404")
    void delete_foreignTenant_bola_returns404() throws Exception {
        // Simula lo que haría el filtro Hibernate: zona no visible para TENANT_B
        when(deleteZone.execute(any(), any())).thenThrow(new ZoneNotFoundException(ZONE_ID));

        mockMvc.perform(delete("/api/v1/zones/{id}", ZONE_ID)
                .with(jwt()
                    .jwt(b -> b.claim("tenant_id", TENANT_B.toString()).subject("attacker"))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isNotFound());
    }

    // ── 5. Correct tenant can update ─────────────────────────────────────────

    @Test
    @DisplayName("ADMIN from TENANT_A can update zone owned by TENANT_A → 200")
    void put_correctTenant_returns200() throws Exception {
        when(updateZone.execute(any())).thenReturn(stubZone());

        mockMvc.perform(put("/api/v1/zones/{id}", ZONE_ID)
                .with(jwt()
                    .jwt(b -> b.claim("tenant_id", TENANT_A.toString()).subject("admin-a"))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ZONE_UPDATE_BODY))
            .andExpect(status().isOk());
    }
}
