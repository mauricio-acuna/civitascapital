package com.magenta.products.application;

import com.magenta.products.domain.model.*;
import com.magenta.products.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublishPropertyUseCaseTest {

    @Mock PropertyRepository propertyRepository;
    @Mock FinancingPort financingPort;
    @Mock DomainEventPublisher eventPublisher;
    @Mock SearchIndexPort searchIndexPort;

    @InjectMocks
    PublishPropertyUseCase useCase;

    private UUID propertyId;
    private Property property;

    @BeforeEach
    void setUp() {
        propertyId = UUID.randomUUID();
        property = buildPublishableProperty(propertyId);
    }

    @Test
    void publish_happy_path_sets_active_status_and_indexes() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(financingPort.evaluateFeasibility(any(), any())).thenReturn(FinancingHint.empty());
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Property result = useCase.execute(propertyId, "agent1");

        assertThat(result.status()).isEqualTo(PropertyStatus.ACTIVE);
        verify(searchIndexPort).index(any());
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    void publish_throws_when_property_not_found() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(propertyId, "agent"))
                .isInstanceOf(PropertyNotFoundException.class);
    }

    @Test
    void publish_throws_when_invariants_violated() {
        // Property with no photos
        Property bare = buildBareProperty(propertyId);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(bare));

        assertThatThrownBy(() -> useCase.execute(propertyId, "agent"))
                .isInstanceOf(PropertyPublishException.class);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Property buildPublishableProperty(UUID id) {
        Property p = Property.create(
                id, UUID.randomUUID(), "REF-TEST", PropertyType.FLAT, null,
                new OwnerInfo("PRIVATE", null, "Owner"),
                new Location("C/ Test", "1", null, null, "28001",
                        new GeoPoint(40.4, -3.7), UUID.randomUUID(), null,
                        LocationVisibility.NEIGHBORHOOD_ONLY),
                new Surface(new BigDecimal("80"), null, null),
                new Layout(3, 1, null, null, null, 2, true),
                PropertyCondition.GOOD, 2000,
                new EnergyRating(EnergyLetter.D, 150.0, EnergyLetter.D, 30.0, "CERT-01", null),
                Set.of(), Set.of(), Set.of(), "agent1");

        for (int i = 0; i < 3; i++) {
            p.addMedia(new MediaAsset(UUID.randomUUID(), id, MediaKind.PHOTO,
                    "s3://b/p" + i, "image/jpeg", 1024L, 800, 600, List.of(), i, i == 0,
                    java.time.Instant.now()));
        }
        p.addOperation(new Operation(UUID.randomUUID(), id, OperationType.SALE,
                Money.euros(new BigDecimal("100000")), null, null, null, null,
                true, null, null, OperationStatus.DRAFT, false, null));
        p.pullDomainEvents(); // consume creation event
        return p;
    }

    private Property buildBareProperty(UUID id) {
        Property p = Property.create(
                id, UUID.randomUUID(), "REF-BARE", PropertyType.FLAT, null,
                new OwnerInfo("PRIVATE", null, "Owner"),
                new Location("C/ Test", "1", null, null, "28001",
                        new GeoPoint(40.4, -3.7), UUID.randomUUID(), null,
                        LocationVisibility.NEIGHBORHOOD_ONLY),
                new Surface(new BigDecimal("80"), null, null),
                null, null, 2000,
                new EnergyRating(EnergyLetter.D, 150.0, EnergyLetter.D, 30.0, "CERT-01", null),
                Set.of(), Set.of(), Set.of(), "agent1");
        p.pullDomainEvents();
        return p;
    }
}
