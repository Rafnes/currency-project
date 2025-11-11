package me.dineka.currency_project.service;

import lombok.extern.slf4j.Slf4j;
import me.dineka.currency_project.dto.CurrencyRequestDTO;
import me.dineka.currency_project.model.Currency;
import me.dineka.currency_project.model.ExchangeRate;
import me.dineka.currency_project.repository.CurrencyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

        ExchangeRate rate = new ExchangeRate();
        rate.setRate(0.0);
        rate.setUpdatedAt(LocalDateTime.now());
        currency.setExchangeRate(rate);

        currencyRepository.save(currency);
        log.info("Добавлена валюта: {}", currency);
        return currency;
    }

}
