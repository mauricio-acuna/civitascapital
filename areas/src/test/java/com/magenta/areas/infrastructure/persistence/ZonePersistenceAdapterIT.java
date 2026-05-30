package com.magenta.areas.infrastructure.persistence;

import com.magenta.areas.domain.model.*;
import com.magenta.areas.infrastructure.adapter.out.persistence.ZonePersistenceAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(ZonePersistenceAdapter.class)
class ZonePersistenceAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>(DockerImageName.parse("postgis/postgis:16-3.4")
            .asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("areas_db")
            .withUsername("areas")
            .withPassword("areas");

    @DynamicPropertySource
    static void dataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",    postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.schemas",    () -> "areas");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    ZonePersistenceAdapter adapter;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Zone buildZone(String code, String name, ZoneType type) {
        return Zone.create(
            UUID.randomUUID(),
            code,
            name,
            type,
            null,
            new GeoPoint(40.416775, -3.703790),
            "test-actor"
        );
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    void save_and_findById_roundTrip() {
        Zone zone = buildZone("ES-MD-MADRID", "Madrid", ZoneType.MUNICIPALITY);

        adapter.save(zone);

        Optional<Zone> found = adapter.findById(zone.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("ES-MD-MADRID");
        assertThat(found.get().getName()).isEqualTo("Madrid");
        assertThat(found.get().getType()).isEqualTo(ZoneType.MUNICIPALITY);
    }

    @Test
    void findById_nonExistent_returnsEmpty() {
        Optional<Zone> result = adapter.findById(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    @Test
    void findChildren_returnsOnlyDirectChildren() {
        Zone parent = buildZone("ES-MD", "Comunidad Madrid", ZoneType.REGION);
        adapter.save(parent);

        Zone child1 = Zone.create(UUID.randomUUID(), "ES-MD-MDC", "Madrid capital",
            ZoneType.MUNICIPALITY, parent.getId(),
            new GeoPoint(40.416775, -3.703790), "test-actor");
        Zone child2 = Zone.create(UUID.randomUUID(), "ES-MD-ALC", "Alcalá",
            ZoneType.MUNICIPALITY, parent.getId(),
            new GeoPoint(40.481950, -3.364560), "test-actor");
        adapter.save(child1);
        adapter.save(child2);

        List<Zone> children = adapter.findChildren(parent.getId());
        assertThat(children).hasSize(2)
            .extracting(Zone::getCode)
            .containsExactlyInAnyOrder("ES-MD-MDC", "ES-MD-ALC");
    }

    @Test
    void searchByText_matchesNameFragment() {
        adapter.save(buildZone("ES-CT-BCN", "Barcelona", ZoneType.MUNICIPALITY));
        adapter.save(buildZone("ES-CT-BCN-GR", "Barceloneta", ZoneType.NEIGHBORHOOD));

        List<Zone> results = adapter.searchByText("Barcel", List.of(), 10);

        assertThat(results).isNotEmpty()
            .extracting(Zone::getCode)
            .allMatch(code -> code.startsWith("ES-CT-BCN"));
    }

    @Test
    void searchByText_withTypeFilter_restrictsResults() {
        adapter.save(buildZone("ES-PV-BIL", "Bilbao", ZoneType.MUNICIPALITY));
        adapter.save(buildZone("ES-PV", "País Vasco", ZoneType.REGION));

        List<Zone> municipalities = adapter.searchByText("B", List.of(ZoneType.MUNICIPALITY), 10);

        assertThat(municipalities)
            .extracting(Zone::getType)
            .containsOnly(ZoneType.MUNICIPALITY);
    }

    @Test
    void findByPostalCode_returnsMatchingZones() {
        Zone zone = Zone.create(UUID.randomUUID(), "ES-MD-28001", "Centro",
            ZoneType.DISTRICT, null,
            new GeoPoint(40.416775, -3.703790), "test-actor");
        zone.update(zone.getName(), java.util.Set.of("28001"), zone.getTags(),
            zone.getPopulation(), zone.getAreaKm2(), "test-actor");
        adapter.save(zone);

        List<Zone> found = adapter.findByPostalCode("28001");
        assertThat(found).isNotEmpty()
            .allMatch(z -> z.getPostalCodes().contains("28001"));
    }

    @Test
    void deprecate_zone_persists_deprecated_status() {
        Zone zone = buildZone("ES-EX-BAD", "Badajoz", ZoneType.MUNICIPALITY);
        adapter.save(zone);

        zone.deprecate("admin");
        adapter.save(zone);

        Optional<Zone> reloaded = adapter.findById(zone.getId());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getStatus()).isEqualTo(ZoneStatus.DEPRECATED);
    }
}
