package com.magenta.servicios.infrastructure.adapter.in.camunda;

import com.magenta.servicios.domain.model.Deliverable;
import com.magenta.servicios.domain.model.DeliverableKind;
import com.magenta.servicios.domain.port.out.DeliverableRepository;
import com.magenta.servicios.infrastructure.adapter.out.invoice.InvoiceGeneratorService;
import com.magenta.servicios.infrastructure.adapter.out.invoice.InvoiceNumberingService;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Genera la factura PDF de la orden y registra el entregable correspondiente.
 * Job type: {@code generateInvoice}.
 *
 * <p>El PDF queda registrado como {@link Deliverable} de tipo {@code INVOICE}.
 * La URI {@code invoice://{invoiceNumber}} es un placeholder hasta que S11
 * implemente el almacenamiento en objeto (URLs firmadas).
 *
 * <p>Variables de entrada:
 * <ul>
 *   <li>{@code orderId}, {@code serviceCode}, {@code customerName}, {@code customerNif}</li>
 *   <li>{@code amount} — base imponible</li>
 *   <li>{@code vatPct} — opcional, por defecto 21</li>
 * </ul>
 *
 * <p>Variables de salida: {@code invoiceNumber}, {@code deliverableId}.
 */
@Component
public class GenerateInvoiceWorker {

    private static final Logger log = LoggerFactory.getLogger(GenerateInvoiceWorker.class);
    private static final String INVOICE_SERIES = "MAG";

    private final InvoiceGeneratorService invoiceGenerator;
    private final InvoiceNumberingService invoiceNumbering;
    private final DeliverableRepository deliverableRepo;

    public GenerateInvoiceWorker(InvoiceGeneratorService invoiceGenerator,
                                  InvoiceNumberingService invoiceNumbering,
                                  DeliverableRepository deliverableRepo) {
        this.invoiceGenerator = invoiceGenerator;
        this.invoiceNumbering = invoiceNumbering;
        this.deliverableRepo = deliverableRepo;
    }

    @JobWorker(type = "generateInvoice")
    public Map<String, Object> generateInvoice(@Variable String orderId,
                                                @Variable String serviceCode,
                                                @Variable String customerName,
                                                @Variable String customerNif,
                                                @Variable BigDecimal amount,
                                                @Variable(required = false) BigDecimal vatPct) {
        log.info("Generando factura: orderId={} serviceCode={}", orderId, serviceCode);

        BigDecimal vat     = vatPct != null ? vatPct : BigDecimal.valueOf(21);
        BigDecimal vatAmt  = amount.multiply(vat).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total   = amount.add(vatAmt);

        String invoiceNumber = invoiceNumbering.nextInvoiceNumber(INVOICE_SERIES);

        byte[] pdf = invoiceGenerator.generateInvoicePdf(new InvoiceGeneratorService.InvoiceData(
                invoiceNumber, LocalDate.now(),
                customerName, customerNif,
                serviceCode, "Servicios inmobiliarios Magenta",
                amount, vat, vatAmt, total,
                "EUR", orderId));

        String sha256 = sha256Hex(pdf);

        Deliverable deliverable = new Deliverable(
                UUID.randomUUID(),
                UUID.fromString(orderId),
                DeliverableKind.INVOICE,
                "invoice://" + invoiceNumber,   // S11: sustituir por URL firmada
                sha256,
                null,
                Instant.now());

        Deliverable saved = deliverableRepo.save(deliverable);

        log.info("Factura generada: invoiceNumber={} deliverableId={}", invoiceNumber, saved.getId());
        return Map.of(
                "invoiceNumber",  invoiceNumber,
                "deliverableId",  saved.getId().toString());
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
