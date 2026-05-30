package com.magenta.areas.infrastructure.adapter.out.ingestion.osm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.areas.domain.model.*;
import com.magenta.areas.domain.port.out.EnrichmentRepositoryPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de ingestión de POIs desde OpenStreetMap (Overpass API).
 *
 * Consulta hospitales y estaciones de tren en España y actualiza
 * {@link ZoneEnrichment} para las zonas que los contienen.
 *
 * Overpass: https://overpass-api.de/api/interpreter
 */
@Component
public class OsmPoiIngestionAdapter {

    private static final Logger log = LoggerFactory.getLogger(OsmPoiIngestionAdapter.class);

    private static final String DEFAULT_OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    /** Consulta Overpass para hospitales en España */
    private static final String HOSPITAL_QUERY =
        "[out:json][timeout:90];" +
        "area[\"ISO3166-1\"=\"ES\"]->.spain;" +
        "nwr[\"amenity\"~\"hospital|clinic\"][\"healthcare\"~\"hospital|clinic\"](area.spain);" +
        "out center;";

    /** Consulta Overpass para estaciones de tren/AVE en España */
    private static final String TRAIN_QUERY =
        "[out:json][timeout:90];" +
        "area[\"ISO3166-1\"=\"ES\"]->.spain;" +
        "nwr[\"railway\"~\"station\"][\"station\"~\"rail|highspeed\"](area.spain);" +
        "out center;";

    private final ZoneRepositoryPort       zoneRepo;
    private final EnrichmentRepositoryPort enrichmentRepo;
    private final RestClient               restClient;
    private final ObjectMapper             objectMapper;

    @Value("${magenta.ingestion.osm.tenant-id:00000000-0000-0000-0000-000000000001}")
    private String systemTenantId;

    public OsmPoiIngestionAdapter(
            ZoneRepositoryPort zoneRepo,
            EnrichmentRepositoryPort enrichmentRepo,
            ObjectMapper objectMapper,
            @Value("${magenta.ingestion.osm.overpass-url:" + DEFAULT_OVERPASS_URL + "}")
            String overpassUrl) {
        this.zoneRepo       = zoneRepo;
        this.enrichmentRepo = enrichmentRepo;
        this.objectMapper   = objectMapper;
        this.restClient     = RestClient.builder().baseUrl(overpassUrl).build();
    }

    /**
     * Cron: sábados a las 04:00 (Overpass tiene cuotas; evitar rush hours).
     */
    @Scheduled(cron = "${magenta.ingestion.osm.schedule:0 0 4 * * SAT}")
    public void ingestPois() {
        log.info("OSM POI ingestion — inicio");
        try {
            ingestHospitals();
            ingestTrainStations();
            log.info("OSM POI ingestion — completado");
        } catch (Exception e) {
            log.error("OSM POI ingestion — error", e);
        }
    }

    // ── Hospitales ────────────────────────────────────────────────────────────

    private void ingestHospitals() throws Exception {
        String response = queryOverpass(HOSPITAL_QUERY);
        JsonNode root = objectMapper.readTree(response);
        int count = 0;
        for (JsonNode element : root.path("elements")) {
            double lat = centerLat(element);
            double lon = centerLon(element);
            if (lat == 0.0 && lon == 0.0) continue;

            Optional<Zone> zoneOpt = zoneRepo.resolvePoint(lat, lon);
            if (zoneOpt.isEmpty()) continue;

            UUID zoneId = zoneOpt.get().getId();
            HospitalKind kind = resolveHospitalKind(element);

            ZoneEnrichment enrichment = enrichmentRepo.findByZoneId(zoneId)
                .orElseGet(() -> ZoneEnrichment.empty(zoneId, UUID.fromString(systemTenantId)));

            // Escalar el kind si el nuevo es "más especializado"
            if (kind.ordinal() > enrichment.getHospitalKind().ordinal()) {
                enrichment.update(
                    enrichment.getFiberCoveragePct(),
                    true,
                    kind,
                    enrichment.getTrainToHubMinutes(),
                    enrichment.getHighwayDistanceKm(),
                    enrichment.getSupermarketsCount(),
                    enrichment.getRiskOccupationScore(),
                    enrichment.getDepopulationRisk(),
                    enrichment.getQualityOfLifeIndex()
                );
                enrichmentRepo.save(enrichment);
                count++;
            }
        }
        log.info("OSM hospitals — {} zonas actualizadas", count);
    }

    // ── Estaciones de tren ────────────────────────────────────────────────────

    private void ingestTrainStations() throws Exception {
        String response = queryOverpass(TRAIN_QUERY);
        JsonNode root = objectMapper.readTree(response);
        int count = 0;
        for (JsonNode element : root.path("elements")) {
            double lat = centerLat(element);
            double lon = centerLon(element);
            if (lat == 0.0 && lon == 0.0) continue;

            Optional<Zone> zoneOpt = zoneRepo.resolvePoint(lat, lon);
            if (zoneOpt.isEmpty()) continue;

            UUID zoneId = zoneOpt.get().getId();
            ZoneEnrichment enrichment = enrichmentRepo.findByZoneId(zoneId)
                .orElseGet(() -> ZoneEnrichment.empty(zoneId, UUID.fromString(systemTenantId)));

            // Solo actualizar si aún no tiene tiempo registrado
            if (enrichment.getTrainToHubMinutes() == null) {
                int estMinutes = estimateMinutesToHub(element);
                enrichment.update(
                    enrichment.getFiberCoveragePct(),
                    enrichment.isHasHospital(),
                    enrichment.getHospitalKind(),
                    estMinutes,
                    enrichment.getHighwayDistanceKm(),
                    enrichment.getSupermarketsCount(),
                    enrichment.getRiskOccupationScore(),
                    enrichment.getDepopulationRisk(),
                    enrichment.getQualityOfLifeIndex()
                );
                enrichmentRepo.save(enrichment);
                count++;
            }
        }
        log.info("OSM train stations — {} zonas actualizadas", count);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String queryOverpass(String qlQuery) {
        return restClient.post()
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("data=" + qlQuery)
            .retrieve()
            .body(String.class);
    }

    private double centerLat(JsonNode el) {
        if (el.has("center")) return el.path("center").path("lat").asDouble(0.0);
        return el.path("lat").asDouble(0.0);
    }

    private double centerLon(JsonNode el) {
        if (el.has("center")) return el.path("center").path("lon").asDouble(0.0);
        return el.path("lon").asDouble(0.0);
    }

    private HospitalKind resolveHospitalKind(JsonNode el) {
        String healthcare = el.path("tags").path("healthcare:speciality").asText("");
        String name = el.path("tags").path("name").asText("").toLowerCase();
        if (name.contains("universitari") || name.contains("referencia")) {
            return HospitalKind.REFERENCE_UNIVERSITY;
        }
        if (name.contains("general") || healthcare.contains("general")) {
            return HospitalKind.GENERAL;
        }
        return HospitalKind.PRIMARY_CARE;
    }

    private int estimateMinutesToHub(JsonNode el) {
        // Heurística: si tiene intercambiador AVE es ~30 min, si es cercanías ~60 min
        String station = el.path("tags").path("station").asText("").toLowerCase();
        return station.contains("highspeed") ? 30 : 60;
    }
}
