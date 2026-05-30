package com.magenta.banks.infrastructure.adapter.in.web;

import com.magenta.banks.application.usecase.GetBankUseCase;
import com.magenta.banks.application.usecase.ListBanksUseCase;
import com.magenta.banks.application.usecase.SearchProductsUseCase;
import com.magenta.banks.domain.model.bank.Bank;
import com.magenta.banks.domain.model.loanproduct.LoanProduct;
import com.magenta.banks.infrastructure.adapter.in.web.dto.BankResponse;
import com.magenta.banks.infrastructure.adapter.in.web.dto.LoanProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/banks")
@Tag(name = "Banks", description = "Entidades financieras")
public class BankController {

    private final ListBanksUseCase    listBanks;
    private final GetBankUseCase      getBank;
    private final SearchProductsUseCase searchProducts;

    public BankController(ListBanksUseCase listBanks, GetBankUseCase getBank,
                          SearchProductsUseCase searchProducts) {
        this.listBanks      = listBanks;
        this.getBank        = getBank;
        this.searchProducts = searchProducts;
    }

    @GetMapping
    @Operation(summary = "Listar bancos activos (UC-B1)")
    public List<BankResponse> list(@RequestHeader(value = "X-Tenant-Id", required = false) UUID tenantHeader,
                                   @AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = tenantId(jwt, tenantHeader);
        return listBanks.execute(tenantId).stream().map(BankResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener banco por ID")
    public BankResponse getById(@PathVariable UUID id) {
        return BankResponse.from(getBank.execute(id));
    }

    @GetMapping("/{id}/products")
    @Operation(summary = "Productos activos de un banco")
    public List<LoanProductResponse> products(@PathVariable UUID id) {
        return searchProducts.findByBankId(id).stream().map(LoanProductResponse::from).toList();
    }

    private UUID tenantId(Jwt jwt, UUID tenantHeader) {
        if (jwt == null) return tenantHeader;
        String raw = jwt.getClaimAsString("tenant_id");
        return raw != null ? UUID.fromString(raw) : tenantHeader;
    }
}
