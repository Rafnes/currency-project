package me.dineka.currency_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.dineka.currency_project.dto.CurrencyRequestDTO;
import me.dineka.currency_project.model.Currency;
import me.dineka.currency_project.service.CurrencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Валюта", description = "Управление валютой")
@RequestMapping("/currency")
public class CurrencyController {
    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @PostMapping("/add")
    @Operation(summary = "Добавить валюту", description = "Добавление новой валюты")
    public ResponseEntity<Currency> addCurrency(@RequestBody CurrencyRequestDTO dto) {
        Currency currency = currencyService.addCurrency(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(currency);
    }
}
