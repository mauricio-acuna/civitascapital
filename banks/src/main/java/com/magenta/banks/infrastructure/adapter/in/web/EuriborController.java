package com.magenta.banks.infrastructure.adapter.in.web;

import com.magenta.banks.application.usecase.GetEuriborRateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/indices/euribor")
@Tag(name = "Market Indices", description = "Indices de referencia para financiacion")
public class EuriborController {

    private final GetEuriborRateUseCase getEuriborRateUseCase;

    public EuriborController(GetEuriborRateUseCase getEuriborRateUseCase) {
        this.getEuriborRateUseCase = getEuriborRateUseCase;
    }

    @GetMapping
    @Operation(summary = "Obtener ultimo Euribor 12M disponible")
    public EuriborRateResponse latest(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate upTo) {
        return EuriborRateResponse.from(getEuriborRateUseCase.latest(upTo));
    }

    @GetMapping("/{period}")
    @Operation(summary = "Obtener Euribor 12M por periodo")
    public EuriborRateResponse byPeriod(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate period) {
        return EuriborRateResponse.from(getEuriborRateUseCase.byPeriod(period));
    }

    public record EuriborRateResponse(
            LocalDate period,
            BigDecimal rate12mPct,
            String source
    ) {
        static EuriborRateResponse from(GetEuriborRateUseCase.Result result) {
            return new EuriborRateResponse(result.period(), result.rate12mPct(), result.source());
        }
    }
}
