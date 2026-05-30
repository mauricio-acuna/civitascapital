package com.magenta.servicios.infrastructure.adapter.in.web;

import com.magenta.servicios.application.usecase.*;
import com.magenta.servicios.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Gestión de órdenes de servicio")
public class OrdersController {

    private final CreateOrderUseCase createOrder;
    private final AcceptOrderUseCase acceptOrder;
    private final CancelOrderUseCase cancelOrder;
    private final ServiceOrderRepository orderRepository;

    public OrdersController(CreateOrderUseCase createOrder,
                             AcceptOrderUseCase acceptOrder,
                             CancelOrderUseCase cancelOrder,
                             ServiceOrderRepository orderRepository) {
        this.createOrder = createOrder;
        this.acceptOrder = acceptOrder;
        this.cancelOrder = cancelOrder;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Crear orden de servicio (UC-S3)")
    public ResponseEntity<ServiceOrder> create(@Valid @RequestBody CreateOrderRequest req,
                                                @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        ServiceOrder order = createOrder.execute(tenantId, req.serviceCode(), req.customerId(),
                req.propertyId(), req.operationId(), req.bankProductId(), req.inputs());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    @Operation(summary = "Obtener orden por ID")
    public ResponseEntity<ServiceOrder> getById(@PathVariable UUID id,
                                                 @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        return orderRepository.findByIdAndTenantId(id, tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Actualizar inputs / cancelar orden")
    public ResponseEntity<ServiceOrder> patch(@PathVariable UUID id,
                                               @RequestBody PatchOrderRequest req,
                                               @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        String actor = jwt.getSubject();
        if (req.cancel() != null && req.cancel()) {
            return ResponseEntity.ok(cancelOrder.execute(id, tenantId, req.reason(), actor));
        }
        if (req.inputs() != null) {
            return ResponseEntity.ok(cancelOrder.updateInputs(id, tenantId, req.inputs(), actor));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Aceptar orden y arrancar workflow (UC-S3)")
    public ResponseEntity<ServiceOrder> accept(@PathVariable UUID id,
                                                @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        return ResponseEntity.ok(acceptOrder.execute(id, tenantId, jwt.getSubject()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    @Operation(summary = "Listar órdenes por cliente o estado")
    public ResponseEntity<Page<ServiceOrder>> list(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStatus status,
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        if (customerId != null) {
            return ResponseEntity.ok(orderRepository.findByCustomerIdAndTenantId(customerId, tenantId, pageable));
        }
        if (status != null) {
            return ResponseEntity.ok(orderRepository.findByStatusAndTenantId(status, tenantId, pageable));
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    @Operation(summary = "Historial de cambios de estado de la orden")
    public ResponseEntity<?> timeline(@PathVariable UUID id,
                                       @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        return orderRepository.findByIdAndTenantId(id, tenantId)
                .map(order -> ResponseEntity.ok(order.getHistory()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/deliverables")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    @Operation(summary = "Listar entregables de la orden")
    public ResponseEntity<?> deliverables(@PathVariable UUID id,
                                           @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        return orderRepository.findByIdAndTenantId(id, tenantId)
                .map(order -> ResponseEntity.ok(order.getDeliverables()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','ADMIN')")
    @Operation(summary = "Listar pagos de la orden")
    public ResponseEntity<?> payments(@PathVariable UUID id,
                                       @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = UUID.fromString(jwt.getClaimAsString("tenant_id"));
        return orderRepository.findByIdAndTenantId(id, tenantId)
                .map(order -> ResponseEntity.ok(order.getPayments()))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Request records ──────────────────────────────────────────────────────

    public record CreateOrderRequest(
            @NotNull ServiceCode serviceCode,
            @NotNull UUID customerId,
            UUID propertyId, UUID operationId, UUID bankProductId,
            String inputs
    ) {}

    public record PatchOrderRequest(Boolean cancel, String reason, String inputs) {}
}
