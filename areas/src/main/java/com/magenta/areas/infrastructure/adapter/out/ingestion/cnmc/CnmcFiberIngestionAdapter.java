package com.magenta.areas.infrastructure.adapter.out.ingestion.cnmc;

import com.magenta.areas.domain.model.ZoneEnrichment;
import com.magenta.areas.domain.port.out.EnrichmentRepositoryPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de ingestión de cobertura FTTH desde datos abiertos de la CNMC.
 *
 * Fuente: dataset "Cobertura de banda ancha" de datos.cnmc.es (CSV semestral).
 * Columnas esperadas: CODIGO_INE; MUNICIPIO; FTTH_PCT
 *
 * Actualiza {@link ZoneEnrichment#getFiberCoveragePct()} por cada municipio.
 */
@Component
public class CnmcFiberIngestionAdapter {

    private static final Logger log = LoggerFactory.getLogger(CnmcFiberIngestionAdapter.class);

    private static final String DEFAULT_CSV_URL =
        "https://datos.cnmc.es/datosCNMC/doc/COBERTURA_BB_MUN.csv";

    private final ZoneRepositoryPort       zoneRepo;
    private final EnrichmentRepositoryPort enrichmentRepo;
    private final RestClient               restClient;

    /** Tenant de sistema */
    @Value("${magenta.ingestion.cnmc.tenant-id:00000000-0000-0000-0000-000000000001}")
    private String systemTenantId;

    public CnmcFiberIngestionAdapter(
            ZoneRepositoryPort zoneRepo,
            EnrichmentRepositoryPort enrichmentRepo,
            @Value("${magenta.ingestion.fiber.url:" + DEFAULT_CSV_URL + "}")
            String csvUrl) {
        this.zoneRepo       = zoneRepo;
        this.enrichmentRepo = enrichmentRepo;
        this.restClient     = RestClient.builder().baseUrl(csvUrl).build();
    }

    /**
     * Cron: domingos a las 03:30 (configurable).
     */
    @Scheduled(cron = "${magenta.ingestion.fiber.schedule:0 30 3 * * SUN}")
    public void ingestFiberCoverage() {
        log.info("CNMC fiber ingestion — inicio");
        try {
            String csv = restClient.get().retrieve().body(String.class);
            if (csv == null) {
                log.warn("CNMC fiber ingestion — CSV vacío");
                return;
            }
            int updated = parseCsvAndUpdate(csv);
            log.info("CNMC fiber ingestion — completado. {} zonas actualizadas", updated);
        } catch (Exception e) {
            log.error("CNMC fiber ingestion — error", e);
        }
    }

    private int parseCsvAndUpdate(String csv) throws Exception {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            String header = reader.readLine(); // saltar cabecera
            if (header == null) return 0;

            int ineColIdx   = findColumn(header, "CODIGO_INE", 0);
            int ftthColIdx  = findColumn(header, "FTTH_PCT",   2);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(";", -1);
                if (cols.length <= Math.max(ineColIdx, ftthColIdx)) continue;

                String ineCode = cols[ineColIdx].trim().replaceAll("\"", "");
                String ftthStr = cols[ftthColIdx].trim().replaceAll("\"", "");
                if (ineCode.isEmpty() || ftthStr.isEmpty()) continue;

                int ftthPct;
                try { ftthPct = (int) Math.round(Double.parseDouble(ftthStr)); }
                catch (NumberFormatException ignored) { continue; }
                ftthPct = Math.min(100, Math.max(0, ftthPct));

                // Buscar zona por código INE
                Optional<com.magenta.areas.domain.model.Zone> zoneOpt =
                    zoneRepo.findByCode("INE-" + ineCode);

                if (zoneOpt.isEmpty()) continue;
                UUID zoneId = zoneOpt.get().getId();

                ZoneEnrichment enrichment = enrichmentRepo.findByZoneId(zoneId)
                    .orElseGet(() -> ZoneEnrichment.empty(zoneId,
                        UUID.fromString(systemTenantId)));

                enrichment.update(
                    ftthPct,
                    enrichment.isHasHospital(),
                    enrichment.getHospitalKind(),
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
        return count;
    }

    private int findColumn(String header, String colName, int fallback) {
        String[] cols = header.split(";");
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].trim().replaceAll("\"", "").equalsIgnoreCase(colName)) return i;
        }
        return fallback;
    }
}
