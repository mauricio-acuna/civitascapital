package com.magenta.customers.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "household_members", schema = "customers")
@IdClass(HouseholdMemberJpaEntity.HouseholdMemberId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseholdMemberJpaEntity {

    @Id
    @Column(name = "household_id", nullable = false)
    private UUID householdId;

    @Id
    @Column(name = "individual_id", nullable = false)
    private UUID individualId;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "ownership_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal ownershipPct;

    // Composite PK class
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class HouseholdMemberId implements java.io.Serializable {
        private UUID householdId;
        private UUID individualId;
    }
}
