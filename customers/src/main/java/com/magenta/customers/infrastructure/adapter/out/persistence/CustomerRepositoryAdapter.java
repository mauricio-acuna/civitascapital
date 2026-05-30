package com.magenta.customers.infrastructure.adapter.out.persistence;

import com.magenta.customers.domain.model.Customer;
import com.magenta.customers.domain.model.CustomerStatus;
import com.magenta.customers.domain.port.out.CustomerRepository;
import com.magenta.customers.infrastructure.adapter.out.persistence.entity.CustomerJpaEntity;
import com.magenta.customers.infrastructure.adapter.out.persistence.jpa.JpaCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final JpaCustomerRepository jpa;
    private final CustomerPersistenceMapper mapper;

    @Override
    public Customer save(Customer customer) {
        CustomerJpaEntity entity = mapper.toEntity(customer);
        return mapper.toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpa.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByKeycloakUserId(String keycloakUserId) {
        return jpa.findByKeycloakUserIdAndDeletedAtIsNull(keycloakUserId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByNifHash(String nifHash) {
        return jpa.existsByNifHash(nifHash);
    }

    @Override
    public boolean existsByEmailHash(String emailHash) {
        return jpa.existsByEmailHash(emailHash);
    }

    @Override
    public void softDelete(UUID id, String deletedBy) {
        int updated = jpa.softDelete(id, Instant.now(), deletedBy);
        if (updated == 0) throw new IllegalStateException("Customer not found or already deleted: " + id);
    }

    @Override
    public Customer updateStatus(UUID id, CustomerStatus status, String updatedBy, long version) {
        CustomerJpaEntity entity = jpa.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        entity.setStatus(status.name());
        entity.setUpdatedBy(updatedBy);
        return mapper.toDomain(jpa.save(entity));
    }
}
