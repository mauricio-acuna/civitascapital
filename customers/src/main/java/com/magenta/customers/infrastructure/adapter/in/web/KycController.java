package com.magenta.customers.infrastructure.adapter.in.web;

import com.magenta.customers.application.*;
import com.magenta.customers.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "KYC", description = "Verificación de identidad")
@RequiredArgsConstructor
public class KycController {

    private final StartKycUseCase startKyc;
    private final ProcessKycCallbackUseCase processCallback;
    private final com.magenta.customers.domain.port.out.CustomerRepository customerRepo;

    @PostMapping("/{id}/kyc/start")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "UC-C6: Iniciar sesión KYC")
    public StartKycUseCase.Result startKyc(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "/kyc/done") String returnUrl) {
        return startKyc.execute(new StartKycUseCase.Command(id, returnUrl));
    }

    /**
     * Callback del proveedor KYC — autenticado por HMAC, no por JWT.
     */
    @PostMapping("/{id}/kyc/callback")
    @Operation(summary = "UC-C6: Callback KYC del proveedor (HMAC firmado)")
    public CustomerResponse kycCallback(
            @PathVariable UUID id,
            @RequestBody KycCallbackRequest req,
            @RequestHeader("X-KYC-Signature") String signature,
            @RequestHeader("X-KYC-Timestamp") long timestamp) {

        Customer updated = processCallback.execute(new ProcessKycCallbackUseCase.Command(
                id, req.rawPayload(), signature, timestamp,
                req.providerRef(), req.documentAuthentic(), req.livenessOk(),
                req.sanctionsClean(), req.pepFlag(), req.addressVerified(),
                req.score(), req.expiresAt()));
        return CustomerResponse.from(updated);
    }

    @GetMapping("/{id}/kyc")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('BANK_OFFICER') or hasRole('ADMIN')")
    @Operation(summary = "Obtener estado KYC del cliente")
    public KycState getKyc(@PathVariable UUID id) {
        return customerRepo.findById(id)
                .map(c -> c.getKyc())
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    public record KycCallbackRequest(
            byte[] rawPayload,
            String providerRef,
            boolean documentAuthentic,
            boolean livenessOk,
            boolean sanctionsClean,
            boolean pepFlag,
            boolean addressVerified,
            Integer score,
            Instant expiresAt
    ) {}
}
