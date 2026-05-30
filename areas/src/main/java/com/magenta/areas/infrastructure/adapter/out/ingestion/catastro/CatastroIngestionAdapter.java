package com.magenta.areas.infrastructure.adapter.out.ingestion.catastro;

import com.magenta.areas.domain.port.in.ImportGeoJsonPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Adaptador de ingestión de polígonos desde Cartociudad (Catastro / IGN).
 *
 * Descarga el GeoJSON de límites municipales desde la API WFS de Cartociudad
 * y lo importa mediante {@link ImportGeoJsonPort} (mismo flujo que UC-A7).
 *
 * URL de referencia: https://www.cartociudad.es/wfs/inspire/...
 */
@Component
public class CatastroIngestionAdapter {

    private static final Logger log = LoggerFactory.getLogger(CatastroIngestionAdapter.class);

    private static final String DEFAULT_WFS_URL =
        "https://www.cartociudad.es/wfs/inspire/municipios?SERVICE=WFS&VERSION=2.0.0" +
        "&REQUEST=GetFeature&TYPENAMES=AD.Address&outputFormat=application/json&count=1000";

    private final ImportGeoJsonPort importGeoJson;
    private final RestClient restClient;

    /** Tenant de sistema para la ingestión automática */
    @Value("${magenta.ingestion.catastro.tenant-id:00000000-0000-0000-0000-000000000001}")
    private String systemTenantId;

    public CatastroIngestionAdapter(
            ImportGeoJsonPort importGeoJson,
            @Value("${magenta.ingestion.catastro.wfs-url:" + DEFAULT_WFS_URL + "}")
            String wfsUrl) {
        this.importGeoJson = importGeoJson;
        this.restClient    = RestClient.builder().baseUrl(wfsUrl).build();
    }

    /**
     * Cron: domingos a las 02:00 (los polígonos municipales cambian poco).
     */
    @Scheduled(cron = "${magenta.ingestion.catastro.schedule:0 0 2 * * SUN}")
    public void ingestMunicipalBoundaries() {
        log.info("Catastro ingestion — inicio");
        try {
            byte[] geoJsonBytes = fetchGeoJson();
            String sha256 = computeSha256(geoJsonBytes);

            ImportGeoJsonPort.Command cmd = new ImportGeoJsonPort.Command(
                UUID.fromString(systemTenantId),
                new ByteArrayInputStream(geoJsonBytes),
                "catastro-municipios.geojson",
                geoJsonBytes.length,
                sha256,
                "catastro-ingestion"
            );

            importGeoJson.execute(cmd);
            log.info("Catastro ingestion — completado ({} bytes)", geoJsonBytes.length);
        } catch (Exception e) {
            log.error("Catastro ingestion — error", e);
        }
    }

    private byte[] fetchGeoJson() {
        String body = restClient.get()
            .retrieve()
            .body(String.class);
        return body != null ? body.getBytes(StandardCharsets.UTF_8) : new byte[0];
    }

    private String computeSha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }
}
