package com.magenta.customers.infrastructure.adapter.in.web;

import com.magenta.customers.application.*;
import com.magenta.customers.domain.model.*;
import com.magenta.customers.domain.port.out.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers", description = "Gestión de clientes")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final RegisterLegalEntityUseCase registerLegalEntity;
    private final CreateHouseholdUseCase createHousehold;

    // ── GET /customers/{id} ──────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or #id.toString() == authentication.name")
    @Operation(summary = "Obtener cliente por ID")
    public CustomerResponse getById(@PathVariable UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return CustomerResponse.from(customer);
    }

    // ── POST /customers/legal-entities ──────────────────────

    public record RegisterLegalEntityRequest(
            String cif, String legalName, String tradeName,
            String legalForm, String regMercantilNumber,
            LocalDate foundedAt, String cnae, String representativeNif,
            PostalAddress address, List<UltimateBeneficialOwner> uboList
    ) {}

    @PostMapping("/legal-entities")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "UC-C2: Registrar persona jurídica")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse registerLegalEntity(
            @Valid @RequestBody RegisterLegalEntityRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        Customer c = registerLegalEntity.execute(new RegisterLegalEntityUseCase.Command(
                tenantId(jwt), req.cif(), req.legalName(), req.tradeName(),
                req.legalForm(), req.regMercantilNumber(), req.foundedAt(),
                req.cnae(), req.representativeNif(), req.address(),
                req.uboList(), jwt.getSubject()));
        return CustomerResponse.from(c);
    }

    // ── POST /customers/households ───────────────────────────

    public record CreateHouseholdRequest(
            String displayName,
            HouseholdProfile.Relationship relationship,
            int dependentsCount,
            List<HouseholdMember> members
    ) {}

    @PostMapping("/households")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "UC-C3: Crear unidad familiar")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createHousehold(
            @Valid @RequestBody CreateHouseholdRequest req,
            @AuthenticationPrincipal Jwt jwt) {

        Customer c = createHousehold.execute(new CreateHouseholdUseCase.Command(
                tenantId(jwt), req.displayName(), req.relationship(),
                req.dependentsCount(), req.members(), jwt.getSubject()));
        return CustomerResponse.from(c);
    }

    private UUID tenantId(Jwt jwt) {
        String tid = jwt.getClaimAsString("tenant_id");
        return tid != null ? UUID.fromString(tid) : UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
