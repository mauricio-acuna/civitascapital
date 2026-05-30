package com.magenta.areas.infrastructure.adapter.out.ingestion.ine;

import com.magenta.areas.domain.model.ZoneType;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Adaptador de ingestión nocturna del INE.
 *
 * Fuente: API JSON del INE → municipios españoles con código y población.
 * Operación: actualiza `ineCode` y `population` en zones existentes (por code).
 * No crea zonas nuevas — la jerarquía base se importa vía GeoJSON (UC-A7).
 *
 * URL de referencia: https://servicios.ine.es/wstempus/js/ES/DATOS_TABLA/2853
 */
@Component
public class IneIngestionAdapter {

    private static final Logger log = LoggerFactory.getLogger(IneIngestionAdapter.class);

    private static final String INE_MUNICIPIOS_URL =
        "https://servicios.ine.es/wstempus/js/ES/DATOS_TABLA/2853?tip=AM";

    private final ZoneRepositoryPort zoneRepo;
    private final RestClient restClient;

    public IneIngestionAdapter(
            ZoneRepositoryPort zoneRepo,
            @Value("${magenta.ingestion.ine.base-url:" + INE_MUNICIPIOS_URL + "}")
            String baseUrl) {
        this.zoneRepo   = zoneRepo;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Cron: por defecto todos los días a las 03:00 (configurable).
     */
    @Scheduled(cron = "${magenta.ingestion.ine.schedule:0 0 3 * * *}")
    public void ingestMunicipios() {
        log.info("INE ingestion — inicio");
        try {
            List<Map<String, Object>> items = fetchMunicipios();
            int updated = 0;
            for (Map<String, Object> item : items) {
                String ineCode = extractIneCode(item);
                Integer population = extractPopulation(item);
                if (ineCode == null) continue;

                int count = zoneRepo.findByCode(buildCode(ineCode))
                    .stream()
                    .mapToInt(zone -> {
                        zone.update(zone.getName(), zone.getPostalCodes(), zone.getTags(),
                            population, zone.getAreaKm2(), "ine-ingestion");
                        zoneRepo.save(zone);
                        return 1;
                    })
                    .sum();
                updated += count;
            }
            log.info("INE ingestion — completado. {} zonas actualizadas", updated);
        } catch (Exception e) {
            log.error("INE ingestion — error", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchMunicipios() {
        List<?> raw = restClient.get()
            .retrieve()
            .body(List.class);
        if (raw == null) return List.of();
        return (List<Map<String, Object>>) raw;
    }

    private String extractIneCode(Map<String, Object> item) {
        Object cod = item.get("COD");
        return cod != null ? cod.toString().trim() : null;
    }

    private Integer extractPopulation(Map<String, Object> item) {
        try {
            Object data = item.get("Data");
            if (data instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?, ?> map) {
                    Object valor = map.get("Valor");
                    if (valor != null) return ((Number) valor).intValue();
                }
            }
        } catch (Exception ignored) { /* valor no disponible */ }
        return null;
    }

    /**
     * Construye el code canónico a partir del código INE de municipio.
     * Formato INE: PPMMM (2 dígitos provincia + 3 municipio).
     * Code resultante: ES-<PROV>-<MUNI> — aproximación; ajustar según catálogo real.
     */
    private String buildCode(String ineCode) {
        return "INE-" + ineCode;
    }
}
