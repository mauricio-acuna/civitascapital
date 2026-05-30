package com.magenta.products.application;

import com.magenta.products.domain.model.*;
import com.magenta.products.domain.port.out.*;
import com.magenta.products.domain.event.TransactionRegistered;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * UC-P9: Registrar transacción cerrada.
 */
@Service
@Transactional
public class RegisterTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final PropertyRepository propertyRepository;
    private final DomainEventPublisher eventPublisher;

    public RegisterTransactionUseCase(TransactionRepository transactionRepository,
                                       PropertyRepository propertyRepository,
                                       DomainEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.propertyRepository = propertyRepository;
        this.eventPublisher = eventPublisher;
    }

    public Transaction execute(Command cmd) {
        Property property = propertyRepository.findById(cmd.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(cmd.propertyId()));

        BigDecimal pricePerSqm = cmd.finalPrice()
                .divide(property.surface().builtSqm(), 2, RoundingMode.HALF_UP);

        Instant now = Instant.now();
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                property.tenantId(),
                cmd.propertyId(),
                cmd.operationId(),
                cmd.type(),
                cmd.finalPrice(),
                cmd.currency(),
                property.surface().builtSqm(),
                pricePerSqm,
                cmd.buyerCustomerId(),
                cmd.sellerCustomerId(),
                cmd.bankProductId(),
                cmd.mortgageAmount(),
                cmd.ltv(),
                cmd.closedAt(),
                cmd.deedNotaryProtocol(),
                TransactionSource.PLATFORM,
                now);

        Transaction saved = transactionRepository.save(tx);

        // Update property status
        if (cmd.type() == OperationType.SALE) {
            property.setStatus(PropertyStatus.SOLD);
        } else if (cmd.type() == OperationType.RENT) {
            property.setStatus(PropertyStatus.RENTED);
        }
        property.update(cmd.registeredBy());
        propertyRepository.save(property);

        eventPublisher.publish(new TransactionRegistered(
                saved.id(), saved.propertyId(), saved.tenantId(),
                saved.finalPrice(), saved.currency(),
                saved.pricePerSqm(), now));
        return saved;
    }

    public record Command(
            UUID propertyId,
            UUID operationId,
            OperationType type,
            BigDecimal finalPrice,
            String currency,
            UUID buyerCustomerId,
            UUID sellerCustomerId,
            UUID bankProductId,
            BigDecimal mortgageAmount,
            BigDecimal ltv,
            java.time.LocalDate closedAt,
            String deedNotaryProtocol,
            String registeredBy) {}
}
