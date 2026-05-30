package com.magenta.banks.infrastructure.adapter.out.persistence.preapproval;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "preapproval_events", schema = "banks")
@Getter
@Setter
public class PreapprovalEventJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "preapproval_id", nullable = false)
    private UUID preapprovalId;

    @Column(name = "from_status", length = 16)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 16)
    private String toStatus;

    @Column
    private String reason;

    @Column(nullable = false, length = 64)
    private String actor;

    @Column(name = "at", nullable = false)
    private Instant at;
}
