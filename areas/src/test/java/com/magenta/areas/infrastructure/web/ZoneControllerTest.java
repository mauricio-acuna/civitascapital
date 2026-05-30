package com.magenta.areas.infrastructure.web;

import com.magenta.areas.application.GetPriceIndexUseCase;
import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.*;
import com.magenta.areas.domain.port.in.*;
import com.magenta.areas.infrastructure.adapter.in.web.ZoneController;
import com.magenta.areas.infrastructure.adapter.in.web.GlobalExceptionHandler;
import com.magenta.areas.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ZoneController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class ZoneControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean SearchZonesPort  searchZonesPort;
    @MockBean GetZonePort       getZonePort;
    @MockBean ResolvePointPort  resolvePointPort;
    @MockBean CreateZonePort    createZonePort;
    @MockBean UpdateZonePort    updateZonePort;
    @MockBean DeleteZonePort    deleteZonePort;
    @MockBean ImportGeoJsonPort importGeoJsonPort;
    @MockBean GetPriceIndexUseCase priceIndexUseCase;
    @MockBean GetEnrichmentPort getEnrichmentPort;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Zone stubZone(String code, String name) {
        return Zone.reconstitute(
            UUID.randomUUID(), UUID.randomUUID(), code, name,
            ZoneType.MUNICIPALITY, null, null,
            new GeoPoint(40.416, -3.703),
            null, null, null, null,
            ZoneStatus.ACTIVE, java.util.Set.of(), java.util.Set.of(),
            java.time.Instant.now(), java.time.Instant.now(), "system", "system", 0L
        );
    }

    // ── Tests: GET /zones/search ──────────────────────────────────────────────

    @Test
    void search_public_returnsMatchingZones() throws Exception {
        when(searchZonesPort.execute(any()))
            .thenReturn(List.of(stubZone("ES-MD-MDC", "Madrid")));

        mockMvc.perform(get("/api/v1/zones/search")
                .param("q", "Madr")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("ES-MD-MDC"));
    }

    @Test
    void search_emptyResult_returns200WithEmptyList() throws Exception {
        when(searchZonesPort.execute(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/zones/search")
                .param("q", "xyz_nonexistent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    // ── Tests: GET /zones/{id} ────────────────────────────────────────────────

    @Test
    void getZone_found_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(getZonePort.byId(id)).thenReturn(stubZone("ES-AN-GR", "Granada"));
        when(getEnrichmentPort.execute(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/zones/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ES-AN-GR"));
    }

    @Test
    void getZone_notFound_returns404WithProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(getZonePort.byId(id)).thenThrow(new ZoneNotFoundException(id));

        mockMvc.perform(get("/api/v1/zones/{id}", id)
                .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    // ── Tests: GET /zones/resolve ─────────────────────────────────────────────

    @Test
    void resolvePoint_found_returns200() throws Exception {
        when(resolvePointPort.execute(anyDouble(), anyDouble()))
            .thenReturn(Optional.of(stubZone("ES-MD-MDC", "Madrid")));

        mockMvc.perform(get("/api/v1/zones/resolve")
                .param("lat", "40.416")
                .param("lng", "-3.703"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("ES-MD-MDC"));
    }

    @Test
    void resolvePoint_noMatch_returns404() throws Exception {
        when(resolvePointPort.execute(anyDouble(), anyDouble())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/zones/resolve")
                .param("lat", "0.0")
                .param("lng", "0.0"))
            .andExpect(status().isNotFound());
    }

    // ── Tests: POST /zones — autorización ─────────────────────────────────────

    @Test
    void createZone_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/zones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"TEST\",\"name\":\"Test\",\"type\":\"MUNICIPALITY\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void createZone_withUserRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/zones")
                .with(jwt().jwt(j -> j.claim("realm_access",
                    java.util.Map.of("roles", List.of("USER")))))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"TEST\",\"name\":\"Test\",\"type\":\"MUNICIPALITY\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void createZone_withAdminRole_returns201() throws Exception {
        UUID tenantId = UUID.randomUUID();
        when(createZonePort.execute(any()))
            .thenReturn(stubZone("ES-TEST", "Test Zone"));

        mockMvc.perform(post("/api/v1/zones")
                .with(jwt().jwt(j -> j
                    .claim("realm_access", java.util.Map.of("roles", List.of("ADMIN")))
                    .claim("sub", UUID.randomUUID().toString())
                    .claim("tenant_id", tenantId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "ES-TEST",
                      "name": "Test Zone",
                      "type": "MUNICIPALITY",
                      "centroid": { "lat": 40.0, "lng": -3.0 }
                    }
                    """))
            .andExpect(status().isCreated());
    }

    // ── Tests: DELETE /zones/{id} ─────────────────────────────────────────────

    @Test
    void deleteZone_withAdminRole_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/zones/{id}", id)
                .with(jwt().jwt(j -> j
                    .claim("realm_access", java.util.Map.of("roles", List.of("ADMIN")))
                    .claim("sub", UUID.randomUUID().toString())
                    .claim("tenant_id", UUID.randomUUID().toString()))))
            .andExpect(status().isNoContent());
    }
}
