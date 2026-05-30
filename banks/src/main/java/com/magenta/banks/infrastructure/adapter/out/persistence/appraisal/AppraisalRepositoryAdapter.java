package com.magenta.banks.infrastructure.adapter.out.persistence.appraisal;

import com.magenta.banks.domain.model.appraisal.Appraisal;
import com.magenta.banks.domain.port.out.AppraisalRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AppraisalRepositoryAdapter implements AppraisalRepository {

    private final AppraisalJpaRepository jpaRepository;

    public AppraisalRepositoryAdapter(AppraisalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Appraisal save(Appraisal appraisal) {
        return toDomain(jpaRepository.save(toEntity(appraisal)));
    }

    @Override
    public Optional<Appraisal> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Appraisal> findByPropertyId(UUID propertyId) {
        return jpaRepository.findByPropertyId(propertyId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Appraisal> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerId(customerId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Appraisal> findLatestValidByPropertyId(UUID propertyId) {
        return jpaRepository.findFirstByPropertyIdAndValidUntilAfterOrderByIssuedAtDesc(propertyId, LocalDate.now())
                .map(this::toDomain);
    }

    private AppraisalJpaEntity toEntity(Appraisal appraisal) {
        AppraisalJpaEntity entity = new AppraisalJpaEntity();
        entity.setId(appraisal.id());
        entity.setTenantId(appraisal.tenantId());
        entity.setPropertyId(appraisal.propertyId());
        entity.setCustomerId(appraisal.customerId());
        entity.setProviderId(appraisal.providerId());
        entity.setRegulation(appraisal.regulation());
        entity.setMarketValue(appraisal.marketValue());
        entity.setMortgageValue(appraisal.mortgageValue());
        entity.setSurfaceSqm(appraisal.surfaceSqm());
        entity.setIssuedAt(appraisal.issuedAt());
        entity.setValidUntil(appraisal.validUntil());
        entity.setPdfUrl(appraisal.pdfUrl());
        entity.setCreatedAt(appraisal.createdAt());
        return entity;
    }

    private Appraisal toDomain(AppraisalJpaEntity entity) {
        return new Appraisal(
                entity.getId(),
                entity.getTenantId(),
                entity.getPropertyId(),
                entity.getCustomerId(),
                entity.getProviderId(),
                entity.getRegulation(),
                entity.getMarketValue(),
                entity.getMortgageValue(),
                entity.getSurfaceSqm(),
                entity.getIssuedAt(),
                entity.getValidUntil(),
                entity.getPdfUrl(),
                List.of(),
                entity.getCreatedAt());
    }
}
