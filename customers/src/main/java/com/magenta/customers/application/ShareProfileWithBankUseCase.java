package com.magenta.customers.application;

import com.magenta.customers.domain.model.*;
import com.magenta.customers.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * UC-C11: Compartir perfil financiero con banco (requiere consentimiento expreso).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShareProfileWithBankUseCase {

    private final CustomerRepository customerRepository;
    private final FinancialSnapshotRepository snapshotRepository;
    private final ConsentRepository consentRepository;

    public record Command(UUID customerId, UUID bankId) {}

    public FinancialSnapshot execute(Command cmd) {
        Customer customer = customerRepository.findById(cmd.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));

        // Verificar consentimiento expreso
        if (!consentRepository.hasActiveConsent(cmd.customerId(), "share_with_bank")) {
            throw new IllegalStateException(
                    "Customer has not granted consent for share_with_bank");
        }

        // Verificar KYC
        if (customer.getKyc() == null || customer.getKyc().getStatus() != KycStatus.VERIFIED) {
            throw new IllegalStateException("KYC must be VERIFIED before sharing profile");
        }

        return snapshotRepository.findLatestByCustomerId(cmd.customerId())
                .orElseThrow(() -> new IllegalStateException(
                        "No financial snapshot available for customer " + cmd.customerId()));
    }
}
