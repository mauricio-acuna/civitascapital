package com.magenta.products.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PropertyAggregateTest {

    private static final UUID TENANT = UUID.randomUUID();

    @Test
    void create_property_starts_in_draft_status() {
        Property p = buildMinimalProperty();
        assertThat(p.status()).isEqualTo(PropertyStatus.DRAFT);
    }

    @Test
    void publish_fails_without_photos() {
        Property p = buildMinimalProperty();
        p.addOperation(draftOperation(p.id()));

        assertThatThrownBy(() -> p.publish("agent"))
                .isInstanceOf(PropertyPublishException.class)
                .hasMessageContaining("at least 3 photos");
    }

    @Test
    void publish_fails_without_active_operation() {
        Property p = buildMinimalProperty();
        addPhotos(p, 3);

        assertThatThrownBy(() -> p.publish("agent"))
                .isInstanceOf(PropertyPublishException.class)
                .hasMessageContaining("at least 1 active operation");
    }

    @Test
    void publish_fails_without_energy_rating() {
        Property p = buildMinimalPropertyNoEnergy();
        addPhotos(p, 3);
        p.addOperation(draftOperation(p.id()));

        assertThatThrownBy(() -> p.publish("agent"))
                .isInstanceOf(PropertyPublishException.class)
                .hasMessageContaining("energyRating");
    }

    @Test
    void publish_succeeds_when_all_invariants_met() {
        Property p = buildMinimalProperty();
        addPhotos(p, 3);
        p.addOperation(draftOperation(p.id()));

        p.publish("agent1");

        assertThat(p.status()).isEqualTo(PropertyStatus.ACTIVE);
        assertThat(p.publishedAt()).isNotNull();
    }

    @Test
    void publish_emits_PropertyPublished_event() {
        Property p = buildMinimalProperty();
        addPhotos(p, 3);
        p.addOperation(draftOperation(p.id()));
        p.pullDomainEvents();

        p.publish("agent1");
        List<Object> events = p.pullDomainEvents();

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(com.magenta.products.domain.event.PropertyPublished.class);
    }

    @Test
    void archive_changes_status_to_archived() {
        Property p = buildMinimalProperty();
        p.archive("admin");
        assertThat(p.status()).isEqualTo(PropertyStatus.ARCHIVED);
    }

    @Test
    void money_rejects_zero_amount() {
        assertThatThrownBy(() -> Money.euros(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void geopoint_rejects_invalid_lat() {
        assertThatThrownBy(() -> new GeoPoint(91, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Property buildMinimalProperty() {
        return Property.create(
                UUID.randomUUID(), TENANT, "REF-001", PropertyType.FLAT, null,
                new OwnerInfo("PRIVATE", null, "Owner"),
                new Location("C/ Test", "1", null, null, "28001",
                        new GeoPoint(40.4, -3.7), UUID.randomUUID(), null,
                        LocationVisibility.NEIGHBORHOOD_ONLY),
                new Surface(new BigDecimal("80"), null, null),
                new Layout(3, 1, null, null, null, 2, true),
                PropertyCondition.GOOD, 2000,
                new EnergyRating(EnergyLetter.D, 150.0, EnergyLetter.D, 30.0, "CERT-01", null),
                Set.of("ELEVATOR"), Set.of(), Set.of("luminoso"), "agent1");
    }

    private Property buildMinimalPropertyNoEnergy() {
        return Property.create(
                UUID.randomUUID(), TENANT, "REF-002", PropertyType.FLAT, null,
                new OwnerInfo("PRIVATE", null, "Owner"),
                new Location("C/ Test", "1", null, null, "28001",
                        new GeoPoint(40.4, -3.7), UUID.randomUUID(), null,
                        LocationVisibility.NEIGHBORHOOD_ONLY),
                new Surface(new BigDecimal("80"), null, null),
                null, null, 2000, null,
                Set.of(), Set.of(), Set.of(), "agent1");
    }

    private void addPhotos(Property p, int count) {
        for (int i = 0; i < count; i++) {
            p.addMedia(new MediaAsset(
                    UUID.randomUUID(), p.id(), MediaKind.PHOTO,
                    "s3://bucket/photo" + i + ".jpg", "image/jpeg",
                    1024 * 100L, 1280, 720, List.of(),
                    i, i == 0, java.time.Instant.now()));
        }
    }

    private Operation draftOperation(UUID propertyId) {
        return new Operation(
                UUID.randomUUID(), propertyId, OperationType.SALE,
                Money.euros(new BigDecimal("100000")),
                null, null, null, null, true,
                null, null, OperationStatus.DRAFT, false, null);
    }
}
