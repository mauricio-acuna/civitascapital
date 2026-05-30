package com.magenta.customers.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_preferences", schema = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchPreferenceJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerJpaEntity customer;

    @Column(name = "operation_type", nullable = false, length = 16)
    private String operationType;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "property_types", columnDefinition = "text[]")
    private String[] propertyTypes;

    @Column(name = "price_min", precision = 14, scale = 2)
    private BigDecimal priceMin;

    @Column(name = "price_max", precision = 14, scale = 2)
    private BigDecimal priceMax;

    @Column(name = "surface_min")
    private Integer surfaceMin;

    @Column(name = "rooms_min")
    private Short roomsMin;

    @Column(name = "bathrooms_min")
    private Short bathroomsMin;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "zone_ids", columnDefinition = "uuid[]")
    private UUID[] zoneIds;

    @Column(name = "requires_fiber", nullable = false)
    private boolean requiresFiber;

    @Column(name = "max_risk_occup")
    private Integer maxRiskOccup;

    @Column(name = "alert_channel", nullable = false, length = 8)
    private String alertChannel;

    @Column(name = "alert_frequency", nullable = false, length = 10)
    private String alertFrequency;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }
}
