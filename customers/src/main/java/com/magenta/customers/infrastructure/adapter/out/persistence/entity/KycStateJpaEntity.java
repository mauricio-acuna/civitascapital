package com.magenta.customers.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kyc_states", schema = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycStateJpaEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "customer_id")
    private CustomerJpaEntity customer;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(nullable = false, length = 40)
    private String provider;

    @Column(name = "id_doc_type", length = 16)
    private String idDocType;

    @Column(name = "id_doc_number_h", length = 64)
    private String idDocNumberH;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String checks;

    @Column
    private Integer score;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "provider_ref", length = 120)
    private String providerRef;
}
