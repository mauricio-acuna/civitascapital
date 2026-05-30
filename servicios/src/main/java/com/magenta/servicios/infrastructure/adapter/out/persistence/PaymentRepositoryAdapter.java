package com.magenta.servicios.infrastructure.adapter.out.persistence;

import com.magenta.servicios.domain.model.Payment;
import com.magenta.servicios.domain.port.out.PaymentRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpa;

    public PaymentRepositoryAdapter(PaymentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Payment save(Payment payment) {
        return toDomain(jpa.save(toEntity(payment)));
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Payment> findByOrderId(UUID orderId) {
        return jpa.findByOrderId(orderId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private PaymentJpaEntity toEntity(Payment p) {
        PaymentJpaEntity e = new PaymentJpaEntity();
        e.setId(p.getId());
        e.setOrderId(p.getOrderId());
        e.setDirection(p.getDirection().name());
        e.setAmount(p.getAmount());
        e.setCurrency(p.getCurrency());
        e.setMethod(p.getMethod().name());
        e.setProviderRef(p.getProviderRef());
        e.setStatus(p.getStatus().name());
        e.setVatPct(p.getVatPct());
        e.setInvoiceNumber(p.getInvoiceNumber());
        e.setAt(p.getAt() != null ? p.getAt() : Instant.now());
        return e;
    }

    private Payment toDomain(PaymentJpaEntity e) {
        return new Payment(
                e.getId(),
                e.getOrderId(),
                Payment.Direction.valueOf(e.getDirection()),
                e.getAmount(),
                e.getCurrency(),
                Payment.Method.valueOf(e.getMethod()),
                e.getProviderRef(),
                Payment.Status.valueOf(e.getStatus()),
                e.getVatPct(),
                e.getInvoiceNumber(),
                e.getAt()
        );
    }
}
