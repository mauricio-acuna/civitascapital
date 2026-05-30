package com.magenta.servicios.domain;

import com.magenta.servicios.application.usecase.PartnerAssignmentService;
import com.magenta.servicios.domain.model.*;
import com.magenta.servicios.domain.port.out.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerAssignmentServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    private PartnerAssignmentService service;

    @BeforeEach
    void setUp() {
        service = new PartnerAssignmentService(partnerRepository);
    }

    @Test
    void shouldReturnBestPartnerByScore() {
        UUID zoneId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        Partner lowScorePartner = new Partner(UUID.randomUUID(), tenantId, "P001", "Partner Bajo", PartnerKind.INSURER,
                Set.of(ServiceCode.RENT_DEFAULT_INSURANCE), Set.of(zoneId),
                new BigDecimal("5.0"), new BigDecimal("3.0"), (short) 60, true, null, Instant.now());

        Partner highScorePartner = new Partner(UUID.randomUUID(), tenantId, "P002", "Partner Alto", PartnerKind.INSURER,
                Set.of(ServiceCode.RENT_DEFAULT_INSURANCE), Set.of(zoneId),
                new BigDecimal("2.0"), new BigDecimal("5.0"), (short) 90, true, null, Instant.now());

        when(partnerRepository.findActiveByServiceAndZone(any(), any()))
                .thenReturn(List.of(lowScorePartner, highScorePartner));

        Optional<Partner> result = service.assign(ServiceCode.RENT_DEFAULT_INSURANCE, zoneId);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("P002");
    }

    @Test
    void shouldReturnEmptyWhenNoPartnersAvailable() {
        when(partnerRepository.findActiveByServiceAndZone(any(), any())).thenReturn(List.of());

        Optional<Partner> result = service.assign(ServiceCode.APPRAISAL, UUID.randomUUID());

        assertThat(result).isEmpty();
    }
}
