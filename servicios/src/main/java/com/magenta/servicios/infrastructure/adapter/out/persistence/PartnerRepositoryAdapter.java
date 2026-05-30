package com.magenta.servicios.infrastructure.adapter.out.persistence;

import com.magenta.servicios.domain.model.Partner;
import com.magenta.servicios.domain.model.PartnerKind;
import com.magenta.servicios.domain.model.ServiceCode;
import com.magenta.servicios.domain.port.out.PartnerRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PartnerRepositoryAdapter implements PartnerRepository {

    private final PartnerJpaRepository jpa;

    public PartnerRepositoryAdapter(PartnerJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Partner save(Partner partner) {
        PartnerJpaEntity entity = toEntity(partner);
        entity.setUpdatedAt(Instant.now());
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Partner> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Partner> findActiveByServiceAndZone(ServiceCode serviceCode, UUID zoneId) {
        return jpa.findActiveByServiceAndZone(serviceCode.name(), zoneId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Partner> findAll(UUID tenantId) {
        return jpa.findByTenantIdAndActiveTrue(tenantId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // ── Mapeo dominio → entidad ───────────────────────────────────────────────

    private PartnerJpaEntity toEntity(Partner p) {
        PartnerJpaEntity e = new PartnerJpaEntity();
        e.setId(p.getId());
        e.setTenantId(p.getTenantId());
        e.setCode(p.getCode());
        e.setName(p.getName());
        e.setKind(p.getKind().name());
        e.setServices(p.getServices().stream()
                .map(ServiceCode::name)
                .collect(Collectors.toList()));
        e.setCoverageZoneIds(new ArrayList<>(p.getCoverageZoneIds()));
        e.setCommissionPct(p.getCommissionPct());
        e.setRating(p.getRating());
        e.setNpsScore(p.getNpsScore());
        e.setActive(p.isActive());
        e.setContractRef(p.getContractRef());
        e.setCreatedAt(p.getCreatedAt());
        return e;
    }

    // ── Mapeo entidad → dominio ───────────────────────────────────────────────

    private Partner toDomain(PartnerJpaEntity e) {
        Set<ServiceCode> services = e.getServices().stream()
                .map(ServiceCode::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<UUID> zones = new LinkedHashSet<>(
                e.getCoverageZoneIds() != null ? e.getCoverageZoneIds() : List.of());

        return new Partner(
                e.getId(),
                e.getTenantId(),
                e.getCode(),
                e.getName(),
                PartnerKind.valueOf(e.getKind()),
                services,
                zones,
                e.getCommissionPct(),
                e.getRating(),
                e.getNpsScore(),
                e.isActive(),
                e.getContractRef(),
                e.getCreatedAt()
        );
    }
}
