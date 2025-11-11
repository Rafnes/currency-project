package me.dineka.currency_project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.dineka.currency_project.dto.CurrencyRequestDTO;
import me.dineka.currency_project.dto.CurrencyResponseDTO;
import me.dineka.currency_project.model.Currency;
import me.dineka.currency_project.service.ActualCurrencyRateUpdateService;
import me.dineka.currency_project.service.CurrencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Валюта", description = "Управление валютами")
@RequestMapping("/currency")
public class CurrencyController {
    private final CurrencyService currencyService;
    private final ActualCurrencyRateUpdateService actualCurrencyRateUpdateService;

    public CurrencyController(CurrencyService currencyService, ActualCurrencyRateUpdateService actualCurrencyRateUpdateService) {
        this.currencyService = currencyService;
        this.actualCurrencyRateUpdateService = actualCurrencyRateUpdateService;
    }

    @PostMapping("/add")
    @Operation(summary = "Добавить валюту", description = "Добавление новой валюты")
    public ResponseEntity<Currency> addCurrency(@RequestBody CurrencyRequestDTO dto) {
        Currency currency = currencyService.addCurrency(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(currency);
    }

    @Operation(summary = "Получить все валюты", description = "Получение всех валют ")
    @GetMapping("/getAll")
    public ResponseEntity<List<CurrencyResponseDTO>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @Operation(summary = "Получить информацию о валюте по коду", description = "Получение информации о валюте по коду (USD, EUR итд.) ")
    @GetMapping("/get")
    public ResponseEntity<CurrencyResponseDTO> getCurrencyByCode(@RequestParam String code) {
        return ResponseEntity.ok(currencyService.getCurrencyByCode(code));
    }

    @Operation(summary = "Получить информацию о валюте по UUID", description = "Получение информации о валюте по UUID ")
    @GetMapping("/get/{id}")
    public ResponseEntity<CurrencyResponseDTO> getCurrency(@PathVariable UUID id) {
        return ResponseEntity.ok(currencyService.getCurrencyById(id));
    }

    @Operation(summary = "Обновить валюту", description = "Обновление информации о существующей валюте")
    @PutMapping("/update")
    public ResponseEntity<CurrencyResponseDTO> updateCurrency(@RequestParam UUID id, @RequestBody CurrencyRequestDTO dto) {
        CurrencyResponseDTO response = currencyService.updateCurrencyById(id, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Удалить валюту", description = "Удаление валюты")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCurrency(@RequestParam UUID id) {
        currencyService.deleteCurrencyById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить курсы валют", description = "Обновление курсов валют с сайта ЦБ")
    @GetMapping("/getRatesFromCbr")
    public ResponseEntity<String> getRatesFromCbr() {
        try {
            actualCurrencyRateUpdateService.updateRates();
            return ResponseEntity.ok("Курсы обновлены");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка обновления курсов");
        }
    }
}
