package com.magenta.areas.infrastructure.web;

import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.port.in.GetEnrichmentPort;
import com.magenta.areas.domain.port.in.UpdateEnrichmentPort;
import com.magenta.areas.infrastructure.adapter.in.web.EnrichmentController;
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
 * Tests de autorización BOLA para {@link EnrichmentController}.
 *
 * <p>Verifica que {@code PATCH /api/v1/enrichment/{zoneId}} no permite que un actor
 * de un tenant ajeno modifique el enriquecimiento de zonas que no le pertenecen.
 */
@WebMvcTest(EnrichmentController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
@DisplayName("BOLA – EnrichmentController authorization")
class EnrichmentControllerBOLATest {

    private static final UUID ZONE_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID TENANT_A = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID TENANT_B = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");

    @Autowired MockMvc mockMvc;

    @MockBean GetEnrichmentPort    getEnrichment;
    @MockBean UpdateEnrichmentPort updateEnrichment;

    private static final String ENRICHMENT_BODY = """
        {"fiberCoveragePct":85,"hasHospital":false}
        """;

    // ── Anonymous access ──────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /enrichment/{zoneId} without JWT → 401")
    void patch_anonymous_returns401() throws Exception {
        mockMvc.perform(patch("/api/v1/enrichment/{zoneId}", ZONE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ENRICHMENT_BODY))
            .andExpect(status().isUnauthorized());
    }

    // ── Insufficient role ─────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /enrichment/{zoneId} with USER role → 403")
    void patch_userRole_returns403() throws Exception {
        mockMvc.perform(patch("/api/v1/enrichment/{zoneId}", ZONE_ID)
                .with(jwt()
                    .jwt(b -> b.claim("tenant_id", TENANT_A.toString()).subject("user-a"))
                    .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ENRICHMENT_BODY))
            .andExpect(status().isForbidden());
    }

    // ── BOLA: foreign tenant ──────────────────────────────────────────────────

    @Test
    @DisplayName("BOLA – ADMIN from TENANT_B cannot patch enrichment of TENANT_A zone → 404")
    void patch_foreignTenant_bola_returns404() throws Exception {
        // El use case lanza ZoneNotFoundException porque el tenant filter excluye la zona ajena
        when(updateEnrichment.execute(any())).thenThrow(new ZoneNotFoundException(ZONE_ID));

        mockMvc.perform(patch("/api/v1/enrichment/{zoneId}", ZONE_ID)
                .with(jwt()
                    .jwt(b -> b.claim("tenant_id", TENANT_B.toString()).subject("attacker"))
                    .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(ENRICHMENT_BODY))
            .andExpect(status().isNotFound());
    }
}
