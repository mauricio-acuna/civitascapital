package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.model.bank.Bank;
import com.magenta.banks.domain.port.out.BankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListBanksUseCase {

    private final BankRepository bankRepository;

    public ListBanksUseCase(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    @Transactional(readOnly = true)
    public List<Bank> execute(UUID tenantId) {
        return bankRepository.findAllActive(tenantId);
    }
}
