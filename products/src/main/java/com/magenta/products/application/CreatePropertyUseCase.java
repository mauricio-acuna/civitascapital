package com.magenta.products.application;

import com.magenta.products.domain.model.*;
import com.magenta.products.domain.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * UC-P1: Alta de inmueble con georreferenciación (lat/lng → zoneId).
 */
@Service
@Transactional
public class CreatePropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final ZoneResolverPort zoneResolverPort;
    private final DomainEventPublisher eventPublisher;

    public CreatePropertyUseCase(PropertyRepository propertyRepository,
                                  ZoneResolverPort zoneResolverPort,
                                  DomainEventPublisher eventPublisher) {
        this.propertyRepository = propertyRepository;
        this.zoneResolverPort = zoneResolverPort;
        this.eventPublisher = eventPublisher;
    }

    public Property execute(Command cmd) {
        if (propertyRepository.existsByTenantIdAndReference(cmd.tenantId(), cmd.reference())) {
            throw new IllegalArgumentException("Reference already exists: " + cmd.reference());
        }

        UUID zoneId = zoneResolverPort.resolveZone(cmd.coordinates());
        if (zoneId == null) {
            throw new IllegalStateException("Cannot resolve zone for coordinates: " + cmd.coordinates());
        }

        Location location = new Location(
                cmd.street(), cmd.streetNumber(), cmd.floor(), cmd.door(),
                cmd.postalCode(), cmd.coordinates(), zoneId, null,
                LocationVisibility.NEIGHBORHOOD_ONLY);

        Property property = Property.create(
                UUID.randomUUID(),
                cmd.tenantId(),
                cmd.reference(),
                cmd.type(),
                cmd.subtype(),
                cmd.ownership(),
                location,
                cmd.surface(),
                cmd.layout(),
                cmd.condition(),
                cmd.buildYear(),
                cmd.energyRating(),
                cmd.features(),
                cmd.orientation(),
                cmd.tags(),
                cmd.createdBy());

        Property saved = propertyRepository.save(property);
        saved.pullDomainEvents().forEach(eventPublisher::publish);
        return saved;
    }

    public record Command(
            UUID tenantId,
            String reference,
            PropertyType type,
            String subtype,
            OwnerInfo ownership,
            String street,
            String streetNumber,
            String floor,
            String door,
            String postalCode,
            GeoPoint coordinates,
            Surface surface,
            Layout layout,
            PropertyCondition condition,
            Integer buildYear,
            EnergyRating energyRating,
            Set<String> features,
            Set<Orientation> orientation,
            Set<String> tags,
            String createdBy) {}
}
