package com.magenta.products.application;

import com.magenta.products.domain.model.Property;
import com.magenta.products.domain.port.out.PropertyRepository;
import com.magenta.products.domain.service.PropertyAffordabilityMatchService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Civitas Pro MVP: devuelve inmuebles ordenados por encaje financiero.
 */
@Service
public class MatchAffordablePropertiesUseCase {

    private final PropertyRepository propertyRepository;
    private final PropertyAffordabilityMatchService matchService = new PropertyAffordabilityMatchService();

    public MatchAffordablePropertiesUseCase(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<PropertyAffordabilityMatchService.Match> execute(Command command) {
        Set<Property> candidates = new LinkedHashSet<>();
        if (command.zoneIds() != null) {
            command.zoneIds().forEach(zoneId -> candidates.addAll(
                    propertyRepository.findByTenantIdAndZoneId(command.tenantId(), zoneId)));
        }

        return matchService.match(
                new PropertyAffordabilityMatchService.Query(
                        command.tenantId(),
                        command.maxTicket(),
                        command.zoneIds(),
                        command.roomsMin()),
                new ArrayList<>(candidates));
    }

    public record Command(
            UUID tenantId,
            BigDecimal maxTicket,
            Set<UUID> zoneIds,
            Integer roomsMin
    ) {}
}
