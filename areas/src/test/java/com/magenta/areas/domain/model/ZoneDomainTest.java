package com.magenta.areas.domain.model;

import com.magenta.areas.domain.event.ZoneCreated;
import com.magenta.areas.domain.event.ZoneDeprecated;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class ZoneDomainTest {

    private static final UUID TENANT = UUID.randomUUID();

    @Test
    @DisplayName("Zone.create emite ZoneCreated y queda ACTIVE")
    void create_emitsEventAndIsActive() {
        Zone zone = Zone.create(TENANT, "ES-AN-GR", "Granada", ZoneType.PROVINCE,
                null, new GeoPoint(37.17, -3.59), "admin-1");

        assertThat(zone.isActive()).isTrue();
        assertThat(zone.getStatus()).isEqualTo(ZoneStatus.ACTIVE);

        var events = zone.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ZoneCreated.class);
        ZoneCreated e = (ZoneCreated) events.get(0);
        assertThat(e.getCode()).isEqualTo("ES-AN-GR");
    }

    @Test
    @DisplayName("pullDomainEvents vacía la lista interna")
    void pullDomainEvents_clearsQueue() {
        Zone zone = Zone.create(TENANT, "X", "X", ZoneType.COUNTRY, null,
                new GeoPoint(0, 0), "sys");
        zone.pullDomainEvents();
        assertThat(zone.pullDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("Zone.deprecate emite ZoneDeprecated y pasa a DEPRECATED")
    void deprecate_changesStatus() {
        Zone zone = Zone.create(TENANT, "ES-AN-GR", "Granada", ZoneType.PROVINCE,
                null, new GeoPoint(37.17, -3.59), "admin-1");
        zone.pullDomainEvents(); // limpiar crear

        zone.deprecate("admin-1");

        assertThat(zone.isActive()).isFalse();
        assertThat(zone.getStatus()).isEqualTo(ZoneStatus.DEPRECATED);
        var events = zone.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ZoneDeprecated.class);
    }

    @Test
    @DisplayName("deprecate doble no duplica el evento")
    void deprecate_idempotent() {
        Zone zone = Zone.create(TENANT, "ES", "España", ZoneType.COUNTRY, null,
                new GeoPoint(40.41, -3.70), "sys");
        zone.pullDomainEvents();
        zone.deprecate("sys");
        zone.pullDomainEvents();
        zone.deprecate("sys"); // segunda vez
        assertThat(zone.pullDomainEvents()).isEmpty();
    }

    @Test
    @DisplayName("GeoPoint lanza excepción con latitud inválida")
    void geoPoint_invalidLat() {
        assertThatThrownBy(() -> new GeoPoint(91, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lat");
    }

    @Test
    @DisplayName("GeoPoint lanza excepción con longitud inválida")
    void geoPoint_invalidLng() {
        assertThatThrownBy(() -> new GeoPoint(0, 181))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lng");
    }

    @Test
    @DisplayName("Money lanza excepción con divisa inválida")
    void money_invalidCurrency() {
        assertThatThrownBy(() -> Money.of(java.math.BigDecimal.ONE, "XXX"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
