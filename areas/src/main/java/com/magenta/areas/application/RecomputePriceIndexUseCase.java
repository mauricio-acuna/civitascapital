package com.magenta.areas.application;

import com.magenta.areas.domain.event.PriceIndexPublished;
import com.magenta.areas.domain.exception.ZoneNotFoundException;
import com.magenta.areas.domain.model.PriceIndex;
import com.magenta.areas.domain.port.in.RecomputePriceIndexPort;
import com.magenta.areas.domain.port.out.OutboxPort;
import com.magenta.areas.domain.port.out.PriceIndexRepositoryPort;
import com.magenta.areas.domain.port.out.ZoneRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * UC-A8: Recalcula el PriceIndex para una zona, tipo y período (§9.1).
 * Aplica winsorización al 5/95 % antes de calcular la mediana.
 */
@Service
@Transactional
public class RecomputePriceIndexUseCase implements RecomputePriceIndexPort {

    private final ZoneRepositoryPort zoneRepo;
    private final PriceIndexRepositoryPort priceRepo;
    private final OutboxPort outbox;

    public RecomputePriceIndexUseCase(ZoneRepositoryPort zoneRepo,
                                       PriceIndexRepositoryPort priceRepo,
                                       OutboxPort outbox) {
        this.zoneRepo  = zoneRepo;
        this.priceRepo = priceRepo;
        this.outbox    = outbox;
    }

    @Override
    public void execute(Command command) {
        zoneRepo.findById(command.zoneId())
                .orElseThrow(() -> new ZoneNotFoundException(command.zoneId()));

        List<BigDecimal> raw = priceRepo.fetchTransactionSamples(
                command.zoneId(), command.propertyType(), command.operationType(), command.period());

        if (raw.isEmpty()) return; // sin datos, nada que calcular

        List<BigDecimal> winsorized = winsorize(raw);

        // YoY y MoM — buscamos periodo anterior para calcular delta
        BigDecimal yoy = priceRepo.findLatest(command.zoneId(), command.propertyType(),
                        command.operationType())
                .map(prev -> {
                    BigDecimal newMedian = median(winsorized);
                    BigDecimal oldPrice  = prev.getPricePerSqm().amount();
                    if (oldPrice.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                    return newMedian.subtract(oldPrice)
                            .divide(oldPrice, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                }).orElse(null);

        PriceIndex index = PriceIndex.compute(command.tenantId(), command.zoneId(),
                command.propertyType(), command.operationType(), command.period(),
                winsorized, yoy, null);

        if (index.isPublishable()) {
            priceRepo.save(index);
            outbox.publish(new PriceIndexPublished(index.getId(), index.getZoneId(),
                    index.getPropertyType(), index.getOperationType(), index.getPeriod(),
                    index.getPricePerSqm(), index.getTenantId(), "system"));
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<BigDecimal> winsorize(List<BigDecimal> values) {
        List<BigDecimal> sorted = values.stream().sorted().toList();
        int size = sorted.size();
        int low  = (int) Math.floor(size * 0.05);
        int high = (int) Math.ceil(size  * 0.95);
        return sorted.subList(low, Math.min(high, size));
    }

    private BigDecimal median(List<BigDecimal> sorted) {
        int size = sorted.size();
        if (size % 2 == 1) return sorted.get(size / 2);
        return sorted.get(size / 2 - 1).add(sorted.get(size / 2))
                .divide(BigDecimal.TWO, 2, RoundingMode.HALF_UP);
    }
}
