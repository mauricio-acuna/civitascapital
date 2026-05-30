package com.magenta.banks.infrastructure.adapter.out.persistence.bank;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "bank_contact_channels", schema = "banks")
@Getter @Setter
public class BankContactChannelJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private BankJpaEntity bank;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false)
    private String value;

    @Column(length = 80)
    private String label;
}
