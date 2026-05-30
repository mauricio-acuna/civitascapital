package com.magenta.customers.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "households", schema = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseholdJpaEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "customer_id")
    private CustomerJpaEntity customer;

    @Column(nullable = false, length = 40)
    private String relationship;

    @Column(name = "dependents_count", nullable = false)
    private int dependentsCount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "household_id")
    @Builder.Default
    private List<HouseholdMemberJpaEntity> members = new ArrayList<>();
}
