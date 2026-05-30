package com.magenta.banks.infrastructure.adapter.in.web.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magenta.banks.infrastructure.adapter.out.persistence.idempotency.IdempotencyRecordId;
import com.magenta.banks.infrastructure.adapter.out.persistence.idempotency.IdempotencyRecordJpaEntity;
import com.magenta.banks.infrastructure.adapter.out.persistence.idempotency.IdempotencyRecordJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyService {

    private final IdempotencyRecordJpaRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyService(IdempotencyRecordJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<ResponseEntity<Object>> replay(UUID tenantId, String idempotencyKey, Object requestBody) {
        if (tenantId == null || idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }
        return repository.findById(new IdempotencyRecordId(tenantId, idempotencyKey))
                .map(record -> {
                    String hash = hash(requestBody);
                    if (!record.getRequestHash().equals(hash)) {
                        throw new IllegalArgumentException("Idempotency-Key reused with different request body");
                    }
                    return ResponseEntity.status(HttpStatus.valueOf(record.getResponseStatus()))
                            .body(fromJson(record.getResponseBody()));
                });
    }

    @Transactional
    public void store(UUID tenantId, String idempotencyKey, Object requestBody, int status, Object responseBody) {
        if (tenantId == null || idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        IdempotencyRecordId id = new IdempotencyRecordId(tenantId, idempotencyKey);
        if (repository.existsById(id)) {
            return;
        }
        IdempotencyRecordJpaEntity entity = new IdempotencyRecordJpaEntity();
        entity.setId(id);
        entity.setRequestHash(hash(requestBody));
        entity.setResponseStatus(status);
        entity.setResponseBody(toJson(responseBody));
        entity.setCreatedAt(Instant.now());
        repository.save(entity);
    }

    private String hash(Object value) {
        try {
            byte[] json = toJson(value).getBytes(StandardCharsets.UTF_8);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot hash idempotency request", ex);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot serialize idempotency payload", ex);
        }
    }

    private Object fromJson(String value) {
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot deserialize idempotency payload", ex);
        }
    }
}
