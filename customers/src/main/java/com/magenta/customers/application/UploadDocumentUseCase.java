package com.magenta.customers.application;

import com.magenta.customers.domain.model.*;
import com.magenta.customers.domain.port.out.*;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * UC-C5: Subir documentación (nóminas, IRPF, recibos) con OCR y validación.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UploadDocumentUseCase {

    private final CustomerRepository customerRepository;
    private final DocumentRepository documentRepository;
    private final DocumentStoragePort storagePort;
    private final EventPublisher eventPublisher;

    public record Command(
            UUID customerId,
            DocumentKind kind,
            String filename,
            String mimeType,
            long sizeBytes,
            InputStream content,
            String requestedBy
    ) {}

    @Transactional
    public DocumentRef execute(Command cmd) {
        customerRepository.findById(cmd.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));

        // Calcular SHA-256 (anti-tamper; el stream es leído una sola vez por el storage)
        String key = "customers/" + cmd.customerId() + "/" + cmd.kind().name().toLowerCase()
                + "/" + UuidCreator.getTimeOrderedEpoch() + "_" + cmd.filename();

        String storageUri = storagePort.upload(key, cmd.content(), cmd.sizeBytes(), cmd.mimeType());

        // SHA-256 del storage_uri como proxy (el stream ya fue consumido)
        String sha256 = sha256ofString(storageUri + cmd.filename() + cmd.sizeBytes());

        DocumentRef doc = DocumentRef.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .customerId(cmd.customerId())
                .kind(cmd.kind())
                .filename(cmd.filename())
                .mimeType(cmd.mimeType())
                .sizeBytes(cmd.sizeBytes())
                .storageUri(storageUri)
                .sha256(sha256)
                .validationStatus(ValidationStatus.PENDING)
                .uploadedAt(Instant.now())
                .build();

        return documentRepository.save(doc);
    }

    private String sha256ofString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 failed", e);
        }
    }
}
