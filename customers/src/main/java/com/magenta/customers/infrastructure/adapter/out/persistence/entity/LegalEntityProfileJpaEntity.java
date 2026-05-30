package com.magenta.customers.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "legal_entity_profiles", schema = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegalEntityProfileJpaEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "customer_id")
    private CustomerJpaEntity customer;

    @Column(nullable = false, length = 12, unique = true)
    private String cif;

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "trade_name", length = 200)
    private String tradeName;

    @Column(name = "legal_form", nullable = false, length = 20)
    private String legalForm;

    @Column(name = "reg_mercantil_number", length = 40)
    private String regMercantilNumber;

    @Column(name = "founded_at")
    private LocalDate foundedAt;

    @Column(length = 6)
    private String cnae;

    @Column(name = "representative_nif_h", length = 64)
    private String representativeNifH;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String address;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String ubo;
}
