package com.magenta.areas.application;

import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.*;
import com.magenta.areas.domain.port.in.CompareZonesPort;
import com.magenta.areas.domain.port.out.EnrichmentRepositoryPort;
import com.magenta.areas.domain.port.out.PriceIndexRepositoryPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CompareZonesUseCaseTest {

    private ZoneRepositoryPort zoneRepo;
    private PriceIndexRepositoryPort priceRepo;
    private EnrichmentRepositoryPort enrichRepo;
    private CompareZonesUseCase useCase;

    private final UUID TENANT = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        zoneRepo   = Mockito.mock(ZoneRepositoryPort.class);
        priceRepo  = Mockito.mock(PriceIndexRepositoryPort.class);
        enrichRepo = Mockito.mock(EnrichmentRepositoryPort.class);
        useCase    = new CompareZonesUseCase(zoneRepo, priceRepo, enrichRepo);
    }

    @Test
    @DisplayName("Compare con 0 zonas lanza excepción")
    void compare_emptyList_throws() {
        assertThatThrownBy(() -> useCase.execute(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Compare con más de 5 zonas lanza excepción")
    void compare_tooMany_throws() {
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        assertThatThrownBy(() -> useCase.execute(ids))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Compare zona inexistente lanza ZoneNotFoundException")
    void compare_unknownZone_throws() {
        UUID id = UUID.randomUUID();
        when(zoneRepo.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(List.of(id)))
                .isInstanceOf(ZoneNotFoundException.class);
    }

    @Test
    @DisplayName("Compare 2 zonas devuelve la lista con enriquecimiento vacío si no existe")
    void compare_twoZones_success() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Zone z1 = Zone.create(TENANT, "Z1", "Tortosa", ZoneType.MUNICIPALITY, null,
                new GeoPoint(40.81, 0.52), "sys");
        Zone z2 = Zone.create(TENANT, "Z2", "Balaguer", ZoneType.MUNICIPALITY, null,
                new GeoPoint(41.79, 0.80), "sys");

        when(zoneRepo.findById(id1)).thenReturn(Optional.of(z1));
        when(zoneRepo.findById(id2)).thenReturn(Optional.of(z2));
        when(priceRepo.findLatest(any(), any(), any())).thenReturn(Optional.empty());
        when(enrichRepo.findByZoneId(any())).thenReturn(Optional.empty());

        List<CompareZonesPort.ZoneComparison> result = useCase.execute(List.of(id1, id2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).zone().getName()).isEqualTo("Tortosa");
        assertThat(result.get(0).latestSale()).isNull();
        assertThat(result.get(0).enrichment()).isNotNull(); // ZoneEnrichment.empty(...)
    }
}
