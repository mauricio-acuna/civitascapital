package com.magenta.customers.application;

import com.magenta.customers.domain.model.Customer;
import com.magenta.customers.domain.port.out.CustomerRepository;
import com.magenta.customers.domain.port.out.KycProviderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * UC-C6 (parte 1): Iniciar sesión KYC con el proveedor externo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StartKycUseCase {

    private final CustomerRepository customerRepository;
    private final KycProviderPort kycProvider;

    public record Command(UUID customerId, String returnUrl) {}
    public record Result(String redirectUrl) {}

    public Result execute(Command cmd) {
        Customer customer = customerRepository.findById(cmd.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));

        String redirectUrl = kycProvider.startSession(customer.getId(), cmd.returnUrl());
        log.info("KYC session started for customer={}", cmd.customerId());
        return new Result(redirectUrl);
    }
}
