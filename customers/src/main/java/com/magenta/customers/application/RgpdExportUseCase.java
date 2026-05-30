package com.magenta.customers.application;

import com.magenta.customers.domain.model.*;
import com.magenta.customers.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * UC-C9: Derechos RGPD — exportación (portabilidad).
 * Genera un JSON con todos los datos del cliente y devuelve una URL pre-firmada.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RgpdExportUseCase {

    private final CustomerRepository customerRepository;
    private final FinancialSnapshotRepository snapshotRepository;
    private final DocumentRepository documentRepository;
    private final ConsentRepository consentRepository;
    private final DocumentStoragePort storagePort;

    @Value("${magenta.storage.presigned-url-ttl-minutes:60}")
    private int presignedTtlMinutes;

    public record Command(UUID customerId, UUID tenantId) {}
    public record Result(String downloadUrl) {}

    public Result execute(Command cmd) {
        Customer customer = customerRepository.findById(cmd.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(cmd.customerId()));

        // Construir payload de exportación como JSON (simplificado)
        var snapshots = snapshotRepository.findAllByCustomerId(cmd.customerId());
        var documents = documentRepository.findByCustomerId(cmd.customerId());
        var consents = consentRepository.findByCustomerId(cmd.customerId());

        // El export se almacena en S3 como JSON y se sirve con URL pre-firmada 24h
        String exportKey = "rgpd/exports/" + cmd.customerId() + "/export-" + System.currentTimeMillis() + ".json";

        // Serialización: en producción usar ObjectMapper; aquí marcador del contenido
        String exportJson = buildExportJson(customer, snapshots, documents, consents);
        byte[] bytes = exportJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        storagePort.upload(exportKey,
                new java.io.ByteArrayInputStream(bytes),
                bytes.length, "application/json");

        String url = storagePort.presignedDownloadUrl(exportKey,
                Duration.ofMinutes(presignedTtlMinutes * 24));

        log.info("RGPD export generated for customer={}", cmd.customerId());
        return new Result(url);
    }

    private String buildExportJson(Customer customer,
                                   java.util.List<FinancialSnapshot> snapshots,
                                   java.util.List<DocumentRef> documents,
                                   java.util.List<Consent> consents) {
        // Placeholder: la serialización real usaría ObjectMapper con módulos Jackson
        return "{\"customerId\":\"" + customer.getId() + "\","
                + "\"type\":\"" + customer.getType() + "\","
                + "\"exportedAt\":\"" + java.time.Instant.now() + "\","
                + "\"snapshotsCount\":" + snapshots.size() + ","
                + "\"documentsCount\":" + documents.size() + ","
                + "\"consentsCount\":" + consents.size() + "}";
    }
}
