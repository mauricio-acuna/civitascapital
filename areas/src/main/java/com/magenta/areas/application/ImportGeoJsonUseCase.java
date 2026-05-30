package com.magenta.areas.application;

import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.port.in.ImportGeoJsonPort;
import com.magenta.areas.domain.port.out.OutboxPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * UC-A7: Importar polígonos GeoJSON oficiales (INE, Catastro).
 * Valida tamaño (≤ 100 MB), tipo y opcionalmente firma SHA-256.
 */
@Service
@Transactional
public class ImportGeoJsonUseCase implements ImportGeoJsonPort {

    private static final long MAX_SIZE_BYTES = 100L * 1024 * 1024;

    private final ZoneRepositoryPort repository;
    private final OutboxPort outbox;

    public ImportGeoJsonUseCase(ZoneRepositoryPort repository, OutboxPort outbox) {
        this.repository = repository;
        this.outbox     = outbox;
    }

    @Override
    public int execute(Command command) {
        if (command.fileSizeBytes() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("GeoJSON file exceeds 100 MB limit");
        }

        byte[] content;
        try {
            content = command.geojson().readAllBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read GeoJSON input", e);
        }

        if (command.sha256() != null) {
            verifySha256(content, command.sha256());
        }

        GeoJSONReader reader = new GeoJSONReader();
        FeatureCollection fc;
        try {
            fc = (FeatureCollection) org.wololo.geojson.GeoJSONFactory.create(
                    new String(content, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid GeoJSON format", e);
        }

        int count = 0;
        for (Feature feature : fc.getFeatures()) {
            processFeature(feature, command, reader);
            count++;
        }
        return count;
    }

    private void processFeature(Feature feature, Command command, GeoJSONReader reader) {
        Map<String, Object> props = feature.getProperties();
        if (props == null) return;

        String code = getString(props, "code");
        String name = getString(props, "name");
        if (code == null || name == null) return;

        Geometry geom = reader.read(feature.getGeometry());
        Point centroid = geom.getCentroid();

        var geoPoint = new com.magenta.areas.domain.model.GeoPoint(
                centroid.getY(), centroid.getX());

        Zone zone = repository.findByCode(code)
                .orElse(null);

        if (zone == null) {
            zone = Zone.create(command.tenantId(), code, name,
                    com.magenta.areas.domain.model.ZoneType.MUNICIPALITY,
                    null, geoPoint, command.actorId());
        } else {
            zone.update(name, zone.getPostalCodes(), zone.getTags(),
                    zone.getPopulation(), zone.getAreaKm2(), command.actorId());
        }
        zone.setBoundaryWkt(geom.toText());
        Zone saved = repository.save(zone);
        saved.pullDomainEvents().forEach(outbox::publish);
    }

    private String getString(Map<String, Object> props, String key) {
        Object v = props.get(key);
        return v instanceof String s ? s : null;
    }

    private void verifySha256(byte[] content, String expectedHex) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(content);
            String actual = HexFormat.of().formatHex(digest);
            if (!actual.equalsIgnoreCase(expectedHex)) {
                throw new IllegalArgumentException("SHA-256 mismatch: expected " + expectedHex + " got " + actual);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
