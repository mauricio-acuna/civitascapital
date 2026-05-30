package com.magenta.banks.domain.model.loansimulation;

import java.math.BigDecimal;

/**
 * Value Object: tipos impositivos de la operación (derivados de la CCAA).
 */
public record TaxInfo(
    BigDecimal ivaPct,    // IVA obra nueva
    BigDecimal ajdPct,    // Actos Jurídicos Documentados
    BigDecimal itpPct     // ITP segunda mano (null si obra nueva)
) {}
