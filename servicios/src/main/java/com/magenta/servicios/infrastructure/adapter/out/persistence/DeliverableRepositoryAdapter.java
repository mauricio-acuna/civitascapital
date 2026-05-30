package com.magenta.servicios.infrastructure.adapter.out.persistence;

import com.magenta.servicios.domain.model.Deliverable;
import com.magenta.servicios.domain.model.DeliverableKind;
import com.magenta.servicios.domain.port.out.DeliverableRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DeliverableRepositoryAdapter implements DeliverableRepository {

    private final DeliverableJpaRepository jpa;

    public DeliverableRepositoryAdapter(DeliverableJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Deliverable save(Deliverable deliverable) {
        return toDomain(jpa.save(toEntity(deliverable)));
    }

    @Override
    public Optional<Deliverable> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Deliverable> findByOrderId(UUID orderId) {
        return jpa.findByOrderId(orderId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private DeliverableJpaEntity toEntity(Deliverable d) {
        DeliverableJpaEntity e = new DeliverableJpaEntity();
        e.setId(d.getId());
        e.setOrderId(d.getOrderId());
        e.setKind(d.getKind().name());
        e.setStorageUri(d.getStorageUri());
        e.setSha256(d.getSha256());
        e.setSignedBy(d.getSignedBy());
        e.setIssuedAt(d.getIssuedAt() != null ? d.getIssuedAt() : Instant.now());
        return e;
    }

    private Deliverable toDomain(DeliverableJpaEntity e) {
        return new Deliverable(
                e.getId(),
                e.getOrderId(),
                DeliverableKind.valueOf(e.getKind()),
                e.getStorageUri(),
                e.getSha256(),
                e.getSignedBy(),
                e.getIssuedAt()
        );
    }
}
