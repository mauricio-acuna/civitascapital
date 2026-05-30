package com.magenta.customers.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.customers.domain.model.*;
import com.magenta.customers.infrastructure.adapter.out.persistence.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Convierte entre entidades JPA y objetos de dominio.
 * No depende de Spring ni JPA en las firmas de método.
 */
@Component
@RequiredArgsConstructor
public class CustomerPersistenceMapper {

    private final ObjectMapper objectMapper;

    // ── Customer ────────────────────────────────────────────

    public Customer toDomain(CustomerJpaEntity e) {
        return Customer.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .type(CustomerType.valueOf(e.getType()))
                .displayName(e.getDisplayName())
                .status(CustomerStatus.valueOf(e.getStatus()))
                .keycloakUserId(e.getKeycloakUserId())
                .kyc(e.getKycState() != null ? kycToDomain(e.getKycState()) : null)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .version(e.getVersion())
                .individual(e.getIndividualProfile() != null ? individualToDomain(e.getIndividualProfile()) : null)
                .legalEntity(e.getLegalEntityProfile() != null ? legalEntityToDomain(e.getLegalEntityProfile()) : null)
                .household(e.getHousehold() != null ? householdToDomain(e.getHousehold()) : null)
                .build();
    }

    public CustomerJpaEntity toEntity(Customer d) {
        CustomerJpaEntity entity = CustomerJpaEntity.builder()
                .id(d.getId())
                .tenantId(d.getTenantId())
                .type(d.getType().name())
                .displayName(d.getDisplayName())
                .status(d.getStatus().name())
                .keycloakUserId(d.getKeycloakUserId())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .createdBy(d.getCreatedBy())
                .updatedBy(d.getUpdatedBy())
                .version(d.getVersion())
                .build();
        if (d.getIndividual() != null) {
            IndividualProfileJpaEntity ip = individualToEntity(d.getIndividual());
            ip.setCustomer(entity);
            entity.setIndividualProfile(ip);
        }
        if (d.getLegalEntity() != null) {
            LegalEntityProfileJpaEntity lep = legalEntityToEntity(d.getLegalEntity());
            lep.setCustomer(entity);
            entity.setLegalEntityProfile(lep);
        }
        if (d.getKyc() != null) {
            KycStateJpaEntity kyc = kycToEntity(d.getId(), d.getKyc());
            kyc.setCustomer(entity);
            entity.setKycState(kyc);
        }
        return entity;
    }

    // ── IndividualProfile ────────────────────────────────────

    @SneakyThrows
    public IndividualProfile individualToDomain(IndividualProfileJpaEntity e) {
        PostalAddress addr = e.getAddress() != null
                ? objectMapper.readValue(e.getAddress(), PostalAddress.class) : null;
        ProfessionalProfile prof = e.getProfessional() != null
                ? objectMapper.readValue(e.getProfessional(), ProfessionalProfile.class) : null;
        return IndividualProfile.builder()
                .nif(null)   // NIF no se devuelve desencriptado aquí; lo hace el servicio PII
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .birthDate(e.getBirthDate())
                .nationality(e.getNationality())
                .residenceCountry(e.getResidenceCountry())
                .taxResidence(e.getTaxResidence())
                .civilStatus(e.getCivilStatus())
                .address(addr)
                .zoneId(e.getZoneId())
                .professional(prof)
                .build();
    }

    @SneakyThrows
    public IndividualProfileJpaEntity individualToEntity(IndividualProfile d) {
        return IndividualProfileJpaEntity.builder()
                .firstName(d.getFirstName())
                .lastName(d.getLastName())
                .birthDate(d.getBirthDate())
                .nationality(d.getNationality() != null ? d.getNationality() : "ES")
                .residenceCountry(d.getResidenceCountry() != null ? d.getResidenceCountry() : "ES")
                .taxResidence(d.getTaxResidence() != null ? d.getTaxResidence() : "ES")
                .civilStatus(d.getCivilStatus())
                .address(d.getAddress() != null ? objectMapper.writeValueAsString(d.getAddress()) : null)
                .zoneId(d.getZoneId())
                .professional(d.getProfessional() != null ? objectMapper.writeValueAsString(d.getProfessional()) : null)
                .build();
    }

