package com.magenta.banks.infrastructure.adapter.out.persistence.preapproval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.banks.domain.model.PreapprovalStatus;
import com.magenta.banks.domain.model.pagination.PageResult;
import com.magenta.banks.domain.model.pagination.PageSpec;
import com.magenta.banks.domain.model.preapproval.Preapproval;
import com.magenta.banks.domain.model.preapproval.StatusChange;
import com.magenta.banks.domain.port.out.PreapprovalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PreapprovalRepositoryAdapter implements PreapprovalRepository {

    private final PreapprovalJpaRepository preapprovalRepository;
    private final PreapprovalEventJpaRepository eventRepository;
    private final ObjectMapper objectMapper;

    public PreapprovalRepositoryAdapter(PreapprovalJpaRepository preapprovalRepository,
                                        PreapprovalEventJpaRepository eventRepository,
                                        ObjectMapper objectMapper) {
        this.preapprovalRepository = preapprovalRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Preapproval save(Preapproval preapproval) {
        PreapprovalJpaEntity saved = preapprovalRepository.save(toEntity(preapproval));
        preapproval.history().stream()
                .filter(change -> !eventRepository.existsById(change.id()))
                .map(change -> toEventEntity(preapproval.id(), change))
                .forEach(eventRepository::save);
        return toDomain(saved);
    }

    @Override
    public Optional<Preapproval> findById(UUID id) {
        return preapprovalRepository.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<Preapproval> findByCustomerId(UUID customerId, PageSpec page) {
        Pageable pageable = PageRequest.of(page.page(), page.size());
        Page<Preapproval> result = preapprovalRepository.findByCustomerId(customerId, pageable).map(this::toDomain);
        return new PageResult<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public List<Preapproval> findExpired() {
        return preapprovalRepository.findExpiredApproved(Instant.now()).stream()
                .map(this::toDomain)
                .toList();
    }

    private PreapprovalJpaEntity toEntity(Preapproval preapproval) {
        PreapprovalJpaEntity entity = new PreapprovalJpaEntity();
        entity.setId(preapproval.id());
        entity.setTenantId(preapproval.tenantId());
        entity.setCustomerId(preapproval.customerId());
        entity.setProductId(preapproval.productId());
        entity.setPropertyId(preapproval.propertyId());
        entity.setAmount(preapproval.amount());
        entity.setTermMonths(preapproval.termMonths());
        entity.setLtv(preapproval.ltv());
        entity.setStatus(preapproval.status().name());
        entity.setConditions(toJson(preapproval.conditions()));
        entity.setExpiresAt(preapproval.expiresAt());
        entity.setCreatedAt(preapproval.createdAt());
        entity.setUpdatedAt(preapproval.updatedAt());
        entity.setVersion(preapproval.version());
        return entity;
    }

    private Preapproval toDomain(PreapprovalJpaEntity entity) {
        return new Preapproval(
                entity.getId(),
                entity.getTenantId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getPropertyId(),
                entity.getAmount(),
                entity.getTermMonths(),
                entity.getLtv(),
                PreapprovalStatus.valueOf(entity.getStatus()),
                fromJson(entity.getConditions()),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                eventRepository.findByPreapprovalIdOrderByAtAsc(entity.getId()).stream()
                        .map(this::toStatusChange)
                        .toList(),
                entity.getVersion());
    }

    private PreapprovalEventJpaEntity toEventEntity(UUID preapprovalId, StatusChange change) {
        PreapprovalEventJpaEntity entity = new PreapprovalEventJpaEntity();
        entity.setId(change.id());
        entity.setPreapprovalId(preapprovalId);
        entity.setFromStatus(change.fromStatus());
        entity.setToStatus(change.toStatus());
        entity.setReason(change.reason());
        entity.setActor(change.actor());
        entity.setAt(change.at());
        return entity;
    }

    private StatusChange toStatusChange(PreapprovalEventJpaEntity entity) {
        return new StatusChange(
                entity.getId(),
                entity.getFromStatus(),
                entity.getToStatus(),
                entity.getReason(),
                entity.getActor(),
                entity.getAt());
    }

    private String toJson(List<String> conditions) {
        try {
            return objectMapper.writeValueAsString(conditions);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot serialize preapproval conditions", ex);
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot deserialize preapproval conditions", ex);
        }
    }
}
