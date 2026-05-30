package com.magenta.servicios.infrastructure.adapter.out.invoice;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceGeneratorService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateInvoicePdf(InvoiceData data) {
        try (Document doc = new Document()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, bos);
            doc.open();

            doc.add(new Paragraph("MAGENTA PLATAFORMA INMOBILIARIA S.L.", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            doc.add(new Paragraph("CIF: B-12345678 | magenta.es", FontFactory.getFont(FontFactory.HELVETICA, 10)));
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("FACTURA SIMPLIFICADA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            doc.add(new Paragraph("Nº: " + data.invoiceNumber()));
            doc.add(new Paragraph("Fecha: " + data.issueDate().format(FMT)));
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Cliente: " + data.customerName()));
            doc.add(new Paragraph("NIF/NIE: " + data.customerNif()));
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Concepto: " + data.concept()));
            doc.add(new Paragraph("Servicio: " + data.serviceCode()));
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Base imponible: " + data.baseAmount() + " " + data.currency()));
            doc.add(new Paragraph("IVA (" + data.vatPct() + "%): " + data.vatAmount() + " " + data.currency()));
            doc.add(new Paragraph("TOTAL: " + data.totalAmount() + " " + data.currency(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));

            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Número de orden: " + data.orderId()));
            doc.close();

            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando factura PDF: " + e.getMessage(), e);
        }
    }

    public record InvoiceData(
            String invoiceNumber, LocalDate issueDate,
            String customerName, String customerNif,
            String serviceCode, String concept,
            BigDecimal baseAmount, BigDecimal vatPct, BigDecimal vatAmount, BigDecimal totalAmount,
            String currency, String orderId
    ) {}
}
