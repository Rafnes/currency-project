package me.dineka.currency_project.service;

import lombok.extern.slf4j.Slf4j;
import me.dineka.currency_project.dto.CurrencyRequestDTO;
import me.dineka.currency_project.model.Currency;
import me.dineka.currency_project.repository.CurrencyRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public Currency addCurrency(CurrencyRequestDTO dto) {
        Currency currency = new Currency();
        currency.setName(dto.getName());
        currency.setCode(dto.getCode());
        currency.setNominal(dto.getNominal());
        currencyRepository.save(currency);
        log.info("Добавлена валюта: {}", currency);
        return currency;
    }

}
