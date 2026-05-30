package com.magenta.banks.infrastructure.adapter.out.persistence.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class IdempotencyRecordId implements Serializable {

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    public IdempotencyRecordId(UUID tenantId, String idempotencyKey) {
        this.tenantId = tenantId;
        this.idempotencyKey = idempotencyKey;
    }
}
