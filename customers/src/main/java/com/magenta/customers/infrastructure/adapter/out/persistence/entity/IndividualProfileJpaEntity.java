package com.magenta.customers.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "individual_profiles", schema = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualProfileJpaEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "customer_id")
    private CustomerJpaEntity customer;

    @Column(name = "nif_encrypted", nullable = false)
    private byte[] nifEncrypted;

    @Column(name = "nif_hash", nullable = false, length = 64, unique = true)
    private String nifHash;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 160)
    private String lastName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 2)
    private String nationality;

    @Column(name = "residence_country", nullable = false, length = 2)
    private String residenceCountry;

    @Column(name = "tax_residence", nullable = false, length = 2)
    private String taxResidence;

    @Column(name = "civil_status", length = 20)
    private String civilStatus;

    @Column(name = "phone_encrypted")
    private byte[] phoneEncrypted;

    @Column(name = "email_encrypted")
    private byte[] emailEncrypted;

    @Column(name = "email_hash", length = 64, unique = true)
    private String emailHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String address;

    @Column(name = "zone_id")
    private UUID zoneId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String professional;
}
