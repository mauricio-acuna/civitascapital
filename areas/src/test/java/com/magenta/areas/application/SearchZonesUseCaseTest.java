package com.magenta.areas.application;

import com.magenta.areas.domain.model.GeoPoint;
import com.magenta.areas.domain.model.Zone;
import com.magenta.areas.domain.model.ZoneType;
import com.magenta.areas.domain.port.in.SearchZonesPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SearchZonesUseCaseTest {

    private ZoneRepositoryPort repo;
    private SearchZonesUseCase useCase;

    @BeforeEach
    void setUp() {
        repo    = Mockito.mock(ZoneRepositoryPort.class);
        useCase = new SearchZonesUseCase(repo);
    }

    @Test
    @DisplayName("Query vacío devuelve lista vacía sin llamar al repositorio")
    void emptyQuery_returnsEmpty() {
        List<Zone> result = useCase.execute(new SearchZonesPort.Query("  ", List.of(), 10));
        assertThat(result).isEmpty();
        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("limit se trunca a 50 para evitar abuso")
    void limit_cappedAt50() {
        when(repo.searchByText(anyString(), anyList(), eq(50))).thenReturn(List.of());
        useCase.execute(new SearchZonesPort.Query("Granada", List.of(), 999));
        verify(repo).searchByText("Granada", List.of(), 50);
    }

    @Test
    @DisplayName("Devuelve los resultados del repositorio")
    void returnsRepositoryResults() {
        Zone zone = Zone.create(UUID.randomUUID(), "ES-AN-GR", "Granada",
                ZoneType.PROVINCE, null, new GeoPoint(37.17, -3.59), "sys");
        when(repo.searchByText(eq("Granada"), anyList(), anyInt())).thenReturn(List.of(zone));

        List<Zone> result = useCase.execute(new SearchZonesPort.Query("Granada", List.of(), 10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Granada");
    }
}
