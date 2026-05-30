package com.magenta.servicios.infrastructure.adapter.in.web;

import com.magenta.servicios.application.usecase.*;
import com.magenta.servicios.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Catalog", description = "Catálogo de servicios disponibles")
public class CatalogController {

    private final ListCatalogUseCase listCatalog;
    private final QuoteServiceUseCase quoteService;
    private final ComparePackagesUseCase comparePackages;

    public CatalogController(ListCatalogUseCase listCatalog,
                              QuoteServiceUseCase quoteService,
                              ComparePackagesUseCase comparePackages) {
        this.listCatalog = listCatalog;
        this.quoteService = quoteService;
        this.comparePackages = comparePackages;
    }

    @GetMapping
    @Operation(summary = "Listar catálogo de servicios activos")
    public ResponseEntity<List<ServiceDefinition>> listAll() {
        return ResponseEntity.ok(listCatalog.execute());
    }

    @GetMapping("/{code}")
    @Operation(summary = "Obtener un servicio por código")
    public ResponseEntity<ServiceDefinition> getByCode(@PathVariable ServiceCode code) {
        return listCatalog.execute().stream()
                .filter(s -> s.getCode() == code)
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{code}/quote")
    @Operation(summary = "Cotizar un servicio (UC-S2)")
    public ResponseEntity<QuoteServiceUseCase.QuoteResult> quote(
            @PathVariable ServiceCode code,
            @RequestBody QuoteRequest req) {
        QuoteServiceUseCase.QuoteResult result = quoteService.execute(
                code, req.customerId(), req.propertyId(), req.operationId(), req.extra());
        return ResponseEntity.ok(result);
    }

    public record QuoteRequest(UUID customerId, UUID propertyId, UUID operationId, String extra) {}
}
