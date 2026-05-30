package com.magenta.banks.domain.port.out;

import com.magenta.banks.domain.model.bank.Bank;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankRepository {
    Bank save(Bank bank);
    Optional<Bank> findById(UUID id);
    List<Bank> findAllActive(UUID tenantId);
    boolean existsByCode(String code);
}
