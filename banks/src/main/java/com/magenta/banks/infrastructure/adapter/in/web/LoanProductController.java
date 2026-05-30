package com.magenta.banks.infrastructure.adapter.in.web;

import com.magenta.banks.application.usecase.SearchProductsUseCase;
import com.magenta.banks.domain.model.LoanCategory;
import com.magenta.banks.domain.model.Scheme;
import com.magenta.banks.domain.model.pagination.PageSpec;
import com.magenta.banks.infrastructure.adapter.in.web.dto.LoanProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Bank Products", description = "Productos hipotecarios y lineas preferentes")
public class LoanProductController {

    private final SearchProductsUseCase searchProducts;

    public LoanProductController(SearchProductsUseCase searchProducts) {
        this.searchProducts = searchProducts;
    }

    @GetMapping
    @Operation(summary = "Buscar productos hipotecarios activos")
    public ProductPage search(
            @RequestParam(required = false) String scheme,
            @RequestParam(required = false) BigDecimal ltvMin,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) BigDecimal ticketAmount,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
            @AuthenticationPrincipal Jwt jwt) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));

        var result = searchProducts.execute(new SearchProductsUseCase.Query(
                tenantId(jwt, tenantHeader),
                parseEnum(Scheme.class, scheme),
                ltvMin,
                maxAge,
                ticketAmount,
                parseEnum(LoanCategory.class, category),
                PageSpec.of(safePage, safeSize)));

        return new ProductPage(
                result.content().stream().map(LoanProductResponse::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto hipotecario por ID")
    public LoanProductResponse getById(@PathVariable UUID id) {
        return LoanProductResponse.from(searchProducts.findById(id));
    }

    public record ProductPage(
            List<LoanProductResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    private UUID tenantId(Jwt jwt, UUID tenantHeader) {
        if (jwt == null || jwt.getClaimAsString("tenant_id") == null) {
            return tenantHeader;
        }
        return UUID.fromString(jwt.getClaimAsString("tenant_id"));
    }

    private <T extends Enum<T>> T parseEnum(Class<T> type, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Enum.valueOf(type, raw.trim().toUpperCase());
    }
}
