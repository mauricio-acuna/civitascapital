package com.magenta.servicios.infrastructure.adapter.out.invoice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class InvoiceNumberingService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public String nextInvoiceNumber(String series) {
        int year = java.time.Year.now().getValue();

        // Ensure row exists for current year
        em.createNativeQuery(
                "INSERT INTO services.invoice_sequences (series, year, last_number) " +
                "VALUES (:series, :year, 0) ON CONFLICT DO NOTHING")
                .setParameter("series", series)
                .setParameter("year", year)
                .executeUpdate();

        // Atomic increment
        em.createNativeQuery(
                "UPDATE services.invoice_sequences " +
                "SET last_number = last_number + 1 " +
                "WHERE series = :series AND year = :year")
                .setParameter("series", series)
                .setParameter("year", year)
                .executeUpdate();

        int number = ((Number) em.createNativeQuery(
                "SELECT last_number FROM services.invoice_sequences " +
                "WHERE series = :series AND year = :year")
                .setParameter("series", series)
                .setParameter("year", year)
                .getSingleResult()).intValue();

        return String.format("%s-%d-%05d", series, year, number);
    }
}
