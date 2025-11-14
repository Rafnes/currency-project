package me.dineka.currency_project.mapper;

import me.dineka.currency_project.dto.CurrencyRequestDTO;
import me.dineka.currency_project.dto.CurrencyResponseDTO;
import me.dineka.currency_project.model.Currency;
import me.dineka.currency_project.model.ExchangeRate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CurrencyMapper {
    public CurrencyResponseDTO toDTO(Currency currency) {
        CurrencyResponseDTO response = new CurrencyResponseDTO();
        response.setName(currency.getName());
        response.setCode(currency.getCode());
        response.setNominal(currency.getNominal());
        response.setRate(currency.getExchangeRate() == null ? null : currency.getExchangeRate().getRate());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        response.setUpdatedAt(currency.getExchangeRate().getUpdatedAt().format(formatter));
        return response;
    }

    public Currency toEntity(CurrencyRequestDTO dto) {
        Currency currency = new Currency();
        currency.setName(dto.getName());
        currency.setCode(dto.getCode().toUpperCase());
        currency.setNominal(dto.getNominal());

        ExchangeRate rate = new ExchangeRate();
        rate.setRate(dto.getRate() == null ? null : dto.getRate());
        rate.setUpdatedAt(LocalDateTime.now());
        currency.setExchangeRate(rate);
        return currency;
    }
}
