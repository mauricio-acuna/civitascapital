package com.magenta.banks.application.usecase;

import com.magenta.banks.domain.model.appraisal.Appraisal;
import com.magenta.banks.domain.port.out.AppraisalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class GetAppraisalUseCase {

    private final AppraisalRepository appraisalRepository;

    public GetAppraisalUseCase(AppraisalRepository appraisalRepository) {
        this.appraisalRepository = appraisalRepository;
    }

    @Transactional(readOnly = true)
    public Appraisal byId(UUID appraisalId) {
        return appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new NoSuchElementException("Appraisal not found: " + appraisalId));
    }

    @Transactional(readOnly = true)
    public List<Appraisal> byProperty(UUID propertyId) {
        return appraisalRepository.findByPropertyId(propertyId);
    }

    @Transactional(readOnly = true)
    public List<Appraisal> byCustomer(UUID customerId) {
        return appraisalRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Appraisal latestValidByProperty(UUID propertyId) {
        return appraisalRepository.findLatestValidByPropertyId(propertyId)
                .orElseThrow(() -> new NoSuchElementException("No valid appraisal found for property: " + propertyId));
    }
}
