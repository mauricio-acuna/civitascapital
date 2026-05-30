package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.model.bank.Bank;
import com.magenta.banks.domain.port.out.BankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class GetBankUseCase {

    private final BankRepository bankRepository;

    public GetBankUseCase(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    @Transactional(readOnly = true)
    public Bank execute(UUID id) {
        return bankRepository.findById(id)
               .orElseThrow(() -> new NoSuchElementException("Bank not found: " + id));
    }
}