    // ── LegalEntityProfile ───────────────────────────────────

    @SneakyThrows
    public LegalEntityProfile legalEntityToDomain(LegalEntityProfileJpaEntity e) {
        PostalAddress addr = e.getAddress() != null
                ? objectMapper.readValue(e.getAddress(), PostalAddress.class) : null;
        List<UltimateBeneficialOwner> uboList = e.getUbo() != null
                ? objectMapper.readValue(e.getUbo(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, UltimateBeneficialOwner.class))
                : List.of();
        return LegalEntityProfile.builder()
                .cif(e.getCif())
                .legalName(e.getLegalName())
                .tradeName(e.getTradeName())
                .legalForm(e.getLegalForm())
                .regMercantilNumber(e.getRegMercantilNumber())
                .foundedAt(e.getFoundedAt())
                .cnae(e.getCnae())
                .address(addr)
                .uboList(uboList)
                .build();
    }

    @SneakyThrows
    public LegalEntityProfileJpaEntity legalEntityToEntity(LegalEntityProfile d) {
        return LegalEntityProfileJpaEntity.builder()
                .cif(d.getCif())
                .legalName(d.getLegalName())
                .tradeName(d.getTradeName())
                .legalForm(d.getLegalForm())
                .regMercantilNumber(d.getRegMercantilNumber())
                .foundedAt(d.getFoundedAt())
                .cnae(d.getCnae())
                .address(d.getAddress() != null ? objectMapper.writeValueAsString(d.getAddress()) : "{}")
                .ubo(d.getUboList() != null ? objectMapper.writeValueAsString(d.getUboList()) : "[]")
                .build();
    }

    // ── HouseholdProfile ─────────────────────────────────────

    public HouseholdProfile householdToDomain(HouseholdJpaEntity e) {
        List<HouseholdMember> members = e.getMembers().stream()
                .map(m -> HouseholdMember.builder()
                        .individualId(m.getIndividualId())
                        .role(HouseholdRole.valueOf(m.getRole()))
                        .ownershipPct(m.getOwnershipPct())
                        .build())
                .collect(Collectors.toList());
        return HouseholdProfile.builder()
                .members(members)
                .relationship(HouseholdProfile.Relationship.valueOf(e.getRelationship()))
                .dependentsCount(e.getDependentsCount())
                .build();
    }

    // ── KycState ─────────────────────────────────────────────

    @SneakyThrows
    public KycState kycToDomain(KycStateJpaEntity e) {
        Map<String, Boolean> checks = e.getChecks() != null
                ? objectMapper.readValue(e.getChecks(),
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Boolean.class))
                : Map.of();
        return KycState.builder()
                .status(KycStatus.valueOf(e.getStatus()))
                .provider(KycProvider.valueOf(e.getProvider()))
                .idDocumentType(e.getIdDocType())
                .score(e.getScore())
                .checks(checks)
                .verifiedAt(e.getVerifiedAt())
                .expiresAt(e.getExpiresAt())
                .providerRef(e.getProviderRef())
                .build();
    }

    @SneakyThrows
    public KycStateJpaEntity kycToEntity(UUID customerId, KycState d) {
        return KycStateJpaEntity.builder()
                .customerId(customerId)
                .status(d.getStatus().name())
                .provider(d.getProvider().name())
                .idDocType(d.getIdDocumentType())
                .score(d.getScore())
                .checks(objectMapper.writeValueAsString(d.getChecks() != null ? d.getChecks() : Map.of()))
                .verifiedAt(d.getVerifiedAt())
                .expiresAt(d.getExpiresAt())
                .providerRef(d.getProviderRef())
                .build();
    }
}
