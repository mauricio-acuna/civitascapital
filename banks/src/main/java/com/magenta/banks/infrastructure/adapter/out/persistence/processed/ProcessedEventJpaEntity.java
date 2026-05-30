package com.magenta.banks.infrastructure.adapter.out.persistence.processed;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "processed_event", schema = "banks")
@Getter
@Setter
public class ProcessedEventJpaEntity {

    @EmbeddedId
    private ProcessedEventId id;

    @Column(name = "topic", nullable = false, length = 120)
    private String topic;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}
