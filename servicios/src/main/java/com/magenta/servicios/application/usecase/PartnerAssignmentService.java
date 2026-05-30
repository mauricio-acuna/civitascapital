package com.magenta.servicios.application.usecase;

import com.magenta.servicios.domain.model.Partner;
import com.magenta.servicios.domain.model.ServiceCode;
import com.magenta.servicios.domain.port.out.PartnerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Reglas de asignación de partner según MODULE-SPEC §10.1:
 * 1. Filtrar activos con service = serviceCode
 * 2. Filtrar por coverage_zone_ids ∋ zoneId
 * 3. Ordenar por: slaCompliance30d desc, npsScore desc, commissionPct asc (pesos configurables)
 * 4. Round-robin con weighting
 */
@Service
@Transactional(readOnly = true)
public class PartnerAssignmentService {

    private final PartnerRepository partnerRepository;

    @Value("${magenta.partners.weights.sla:0.5}")
    private double wSla;
    @Value("${magenta.partners.weights.nps:0.3}")
    private double wNps;
    @Value("${magenta.partners.weights.cost:0.2}")
    private double wCost;

    public PartnerAssignmentService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    public Optional<Partner> assign(ServiceCode serviceCode, UUID zoneId) {
        List<Partner> candidates = partnerRepository.findActiveByServiceAndZone(serviceCode, zoneId);

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        // Compute weighted score: higher is better
        return candidates.stream()
                .max(Comparator.comparingDouble(this::score));
    }

    private double score(Partner p) {
        double npsNorm = p.getNpsScore() != null ? (p.getNpsScore() / 100.0) : 0.5;
        double ratingNorm = p.getRating() != null ? p.getRating().doubleValue() / 5.0 : 0.5;
        double costNorm = p.getCommissionPct() != null
                ? 1.0 - (p.getCommissionPct().doubleValue() / 100.0)
                : 0.5;
        return wSla * ratingNorm + wNps * npsNorm + wCost * costNorm;
    }
}
