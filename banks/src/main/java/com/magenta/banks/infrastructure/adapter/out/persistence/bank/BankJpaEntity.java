package com.magenta.banks.infrastructure.adapter.out.persistence.bank;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "banks", schema = "banks")
@Getter @Setter
public class BankJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, unique = true, length = 11)
    private String code;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 80)
    private String brand;

    @Column(nullable = false, length = 2)
    private String country;

    @Column(name = "bde_registry_nr", length = 20)
    private String bdeRegistryNumber;

    @Column(length = 4)
    private String rating;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 64)
    private String updatedBy;

    @Version
    private long version;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @OneToMany(mappedBy = "bank", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BankContactChannelJpaEntity> contactChannels = new ArrayList<>();
}
